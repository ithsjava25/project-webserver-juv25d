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
        this.headers = new LinkedHashMap<>(headers);
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

    public void setHeader(String name, String value) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        this.headers.put(name, value);
    }

    public void setStatus(int statusCode, String statusText) {
        Objects.requireNonNull(statusText, "statusText must not be null");
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public void setBody(byte[] body) {
        Objects.requireNonNull(body, "body must not be null");
        this.body = body.clone();
    }
}
