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
import org.juv25d.plugin.SessionStatusPlugin;
public class RouterConfig {

    private final SimpleRouter router;

    @Inject
    public RouterConfig(SimpleRouter router) {
        this.router = router;
    }

    public void configure() {
        for (ProxyRoute proxyRoute : ConfigLoader.getInstance().getProxyRoutes()) {
            router.registerPlugin(proxyRoute.getBaseRoute(), new ProxyPlugin(proxyRoute));
            router.registerPlugin(proxyRoute.getBaseRoute() + "/*", new ProxyPlugin(proxyRoute));
        }

        router.registerPlugin("/metric", new MetricPlugin());
        router.registerPlugin("/health", new HealthCheckPlugin());
        router.registerPlugin("/slow", new SlowPlugin());
        // Login endpoint: Form-baserad inloggning som skapar serversession
        router.registerPlugin("/login", new LoginPlugin());
        // Logout endpoint: Ogiltigförklarar serversession och rensar cookie
        router.registerPlugin("/logout", new LogoutPlugin());
        // Sessionsstatus (klient kan kolla om inloggad utan 401/redirect)
        router.registerPlugin("/session/status", new SessionStatusPlugin());
        router.registerPlugin("/", new StaticFilesPlugin());
        router.registerPlugin("/*", new StaticFilesPlugin());
        router.registerPlugin("/notfound", new NotFoundPlugin());

        System.out.println("Router configured");
    }
}
