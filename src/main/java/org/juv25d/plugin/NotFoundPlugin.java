package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

public class NotFoundPlugin implements Plugin {
    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        res.setStatusCode(404);
        res.setStatusText("Not Found");
        res.setBody("404 - Resource Not Found".getBytes());
    }
}