package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

public interface Plugin {
    void handle (HttpRequest req, HttpResponse res) throws IOException;
}
