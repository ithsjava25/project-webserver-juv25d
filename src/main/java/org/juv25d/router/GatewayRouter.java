package org.juv25d.router;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.plugin.BasicAuthPlugin;
import org.juv25d.plugin.Plugin;

import java.io.IOException;

/**
 * Router wrapper that enforces global Basic Authentication as a Plugin
 * before delegating to the inner Router's resolved plugin.
 */
public class GatewayRouter implements Router {

    private final Router inner;
    private final BasicAuthPlugin authPlugin;

    public GatewayRouter(Router inner, BasicAuthPlugin authPlugin) {
        this.inner = inner;
        this.authPlugin = authPlugin;
    }

    @Override
    public Plugin resolve(HttpRequest request) {
        Plugin delegate = inner.resolve(request);

        // Return a composite plugin that runs auth first, then delegates if authorized
        return new Plugin() {
            @Override
            public void handle(HttpRequest req, HttpResponse res) throws IOException {
                // Run global auth plugin
                authPlugin.handle(req, res);

                // If auth plugin set an error (e.g., 401), stop here
                if (res.statusCode() != 200) {
                    return;
                }

                // Authorized (or auth disabled) -> proceed to actual handler
                delegate.handle(req, res);
            }
        };
    }
}
