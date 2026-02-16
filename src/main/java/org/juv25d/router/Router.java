package org.juv25d.router;

import org.juv25d.http.HttpRequest;
import org.juv25d.plugin.Plugin;

/**
 * The Router interface defines a contract for components that resolve an incoming HTTP request
 * to a specific Plugin instance responsible for handling that request.
 */
public interface Router {

    /**
     * Resolves the given HttpRequest to a Plugin that can handle it.
     *
     * @param request The incoming HttpRequest.
     * @return The Plugin instance responsible for handling the request. Must not be null.
     */
    Plugin resolve(HttpRequest request);
}
