package org.juv25d;

import org.juv25d.parser.HttpParser;
import org.juv25d.logging.ServerLogging;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketServer {

    private final HttpParser httpParser;

    public SocketServer(HttpParser httpParser) {
        this.httpParser = httpParser;
    }

    private static final Logger logger = ServerLogging.getLogger();

    static void createSocket() {
        int port = 3000;

        try (ServerSocket serverSocket = new ServerSocket(port, 64)) {

            logger.info("Server started at port: " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Client connected from: " + socket.getInetAddress().getHostAddress());

                Thread.ofVirtual().start(() -> handleClient(socket));
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server socket error: " + e);
        }
    }

    static void handleClient(Socket socket) {
        try (socket) {
            InputStream in = socket.getInputStream();

            HttpParser parser = new HttpParser();
            HttpRequest request = parser.parse(in);

            logger.info("Method: " + request.method());
            logger.info("Path: " + request.path());

        } catch (IOException e) {
            logger.log(Level.WARNING, "Error handling client ",e);
        }
    }
}
