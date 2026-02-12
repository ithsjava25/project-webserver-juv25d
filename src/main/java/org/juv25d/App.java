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
