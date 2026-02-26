package org.juv25d.filter;

import org.juv25d.config.BodySizeConfig;
import org.juv25d.config.FilterConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Global(order = 1)
public class BodySizeFilter implements Filter{

    private static final Logger logger = ServerLogging.getLogger();

    private final long maxSizeBytes;
    private final boolean enabled;

    public BodySizeFilter(long maxSizeMb) {
        if (maxSizeMb <= 0) {
            throw new IllegalArgumentException("maxSizeMb must be positive");
        }
        this.maxSizeBytes = maxSizeMb * 1024 * 1024;
        this.enabled = true;

        logger.info("BodySizeFilter initialized with max size: " + maxSizeMb + " MB");
    }

    public BodySizeFilter() {
        BodySizeConfig config = new BodySizeConfig();
        this.enabled = config.isEnabled();
        this.maxSizeBytes = config.getMaxSizeMb() * 1024 * 1024;
    }


    @Override
    public void init(FilterConfig filterConfig) {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        if (!enabled) {
            chain.doFilter(req, res);
            return;
        }

        if (shouldCheckBodySize(req)) {
            String contentLength = req.headers().get("Content-Length");

            if (contentLength == null) {
                logMissingContentLength(req);
                sendPayloadTooLarge(res, "Missing Content-Length header");
                return;
            }

            try {
                long bodySize = Long.parseLong(contentLength);

                if (bodySize < 0) {
                    logInvalidContentLength(req, contentLength);
                    sendPayloadTooLarge(res, "Invalid Content-Length: " + contentLength);
                    return;
                }
                if (bodySize > maxSizeBytes) {
                    logBodySizeExceeded(req, bodySize);
                    sendPayloadTooLarge(res, "Body size " + bodySize + " bytes exceeds maximum " + maxSizeBytes + " bytes");
                    return;
                }
            } catch (NumberFormatException e) {
                logInvalidContentLength(req, contentLength);
                sendPayloadTooLarge(res, "Invalid Content-Length: " + contentLength);
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    private boolean shouldCheckBodySize(HttpRequest req) {
        String method = req.method();
        return method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("PATCH");
    }

    private void logBodySizeExceeded(HttpRequest req, long bodySize) {
        logger.warning(String.format(
            "Request body too large - IP: %s, Method: %s, Path: %s, Size: %d bytes, Max: %d bytes",
            req.remoteIp(), req.method(), req.path(), bodySize, maxSizeBytes
        ));
    }

    private void logMissingContentLength(HttpRequest req) {
        logger.warning(String.format(
            "Missing Content-Length - IP: %s, Method: %s, Path: %s",
            req.remoteIp(), req.method(), req.path()
        ));
    }

    private void logInvalidContentLength(HttpRequest req, String contentLength) {
        logger.warning(String.format(
            "Invalid Content-Length - IP: %s, Method: %s, Path: %s, Content-Length: %s",
            req.remoteIp(), req.method(), req.path(), contentLength
        ));
    }

    private void sendPayloadTooLarge(HttpResponse res, String message) {
        byte[] body = ("413 Payload Too Large: " + message + "\n")
            .getBytes(StandardCharsets.UTF_8);

        res.setStatusCode(413);
        res.setStatusText("Payload Too Large");
        res.setHeader("Content-Type", "text/plain; charset=utf-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setBody(body);
    }

}
