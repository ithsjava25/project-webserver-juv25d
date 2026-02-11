package org.juv25d;

import org.juv25d.logging.ServerLogging;
import org.juv25d.parser.HttpParser;

import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {
        Logger logger = ServerLogging.getLogger();
        HttpParser httpParser = new HttpParser();
        DefaultConnectionHandlerFactory handlerFactory =
            new DefaultConnectionHandlerFactory(httpParser, logger);

        Server server = new Server(
            logger,
            handlerFactory
        );

        server.start();
    }
}
