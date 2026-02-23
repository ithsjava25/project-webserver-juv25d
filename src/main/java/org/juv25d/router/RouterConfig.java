package org.juv25d.router;

import org.juv25d.di.Inject;
import org.juv25d.plugin.HealthCheckPlugin;
import org.juv25d.plugin.MetricPlugin;
import org.juv25d.plugin.NotFoundPlugin;
import org.juv25d.plugin.StaticFilesPlugin;
import org.juv25d.proxy.ProxyPlugin;
import org.juv25d.proxy.ProxyRoute;
import org.juv25d.util.ConfigLoader;

public class RouterConfig {

    @Inject
    public RouterConfig(SimpleRouter router) {

        for (ProxyRoute proxyRoute : ConfigLoader.getInstance().getProxyRoutes()) {
            router.registerPlugin(proxyRoute.getBaseRoute(), new ProxyPlugin(proxyRoute));
            router.registerPlugin(proxyRoute.getBaseRoute() + "/*", new ProxyPlugin(proxyRoute));
        }

        router.registerPlugin("/metric", new MetricPlugin());
        router.registerPlugin("/health", new HealthCheckPlugin());
        router.registerPlugin("/", new StaticFilesPlugin());
        router.registerPlugin("/*", new StaticFilesPlugin());
        router.registerPlugin("/notfound", new NotFoundPlugin());

        System.out.println("Router configured");
    }
}
