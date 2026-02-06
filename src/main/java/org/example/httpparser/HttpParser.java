package org.example.httpparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {
    public HttpRequest parse(InputStream in) throws IOException {

        // 1. Request Line
        String requestLine = readLine(in);
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("The request is empty");
        }

        String[] parts = requestLine.split(" ");
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

        // 2. Headers
        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = readLine(in)).isEmpty()) {
            int colon = line.indexOf(':');
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            headers.put(key, value);
        }

        // 3. Body
        byte[] body = new byte[0];
        if (headers.containsKey("Content-Length")) {
            int length = Integer.parseInt(headers.get("Content-Length"));
            body = in.readNBytes(length);
        }

        return new HttpRequest(method, path, query, version, headers, body);

    }

    private String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\r') {
                in.read();
                break;
            }
            sb.append((char) c);
        }
        return sb.toString();
    }
}