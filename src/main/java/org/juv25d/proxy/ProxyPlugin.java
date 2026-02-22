package org.juv25d.proxy;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;
import org.juv25d.plugin.Plugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.logging.Logger;

public class ProxyPlugin implements Plugin {
    private static final Logger logger = ServerLogging.getLogger();
    private final ProxyRoute proxyRoute;
    private final HttpClient httpClient;

    public ProxyPlugin(ProxyRoute proxyRoute) {
        this.proxyRoute = proxyRoute;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        String baseRoute = proxyRoute.getBaseRoute();
        String targetPath = req.path().substring(baseRoute.length());
        String upstreamUrl = proxyRoute.buildUrl(targetPath, req.queryString());

        logger.info(String.format("Proxying %s %s?%s -> %s",
            req.method(), req.path(), req.queryString(), upstreamUrl));

        Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
            .uri(URI.create(upstreamUrl))
            .method(req.method(),
                req.body().length > 0
                    ? BodyPublishers.ofByteArray(req.body())
                    : BodyPublishers.noBody()
                );

        // copy request headers and pass to new HttpRequest
        for (Map.Entry<String, String> header : req.headers().entrySet()) {
            requestBuilder.header(header.getKey(), header.getValue());
        }

        try {
            java.net.http.HttpResponse<byte[]> upstreamResponse = httpClient.send(
                requestBuilder.build(),
                BodyHandlers.ofByteArray()
            );

            // relay the upstream response back to the client including the headers
            res.setStatusCode(upstreamResponse.statusCode());
            res.setStatusText("OK");
            res.setBody(upstreamResponse.body());

            upstreamResponse.headers().map().forEach((name, values) -> {
                if (!values.isEmpty()) {
                    res.setHeader(name, values.get(0));
                }
            });

        } catch (Exception e) {
            logger.warning("Something went wrong.");
        }
    }
}
