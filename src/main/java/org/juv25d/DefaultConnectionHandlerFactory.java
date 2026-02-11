package org.juv25d;

import org.juv25d.http.HttpParser;

import java.net.Socket;
import java.util.logging.Logger;

public class DefaultConnectionHandlerFactory implements ConnectionHandlerFactory{
    private final HttpParser httpParser;
    private final Logger logger;

    public DefaultConnectionHandlerFactory(HttpParser httpParser, Logger logger) {
        this.httpParser = httpParser;
        this.logger = logger;
    }

    @Override
    public Runnable create(Socket socket, Pipeline pipeline) {
        return new ConnectionHandler(socket, httpParser, logger, pipeline);
    }
}
