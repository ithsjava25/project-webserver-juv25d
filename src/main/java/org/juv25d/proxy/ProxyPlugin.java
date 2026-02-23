package org.juv25d.proxy;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.http.HttpStatus;
import org.juv25d.logging.ServerLogging;
import org.juv25d.plugin.Plugin;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
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

    public ProxyPlugin(ProxyRoute proxyRoute, HttpClient httpClient) {
        this.proxyRoute = proxyRoute;
        this.httpClient = httpClient;
    }

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        String baseRoute = proxyRoute.getBaseRoute();
        String targetPath = req.path().substring(baseRoute.length());
        String upstreamUrl = proxyRoute.buildUrl(targetPath, req.queryString());

        String query = req.queryString();
        String fullPath = req.path();

        if (query != null && !query.isEmpty()) {
            fullPath += "?" + query;
        }

        logger.info(String.format("Proxying %s %s -> %s",
            req.method(), fullPath, upstreamUrl));

        Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
            .uri(URI.create(upstreamUrl))
            .method(req.method(),
                req.body().length > 0
                    ? BodyPublishers.ofByteArray(req.body())
                    : BodyPublishers.noBody()
                );

        // copy request headers and pass to new HttpRequest
        for (Map.Entry<String, String> header : req.headers().entrySet()) {
            if (!isRestrictedHeader(header.getKey())) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
        }

        try {
            java.net.http.HttpResponse<byte[]> upstreamResponse = httpClient.send(
                requestBuilder.build(),
                BodyHandlers.ofByteArray()
            );

            // relay the upstream response back to the client including the headers
            res.setStatusCode(upstreamResponse.statusCode());
            res.setStatusText(String.valueOf(
                HttpStatus.getStatusFromCode(upstreamResponse.statusCode()).getDescription()
                )
            );
            res.setBody(upstreamResponse.body());

            upstreamResponse.headers().map().forEach((name, values) -> {
                if (!values.isEmpty()) {
                    res.setHeader(name, values.get(0));
                }
            });

            logger.info(String.format("Successful proxy %s %s -> %s (upstream: %d %s)",
                req.method(), req.path(), upstreamUrl, res.statusCode(), res.statusText()));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            res.setStatusCode(500);
            res.setStatusText(String.valueOf(
                HttpStatus.getStatusFromCode(res.statusCode()).getDescription()
            ));

            logger.warning(String.format("Request interrupted while proxying %s %s -> %s",
                req.method(), req.path(), upstreamUrl));

        } catch (ConnectException e) {
            res.setStatusCode(502);
            res.setStatusText(String.valueOf(
                HttpStatus.getStatusFromCode(res.statusCode()).getDescription()
            ));
            logger.warning(String.format("Connection failed to upstream server %s",
                upstreamUrl));

        } catch (HttpTimeoutException e) {
            res.setStatusCode(504);
            res.setStatusText(String.valueOf(
                HttpStatus.getStatusFromCode(res.statusCode()).getDescription()
            ));

            logger.warning(String.format("Timeout connecting to upstream server %s",
                upstreamUrl));

        } catch (Exception e) {
            res.setStatusCode(502);
            res.setStatusText(String.valueOf(
                HttpStatus.getStatusFromCode(res.statusCode()).getDescription()
            ));

            logger.warning(String.format("Proxy error for %s %s -> %s",
                req.method(), req.path(), upstreamUrl));
        }
    }

    private boolean isRestrictedHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return lower.equals("connection") ||
            lower.equals("content-length") ||
            lower.equals("host") ||
            lower.equals("upgrade") ||
            lower.equals("http2-settings") ||
            lower.equals("te") ||
            lower.equals("trailer") ||
            lower.equals("transfer-encoding");
    }
}
