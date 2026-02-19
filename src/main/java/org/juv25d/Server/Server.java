package org.juv25d.Server;

import org.juv25d.connections.ConnectionHandlerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * HTTP Server runtime component.
 *
 * Lifecycle:
 * <ul>
 *     <li>{@link #start()} initializes filters and starts accepting connections.</li>
 *     <li>{@link #stop()} gracefully shuts down the server and destroys filters.</li>
 * </ul>
 *
 * The server runs its accept loop on a virtual thread and
 * supports graceful shutdown.
 */

public class Server {

    private final int port;
    private final Logger logger;
    private final ConnectionHandlerFactory handlerFactory;
    private final Pipeline pipeline;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket serverSocket;
    private Thread acceptThread;

    public Server(int port,
                  Logger logger,
                  ConnectionHandlerFactory handlerFactory,
                  Pipeline pipeline) {

        this.port = port;
        this.logger = logger;
        this.handlerFactory = handlerFactory;
        this.pipeline = pipeline;

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    /**
     * Starts the server.
     *
     * Initializes filters and begins accepting connections
     * on a background virtual thread.
     */
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        pipeline.initFilters();

        acceptThread = Thread.ofVirtual().start(() -> {
            try (ServerSocket socket = new ServerSocket(port, 64)) {
                this.serverSocket = socket;

                logger.info("Server started on port: " + socket.getLocalPort());

                while (running.get()) {
                    try {
                        Socket client = socket.accept();
                        Runnable handler = handlerFactory.create(client);
                        Thread.ofVirtual().start(handler);
                    } catch (IOException e) {
                        if (running.get()) {
                            logger.severe("Error accepting connection: " + e.getMessage());
                        }
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException("Server startup failed", e);
            }
        });
    }

    /**
     * Stops the server gracefully.
     *
     * Closes the server socket and destroys all filters.
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        logger.info("Shutting down server...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.warning("Error closing server socket: " + e.getMessage());
        }

        pipeline.destroyFilters();
    }
}
