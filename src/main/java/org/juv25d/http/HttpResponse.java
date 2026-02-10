package org.juv25d.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class HttpResponse {

    private final int statusCode;
    private final String statusText;
    private final Map<String, String> headers;
    private final byte[] body;

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

}
