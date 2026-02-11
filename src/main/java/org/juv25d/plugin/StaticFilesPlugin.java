package org.juv25d.plugin;

import org.juv25d.plugin.Plugin;
import org.juv25d.handler.StaticFileHandler;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

/**
 * Plugin that serves static files using StaticFileHandler.
 */
public class StaticFilesPlugin implements Plugin {

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        // Use StaticFileHandler to handle the request
        HttpResponse staticResponse = StaticFileHandler.handle(request);

        // Copy the response from StaticFileHandler to the pipeline response
        response.setStatusCode(staticResponse.statusCode());
        response.setStatusText(staticResponse.statusText());
        response.setHeaders(staticResponse.headers());
        response.setBody(staticResponse.body());
    }
}
