package org.juv25d.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import org.jspecify.annotations.Nullable;

public class HttpParser {

    private static final String CONTENT_LENGTH = "Content-Length";

    public HttpRequest parse(InputStream in) throws IOException {

        String requestLine = readLine(in);
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("The request is empty");
        }

        String[] parts = requestLine.split("\\s+");
        if (parts.length < 3) {
            throw new IOException("Malformed request line: " + requestLine);
        }
        String method = parts[0];
        String fullPath = parts[1];
        String version = parts[2];

        String path;
        String query = null;

        int qIndex = fullPath.indexOf('?');
        if (qIndex >= 0) {
            path = fullPath.substring(0, qIndex);
            query = fullPath.substring(qIndex + 1);
        } else {
            path = fullPath;
        }

        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon <= 0) {
                throw new IOException("Malformed header line: " + line);
            }
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            headers.put(key, value);
        }

        byte[] body = new byte[0];
        if (headers.containsKey(CONTENT_LENGTH)) {
            int length;
            try {
                length = Integer.parseInt(headers.get(CONTENT_LENGTH));
            } catch (NumberFormatException e) {
                throw new IOException("Invalid Content-Length: " + headers.get(CONTENT_LENGTH), e);
            }
            if (length < 0) {
                throw new IOException("Negative Content-Length: " + length);
            }
            body = in.readNBytes(length);
        }
        return new HttpRequest(method, path, query, version, headers, body, "UNKNOWN");
    }

    @Nullable private String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                sb.append((char) b);
            }
        }
        if (b == -1 && sb.isEmpty()) {
            return null;
        }
        return sb.toString();
    }
}
