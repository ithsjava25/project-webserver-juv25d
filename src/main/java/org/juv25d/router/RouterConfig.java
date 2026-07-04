package org.juv25d.router;

import org.juv25d.di.Inject;
import org.juv25d.plugin.HealthCheckPlugin;
import org.juv25d.plugin.MetricPlugin;
import org.juv25d.plugin.NotFoundPlugin;
import org.juv25d.plugin.StaticFilesPlugin;
import org.juv25d.plugin.LoginPlugin;
import org.juv25d.proxy.ProxyPlugin;
import org.juv25d.proxy.ProxyRoute;
import org.juv25d.util.ConfigLoader;
import org.juv25d.plugin.SlowPlugin;
import org.juv25d.plugin.LogoutPlugin;
import org.juv25d.plugin.AuthStatusPlugin;
public class RouterConfig {

    @Inject
    public RouterConfig(SimpleRouter router) {

        for (ProxyRoute proxyRoute : ConfigLoader.getInstance().getProxyRoutes()) {
            router.registerPlugin(proxyRoute.getBaseRoute(), new ProxyPlugin(proxyRoute));
            router.registerPlugin(proxyRoute.getBaseRoute() + "/*", new ProxyPlugin(proxyRoute));
        }

        router.registerPlugin("/metric", new MetricPlugin());
        router.registerPlugin("/health", new HealthCheckPlugin());
        router.registerPlugin("/slow", new SlowPlugin());
        // Login endpoint: Enforce BasicAuth, then show a small page and redirect to "/" after 1s
        router.registerPlugin("/login", new LoginPlugin());
        // Logout endpoint: Forces browser to drop Basic Auth creds via 401 + new realm
        router.registerPlugin("/logout", new LogoutPlugin());
        // Auth status endpoint: report auth state without 401 to avoid browser prompt on homepage
        router.registerPlugin("/auth/status", new AuthStatusPlugin());
        router.registerPlugin("/", new StaticFilesPlugin());
        router.registerPlugin("/*", new StaticFilesPlugin());
        router.registerPlugin("/notfound", new NotFoundPlugin());

        System.out.println("Router configured");
    }
}
