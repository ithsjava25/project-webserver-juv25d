package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

public class SlowPlugin implements Plugin {

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            return;
        }

        res.setStatusCode(200);
        res.setStatusText("OK");
        res.setBody("Slow response finished".getBytes());
    }
}
