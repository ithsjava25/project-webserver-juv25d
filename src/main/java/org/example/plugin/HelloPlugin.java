package org.example.plugin;

import org.example.http.HttpRequest;
import org.example.http.HttpResponse;

import java.io.IOException;

public class HelloPlugin implements Plugin{

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {

        // Does not work since Http response has not been initialized (TODO)

        /*
        res.setStatus(200);
        res.setBody("Hello from plugin!");
         */

    }
}
