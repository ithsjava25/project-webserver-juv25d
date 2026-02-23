package org.juv25d;

import org.juv25d.filter.*;
import org.juv25d.logging.ServerLogging;
import org.juv25d.http.HttpParser;
import org.juv25d.plugin.HealthCheckPlugin;
import org.juv25d.plugin.MetricPlugin;
import org.juv25d.plugin.NotFoundPlugin; // New import
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
        HttpParser httpParser = new HttpParser();
        Pipeline pipeline = new Pipeline();

        pipeline.addGlobalFilter(new SecurityHeadersFilter(), 0);

        pipeline.addGlobalFilter(new LoggingFilter(), 1);

        pipeline.addGlobalFilter(new IpFilter(Set.of(), Set.of(), true), 2);

        if (config.isRateLimitingEnabled()) {pipeline.addGlobalFilter(new RateLimitingFilter(
            config.getRequestsPerMinute(), config.getBurstCapacity()), 3);}

        List<RedirectRule> redirectRules = List.of(
            new RedirectRule("/old-page", "/new-page", 301),
            new RedirectRule("/temp", "https://example.com/temporary", 302),
            new RedirectRule("/docs/*", "/documentation/", 301)
        );

        pipeline.addGlobalFilter(new RedirectFilter(redirectRules), 4);


        SimpleRouter router = new SimpleRouter();
        router.registerPlugin("/metric", new MetricPlugin()); //Register MetricPlugin for a specified path
        router.registerPlugin("/health", new HealthCheckPlugin()); //Register HealthCheckPlugin for a specified path
        router.registerPlugin("/", new StaticFilesPlugin()); // Register StaticFilesPlugin for the root path
        router.registerPlugin("/*", new StaticFilesPlugin()); // Register StaticFilesPlugin for all paths
        router.registerPlugin("/notfound", new NotFoundPlugin()); // Example: Register NotFoundPlugin for a specific path

        pipeline.setRouter(router);

        DefaultConnectionHandlerFactory handlerFactory =
            new DefaultConnectionHandlerFactory(httpParser, logger, pipeline);

        Server server = new Server(
            config.getPort(),
            logger,
            handlerFactory,
            pipeline
        );
        server.start();
    }
}
