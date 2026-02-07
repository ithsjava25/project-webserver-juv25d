package org.juv25d;

import java.util.Map;

public record HttpRequest(
        String method,
        String path,
        String queryString,
        String httpVersion,
        Map<String, String> headers,
        byte[] body
) {}
