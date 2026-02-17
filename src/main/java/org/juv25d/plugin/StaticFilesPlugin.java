package org.juv25d.plugin;

import org.juv25d.handler.StaticFileHandler;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Plugin that serves static files using StaticFileHandler.
 * Integrates with the Pipeline architecture to handle GET requests for static resources.
 *
 */
public class StaticFilesPlugin implements Plugin {

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        // Use StaticFileHandler to handle the request
        HttpResponse staticResponse = StaticFileHandler.handle(request);

        // Copy the response from StaticFileHandler to the pipeline response
        response.setStatusCode(staticResponse.statusCode());
        response.setStatusText(staticResponse.statusText());

        for (Map.Entry<String, String> header : staticResponse.headers().entrySet()) {
            response.setHeader(header.getKey(), header.getValue());
        }
        response.setBody(staticResponse.body());
    }
}

