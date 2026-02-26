package org.juv25d.handler;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Resolves MIME types based on file extensions.
 * Used to set the correct Content-Type header for HTTP responses.
 */
public class MimeTypeResolver {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        // Text types
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("txt", "text/plain");

        // Image types
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("webp", "image/webp");

        // Font types
        MIME_TYPES.put("woff", "font/woff");
        MIME_TYPES.put("woff2", "font/woff2");
        MIME_TYPES.put("ttf", "font/ttf");
        MIME_TYPES.put("otf", "font/otf");

        // Other common types
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("zip", "application/zip");
    }

    private MimeTypeResolver() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the MIME type for a given filename.
     *
     * @param filename the name of the file (e.g., "index.html", "styles.css")
     * @return the MIME type (e.g., "text/html", "text/css")
     *         or "application/octet-stream" if unknown
     */
    public static String getMimeType(@Nullable String filename) {
        if (filename == null || filename.isEmpty()) {
            return "application/octet-stream";
        }

        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            // No extension or dot is last character
            return "application/octet-stream";
        }

        String extension = filename.substring(lastDot + 1).toLowerCase();
        return MIME_TYPES.getOrDefault(extension, "application/octet-stream");
    }
}
