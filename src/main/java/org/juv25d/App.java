package org.juv25d;

import org.juv25d.filter.LoggingFilter;
import org.juv25d.logging.ServerLogging;
import org.juv25d.http.HttpParser;
import org.juv25d.plugin.StaticFilesPlugin;
import org.juv25d.util.ConfigLoader;

import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {
        ConfigLoader config = ConfigLoader.getInstance();
        Logger logger = ServerLogging.getLogger();
        HttpParser httpParser = new HttpParser();

        Pipeline pipeline = new Pipeline();
        pipeline.addFilter(new LoggingFilter());
        pipeline.setPlugin(new StaticFilesPlugin());
        pipeline.init();

        DefaultConnectionHandlerFactory handlerFactory =
            new DefaultConnectionHandlerFactory(httpParser, logger);

        Server server = new Server(
            config.getPort(),
            logger,
            handlerFactory,
            pipeline
        );

        server.start();
    }
}
