package org.juv25d;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Server {
    private static final int PORT = 3000;
    private final Logger logger;
    private final ConnectionHandlerFactory handlerFactory;

    public Server(Logger logger, ConnectionHandlerFactory handlerFactory) {
        this.logger = logger;
        this.handlerFactory = handlerFactory;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT, 64)) {
            logger.info("Server started at port: " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                Runnable handler = handlerFactory.create(socket);
                Thread.ofVirtual().start(handler);
            }

        } catch (IOException e) {
            throw new RuntimeException("Server error", e);
        }
    }
}
