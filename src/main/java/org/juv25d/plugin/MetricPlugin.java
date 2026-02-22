package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Logger;

public class MetricPlugin implements Plugin {


    private static final String SERVER_NAME = "juv25d-webserver";
    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private final String version;
    private final String commit;

    public MetricPlugin() {
        Logger logger = ServerLogging.getLogger();
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("build.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            logger.warning("Error loading build.properties: " + e.getMessage());
        }
        this.version = escapeJson(props.getProperty("build.version", "dev"));
        String commitValue = props.getProperty("build.commit");
        if (commitValue == null || commitValue.isBlank()) {
            this.commit = "unknown";
        } else {
            this.commit = escapeJson(commitValue);
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long responseTimeUs =
            (System.nanoTime() - req.creationTimeNanos()) / 1_000;
        String localTime = ZonedDateTime
            .now(ZoneId.systemDefault())
            .format(TIME_FORMAT);
        String utcTime = ZonedDateTime
            .now(ZoneId.of("UTC"))
            .format(TIME_FORMAT);

        String jsonBody = String.format("""
            {
              "localTime": "%s",
              "utcTime": "%s",
              "server": "%s",
              "buildVersion": "%s",
              "gitCommit": "%s",
              "responseTimeUs": %d,
              "memory": {
                "usedBytes": %d,
                "maxBytes": %d
              }
            }
            """,
            localTime,
            utcTime,
            SERVER_NAME,
            version,
            commit,
            responseTimeUs,
            usedMemory,
            maxMemory
        );

        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

        res.setStatusCode(200);
        res.setHeader("Content-Type", "application/json");
        res.setHeader("Content-Length", String.valueOf(bodyBytes.length));
        res.setBody(bodyBytes);
    }
}
