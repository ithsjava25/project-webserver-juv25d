package org.juv25d.http;

import java.util.*;

public class HttpResponse {

    private int statusCode;
    private String statusText;
    private Map<String, String> headers;
    private byte[] body;

    public HttpResponse(int statusCode, String statusText, Map<String, String> headers, byte[] body) {
        Objects.requireNonNull(statusText, "statusText must not be null");
        Objects.requireNonNull(headers, "headers must not be null");
        Objects.requireNonNull(body, "body must not be null");
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body.clone();
    }

    public int statusCode(){
        return statusCode;
    }

    public String statusText(){
        return statusText;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public byte[] body(){
        return body.clone();
    }

    // Setters for Pipeline plugin integration
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public void setHeaders(java.util.Map<String, String> headers) {
        this.headers = new java.util.LinkedHashMap<>(headers);
    }

    public void setBody(byte[] body) {
        this.body = body.clone();
    }

}
