package org.juv25d.router;

import org.juv25d.http.HttpRequest;
import org.juv25d.plugin.NotFoundPlugin;
import org.juv25d.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;

/**
 * A simple router implementation that maps request paths to specific Plugin instances.
 * If no specific plugin is registered for a path, it defaults to a NotFoundPlugin.
 */
public class SimpleRouter implements Router {

    private final Map<String, Plugin> routes;
    private final Plugin notFoundPlugin;

    public SimpleRouter() {
        this.routes = new HashMap<>();
        this.notFoundPlugin = new NotFoundPlugin();
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
     * Resolution order:
     * 1. Exact path match
     * 2. Wildcard match (longest prefix wins)
     * 3. NotFoundPlugin
     *
     * @param request The incoming HttpRequest.
     * @return The Plugin instance responsible for handling the request.
     */
    @Override
    public Plugin resolve(HttpRequest request) {
        String path = request.path();

        // 1. Exact match
        Plugin exactMatch = routes.get(path);
        if (exactMatch != null) {
            return exactMatch;
        }

        // 2. Wildcard match (deterministic: longest prefix first)
        return routes.entrySet().stream()
            .filter(entry -> entry.getKey().endsWith("/*"))
            .sorted(Comparator.comparingInt(
                entry -> -entry.getKey().length()
            )) // longest (most specific) first
            .filter(entry -> {
                String prefix = entry.getKey().substring(0, entry.getKey().length() - 1);
                return path.startsWith(prefix);
            })
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(notFoundPlugin);
    }
}
