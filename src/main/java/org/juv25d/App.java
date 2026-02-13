package org.juv25d;

import org.juv25d.filter.IpFilter;
import org.juv25d.filter.LoggingFilter;
import org.juv25d.logging.ServerLogging;
import org.juv25d.http.HttpParser;
import org.juv25d.plugin.StaticFilesPlugin;
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
        pipeline.addFilter(new IpFilter(
            Set.of(),
            Set.of()
        ));
        pipeline.addFilter(new LoggingFilter());
        pipeline.addGlobalFilter(new LoggingFilter(), 0);
        pipeline.setPlugin(new StaticFilesPlugin());

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
