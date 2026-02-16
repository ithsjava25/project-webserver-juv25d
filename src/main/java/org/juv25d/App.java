package org.juv25d;

import org.juv25d.filter.IpFilter;
import org.juv25d.filter.LoggingFilter;
import org.juv25d.logging.ServerLogging;
import org.juv25d.http.HttpParser;
import org.juv25d.plugin.NotFoundPlugin; // New import
import org.juv25d.plugin.StaticFilesPlugin;
import org.juv25d.router.SimpleRouter; // New import
import org.juv25d.util.ConfigLoader;

import java.util.Set;
import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {
        ConfigLoader config = ConfigLoader.getInstance();
        Logger logger = ServerLogging.getLogger();
        HttpParser httpParser = new HttpParser();

        Pipeline pipeline = new Pipeline();

        // IP filter is enabled but configured with open access during development
        // White/blacklist can be tightened when specific IP restrictions are decided
        pipeline.addGlobalFilter(new IpFilter(
            Set.of(),
            Set.of()
        ), 0);
        pipeline.addGlobalFilter(new LoggingFilter(), 0);

        // Initialize and configure SimpleRouter
        SimpleRouter router = new SimpleRouter();
        router.registerPlugin("/", new StaticFilesPlugin()); // Register StaticFilesPlugin for the root path
        router.registerPlugin("/*", new StaticFilesPlugin()); // Register StaticFilesPlugin for all paths
        router.registerPlugin("/notfound", new NotFoundPlugin()); // Example: Register NotFoundPlugin for a specific path

        pipeline.setRouter(router); // Set the router in the pipeline

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
