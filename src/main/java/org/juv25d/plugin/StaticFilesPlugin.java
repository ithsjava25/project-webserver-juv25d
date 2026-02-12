package org.juv25d.plugin;

import org.juv25d.handler.StaticFileHandler;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

/**
 * Plugin that serves static files using StaticFileHandler.
 *
 * NOTE: Currently not in use because it requires HttpResponse to have setters,
 * but HttpResponse is being modified by other team members in parallel branches.
 * To avoid merge conflicts, we use direct integration in ConnectionHandler instead.
 *
 * This plugin can be activated later once HttpResponse refactoring is complete.
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
