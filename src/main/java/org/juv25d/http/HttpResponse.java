package org.juv25d.http;

import java.util.*;

/**
 * Represents an HTTP Response.
 * Changed to be mutable to allow Filters and Plugins in the Pipeline
 * to modify status, headers, and body during processing.
 */
public class HttpResponse {

    private int statusCode;
    private String statusText;
    private Map<String, String> headers;
    private byte[] body;

    public HttpResponse() {
        this.statusCode = 200;
        this.statusText = "OK";
        this.headers = new LinkedHashMap<>();
        this.body = new byte[0];
    }

    public HttpResponse(int statusCode, String statusText, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.statusText = Objects.requireNonNull(statusText, "statusText must not be null");
        this.headers = new LinkedHashMap<>(headers != null ? headers : Map.of());
        this.body = body != null ? body.clone() : new byte[0];
    }

    public int statusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String statusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        Objects.requireNonNull(statusText, "statusText must not be null");
        this.statusText = statusText;
    }

    public Map<String, String> headers() {
        return headers;
    }

    @org.jspecify.annotations.Nullable public String getHeader(@org.jspecify.annotations.Nullable String name) {
        if (name == null) {
            return null;
        }
        for (var entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public byte[] body() {
        return body != null ? body.clone() : new byte[0];
    }

    public void setBody(byte[] body) {
        this.body = body != null ? body.clone() : new byte[0];
    }
}
