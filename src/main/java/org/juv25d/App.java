package org.juv25d;

import org.juv25d.Server.Server;
import org.juv25d.Server.ServerBuilder;
import org.juv25d.filter.IpFilter;
import org.juv25d.filter.LoggingFilter;
import org.juv25d.filter.RateLimitingFilter;
import org.juv25d.filter.RedirectFilter;
import org.juv25d.filter.RedirectRule;
import org.juv25d.logging.ServerLogging;
import org.juv25d.plugin.NotFoundPlugin;
import org.juv25d.plugin.StaticFilesPlugin;
import org.juv25d.router.SimpleRouter;
import org.juv25d.util.ConfigLoader;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class App {

    public static void main(String[] args) {

        ConfigLoader config = ConfigLoader.getInstance();
        Logger logger = ServerLogging.getLogger();

        List<RedirectRule> redirectRules = List.of(
            new RedirectRule("/old-page", "/new-page", 301),
            new RedirectRule("/temp", "https://example.com/temporary", 302),
            new RedirectRule("/docs/*", "/documentation/", 301)
        );

        SimpleRouter router = new SimpleRouter();
        router.registerPlugin("/", new StaticFilesPlugin());
        router.registerPlugin("/*", new StaticFilesPlugin());
        router.registerPlugin("/notfound", new NotFoundPlugin());

        Server server = new ServerBuilder()
            .port(config.getPort())
            .logger(logger)
            .router(router)
            .addFilter(new RedirectFilter(redirectRules))
            .addFilter(new IpFilter(Set.of(), Set.of()))
            .addFilter(new LoggingFilter())
            .addFilterIf(
                config.isRateLimitingEnabled(),
                () -> new RateLimitingFilter(
                    config.getRequestsPerMinute(),
                    config.getBurstCapacity()
                )
            )
            .build();

        server.start();
    }
}
