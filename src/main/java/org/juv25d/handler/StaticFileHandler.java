package org.juv25d.handler;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles serving static files from the /resources/static/ directory.
 * <p>
 * URL mapping:
 * - GET / → /resources/static/index.html
 * - GET /about.html → /resources/static/about.html
 * - GET /css/styles.css → /resources/static/css/styles.css
 * <p>
 * Security: Validates paths to prevent directory traversal attacks.
 */
public class StaticFileHandler {

    private static final Logger logger = ServerLogging.getLogger();
    private static final String STATIC_DIR = "/static/";

    // Cache static files for 5 seconds, short max_age so that changes appear quickly and is easier to develop around
    private static final int MAX_AGE_SECONDS = 5;

    private StaticFileHandler() {
        // Utility class - prevent instantiation
    }

    /**
     * Handles a static file request.
     *
     * @param request the HTTP request
     * @return an HTTP response with the file content or an error response
     */
    public static HttpResponse handle(HttpRequest request) {
        String path = request.path();

        // Only handle GET requests
        if (!request.method().equalsIgnoreCase("GET")) {
            return createErrorResponse(405, "Method Not Allowed");
        }

        // Validate path for security
        if (!isPathSafe(path)) {
            logger.warning("Security violation: Attempted path traversal with path: " + path);
            return createErrorResponse(403, "Forbidden");
        }

        // Map URL path to resource path
        String resourcePath = mapToResourcePath(path);

        logger.info("Attempting to serve static file: " + resourcePath);

        // Try to load and serve the file
        try {
            byte[] fileContent = loadResource(resourcePath);
            String mimeType = MimeTypeResolver.getMimeType(resourcePath);

            // Add charset for text-based content types
            if (mimeType.startsWith("text/") ||
                mimeType.contains("javascript") ||
                mimeType.contains("json")) {
                mimeType += "; charset=utf-8";
            }

            String etag = computeStrongEtag(fileContent);

            String ifNoneMatch = getHeaderIgnoreCase(request.headers(), "If-None-Match");
            if (etagMatches(ifNoneMatch, etag)) {
                Map<String, String> headers = new HashMap<>();
                headers.put("ETag", etag);
                headers.put("Cache-Control", "public, max-age=" + MAX_AGE_SECONDS);

                logger.info("ETag match for " + resourcePath + " -> 304 Not Modified");
                return new HttpResponse(304, "Not Modified", headers, new byte[0]);
            }

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", mimeType);
            headers.put("ETag", etag);
            headers.put("Cache-Control", "public, max-age=" + MAX_AGE_SECONDS);

            logger.info("Successfully served: " + resourcePath + " (" + mimeType + ")");
            return new HttpResponse(200, "OK", headers, fileContent);

        } catch (IOException e) {
            logger.log(Level.WARNING, "File not found: " + resourcePath);
            return createNotFoundResponse(path);
        }
    }

    /**
     * Validates that the path is safe and doesn't contain directory traversal attempts.
     *
     * @param path the requested path
     * @return true if the path is safe, false otherwise
     */
    private static boolean isPathSafe(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        // Reject paths with directory traversal attempts
        if (path.contains("..") || path.contains("//") || path.contains("\\")) {
            return false;
        }

        // Reject absolute paths (should start with /)
        if (!path.startsWith("/")) {
            return false;
        }

        return true;
    }

    /**
     * Maps a URL path to a resource path in /resources/static/.
     * <p>
     * Examples:
     * - "/" → "/static/index.html"
     * - "/about.html" → "/static/about.html"
     * - "/css/styles.css" → "/static/css/styles.css"
     *
     * @param urlPath the URL path from the request
     * @return the resource path
     */
    private static String mapToResourcePath(String urlPath) {
        // Handle root path - serve index.html
        if (urlPath.equals("/")) {
            return STATIC_DIR + "index.html";
        }

        // Remove leading slash and prepend /static/
        String cleanPath = urlPath.startsWith("/") ? urlPath.substring(1) : urlPath;
        return STATIC_DIR + cleanPath;
    }

    /**
     * Loads a resource from the classpath.
     *
     * @param resourcePath the path to the resource (e.g., "/static/index.html")
     * @return the file content as bytes
     * @throws IOException if the resource cannot be loaded
     */
    private static byte[] loadResource(String resourcePath) throws IOException {
        InputStream inputStream = StaticFileHandler.class.getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        try (inputStream) {
            return inputStream.readAllBytes();
        }
    }

    @Nullable private static String getHeaderIgnoreCase(Map<String, String> headers, String name) {
       if (headers == null || headers.isEmpty() || name == null) {
           return null;
       }
       for (Map.Entry<String, String> e : headers.entrySet()) {
           if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
               return e.getValue();
           }
       }
        return null;
    }

    private static String computeStrongEtag(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return "\"" + toHex(hash) + "\"";
        }catch (NoSuchAlgorithmException e) {
            throw new AssertionError("SHA-256 not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hex = "0123456789abcdef".toCharArray();
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = hex[v >>> 4];
            out[i * 2 + 1] = hex[v & 0x0F];
        }
        return new String(out);
    }

    @Nullable private static String opaqueTag(@Nullable String etag) {
        if (etag == null) return null;
        String e = etag.trim();
        return e.startsWith("W/") ? e.substring(2) : e;
    }

    private static boolean etagMatches(@Nullable String ifNoneMatchHeader, String currentEtag) {
        if (ifNoneMatchHeader == null || ifNoneMatchHeader.isBlank()) {
            return false;
        }
        String value = ifNoneMatchHeader.trim();
        if (value.equals("*")) {
            return true;
        }

        String[] parts = value.split(",");
        for (String part : parts) {
            String tag = opaqueTag(part);
            if (part != null && tag != null && tag.equals(opaqueTag(currentEtag))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a 404 Not Found error response with HTML content.
     */
    private static HttpResponse createNotFoundResponse(String path) {
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>404 Not Found</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            max-width: 600px;
                            margin: 100px auto;
                            text-align: center;
                        }
                        h1 { color: #e74c3c; }
                        p { color: #666; }
                    </style>
                </head>
                <body>
                    <h1>404 - Not Found</h1>
                    <p>The requested resource <code>%s</code> was not found on this server.</p>
                </body>
                </html>
                """.formatted(path);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/html; charset=utf-8");

        return new HttpResponse(404, "Not Found", headers, html.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a generic error response.
     */
    private static HttpResponse createErrorResponse(int statusCode, String statusText) {
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>%d %s</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            max-width: 600px;
                            margin: 100px auto;
                            text-align: center;
                        }
                        h1 { color: #e74c3c; }
                    </style>
                </head>
                <body>
                    <h1>%d - %s</h1>
                </body>
                </html>
                """.formatted(statusCode, statusText, statusCode, statusText);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/html; charset=utf-8");

        return new HttpResponse(statusCode, statusText, headers, html.getBytes(StandardCharsets.UTF_8));
    }
}
