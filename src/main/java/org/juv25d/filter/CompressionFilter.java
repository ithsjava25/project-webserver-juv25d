package org.juv25d.filter;

import org.juv25d.config.CompressionConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;


@Global(order = 10)
public class CompressionFilter implements Filter{
    private static final Logger LOGGER = ServerLogging.getLogger();

    private final boolean enabled;
    private final int minCompressSize;

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        if (!enabled) {
            chain.doFilter(req, res);
            return;
        }

        if (!acceptsGzip(req)) {
            chain.doFilter(req, res);
            return;
        }

        chain.doFilter(req, res);

        byte[] body = res.body();
        if (body.length < minCompressSize) {
            return;
        }

        if (res.getHeader("Content-Encoding") != null) {
            return;
        }

        byte[] compressed = compress(body);
        res.setBody(compressed);
        res.setHeader("Content-Encoding", "gzip");

        String existingVary = res.getHeader("Vary");
        if (existingVary != null && !existingVary.isEmpty()) {
            res.setHeader("Vary", existingVary + ", Accept-Encoding");
        } else {
            res.setHeader("Vary", "Accept-Encoding");
        }

        LOGGER.info("Compressed " + body.length + " bytes to " + compressed.length + " bytes");
    }

    public CompressionFilter() {
        CompressionConfig config = new CompressionConfig();
        this.enabled = config.isEnabled();
        this.minCompressSize = config.getMinCompressSize();
    }

    public CompressionFilter(boolean enabled, int minCompressSize) {
        this.enabled = enabled;
        this.minCompressSize = minCompressSize;
    }

    private boolean acceptsGzip(HttpRequest req) {
        String acceptEncoding = req.headers().entrySet().stream()
            .filter(e -> e.getKey().equalsIgnoreCase("Accept-Encoding"))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);

        if (acceptEncoding == null || acceptEncoding.isEmpty()) {
            return false;
        }

        return Arrays.stream(acceptEncoding.split(","))
            .map(String::trim)
            .filter(this::isGzipWithQualityAboveZero)
            .anyMatch(e -> e.split(";")[0].trim().equalsIgnoreCase("gzip"));
    }

    private boolean isGzipWithQualityAboveZero(String encoding) {
        String[] parts = encoding.split(";");
        String name = parts[0].trim();
        if (!name.equalsIgnoreCase("gzip")) return false;

        if (parts.length > 1) {
            String q = parts[1].trim();
            if (q.startsWith("q=")) {
                try {
                    double quality = Double.parseDouble(q.substring(2));
                    return quality > 0;
                } catch (NumberFormatException ignored) {}
            }
        }
        return true;
    }

    private byte [] compress(byte [] data) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipstream = new GZIPOutputStream(byteStream);

        gzipstream.write(data);
        gzipstream.close();
        return byteStream.toByteArray();
    }
}
