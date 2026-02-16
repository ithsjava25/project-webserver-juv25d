package org.juv25d.router;

import org.juv25d.http.HttpRequest;
import org.juv25d.plugin.NotFoundPlugin;
import org.juv25d.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple router implementation that maps request paths to specific Plugin instances.
 * If no specific plugin is registered for a path, it defaults to a NotFoundPlugin.
 */
public class SimpleRouter implements Router {

    private final Map<String, Plugin> routes;
    private final Plugin notFoundPlugin;

    public SimpleRouter() {
        this.routes = new HashMap<>();
        this.notFoundPlugin = new NotFoundPlugin(); // Default not found handler
    }

    /**
     * Registers a plugin for a specific path.
     *
     * @param path   The path for which the plugin should handle requests.
     * @param plugin The plugin to handle requests for the given path.
     */
    public void registerPlugin(String path, Plugin plugin) {
        routes.put(path, plugin);
    }

    /**
     * Resolves the given HttpRequest to a Plugin that can handle it.
     * It attempts to find an exact match for the request path. If no exact match is found,
     * it checks for a wildcard path (e.g., "/*"). If still no match, it returns the notFoundPlugin.
     *
     * @param request The incoming HttpRequest.
     * @return The Plugin instance responsible for handling the request. Never returns null.
     */
    @Override
    public Plugin resolve(HttpRequest request) {
        String path = request.path();

        // Try to find an exact match for the path
        if (routes.containsKey(path)) {
            return routes.get(path);
        }

        // Check for wildcard path if no exact match (e.g., "/*" for a catch-all)
        // This is a simple implementation and can be extended for more complex wildcard matching
        for (Map.Entry<String, Plugin> entry : routes.entrySet()) {
            String registeredPath = entry.getKey();
            if (registeredPath.endsWith("/*") && path.startsWith(registeredPath.substring(0, registeredPath.length() - 1))) {
                return entry.getValue();
            }
        }

        // If nothing matches, return the NotFoundPlugin
        return notFoundPlugin;
    }
}
