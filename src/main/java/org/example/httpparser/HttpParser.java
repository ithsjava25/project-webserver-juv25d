package org.example.httpparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {
    public HttpRequest parse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        // 1. Request Line
        String requestLine = reader.readLine();
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
        while (!(line = reader.readLine()).isEmpty()) {
            int colon = line.indexOf(':');
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            headers.put(key, value);
        }

        // 3. Body
        byte[] body = new byte[0];
        if (headers.containsKey("Content-Length")) {
            int length = Integer.parseInt(headers.get("Content-Lenght"));
            body = in.readNBytes(length);
        }

        return new HttpRequest(method, path, query, version, headers, body);

    }
}