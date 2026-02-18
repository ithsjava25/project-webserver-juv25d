package org.juv25d.Server;

import org.juv25d.ConnectionHandlerFactory;
import org.juv25d.Pipeline;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Server {
    private final int port;
    private final Logger logger;
    private final ConnectionHandlerFactory handlerFactory;
    private final Pipeline pipeline;
    private final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

    public Server(int port, Logger logger, ConnectionHandlerFactory handlerFactory, Pipeline pipeline) {
        this.port = port;
        this.logger = logger;
        this.handlerFactory = handlerFactory;
        this.pipeline = pipeline;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        logger.info("Shutting down server...");
        pipeline.destroyFilters();
        }));
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(port, 64)) {

            logger.info("Server started at port: " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                Runnable handler = handlerFactory.create(socket, pipeline);
                Thread.ofVirtual().start(handler);
            }

        } catch (IOException e) {
            throw new RuntimeException("Server error", e);
        }
    }
}
