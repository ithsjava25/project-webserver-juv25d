package org.example.httpparser;

import java.util.Map;

public record HttpRequest(
        String method,
        String path,
        String queryString,
        String httpVersion,
        Map<String, String> headers,
        byte[] body
) {}
