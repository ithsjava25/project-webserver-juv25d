package org.juv25d;

import org.juv25d.http.HttpParser;

import java.net.Socket;
import java.util.logging.Logger;

public class DefaultConnectionHandlerFactory implements ConnectionHandlerFactory{
    private final HttpParser httpParser;
    private final Logger logger;
    private final Pipeline pipeline;

    public DefaultConnectionHandlerFactory(HttpParser httpParser, Logger logger, Pipeline pipeline) {
        this.httpParser = httpParser;
        this.logger = logger;
        this.pipeline = pipeline;
    }

    @Override
    public Runnable create(Socket socket) {
        return new ConnectionHandler(socket, httpParser, logger, this.pipeline);
    }
}
