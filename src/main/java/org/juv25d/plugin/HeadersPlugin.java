package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HeadersPlugin implements Plugin {


    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        String headers = "";

        for (var headerEntry : req.headers().entrySet()) {
            headers += headerEntry.getKey() + ": " + headerEntry.getValue();
            headers += System.lineSeparator();
        }

        byte[] bodyBytes = headers.getBytes(StandardCharsets.UTF_8);

        res.setStatusCode(200);
        res.setHeader("Content-Type", "application/json");
        res.setHeader("Content-Length", String.valueOf(bodyBytes.length));
        res.setBody(bodyBytes);
    }
}
