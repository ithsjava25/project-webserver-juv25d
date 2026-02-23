package org.juv25d.http;

import java.util.Map;

public record HttpRequest(
        String method,
        String path,
        String queryString,
        String httpVersion,
        Map<String, String> headers,
        byte[] body,
        String remoteIp,
        long creationTimeNanos
) {
    public HttpRequest(String method, String path, String queryString, String httpVersion, Map<String, String> headers, byte[] body, String remoteIp) {
        this(method, path, queryString, httpVersion, headers, body, remoteIp, System.nanoTime());
    }
}
