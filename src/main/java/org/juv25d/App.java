package org.juv25d;

import org.juv25d.filter.LoggingFilter;
import org.juv25d.logging.ServerLogging;
import org.juv25d.http.HttpParser;
import org.juv25d.plugin.HelloPlugin;

import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {
        Logger logger = ServerLogging.getLogger();
        HttpParser httpParser = new HttpParser();

        Pipeline pipeline = new Pipeline();
        pipeline.addFilter(new LoggingFilter());
        pipeline.setPlugin(new HelloPlugin());
        pipeline.init();

        DefaultConnectionHandlerFactory handlerFactory =
            new DefaultConnectionHandlerFactory(httpParser, logger);

        Server server = new Server(
            logger,
            handlerFactory,
            pipeline
        );

        server.start();
    }
}
