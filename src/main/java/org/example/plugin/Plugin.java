package org.example.plugin;

import org.example.http.HttpRequest;
import org.example.http.HttpResponse;

import java.io.IOException;

public interface Plugin {
    void handle (HttpRequest req, HttpResponse res) throws IOException;
}
