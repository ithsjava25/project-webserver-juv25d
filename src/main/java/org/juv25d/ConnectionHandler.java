package org.juv25d;

import org.juv25d.parser.HttpParser;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final HttpParser httpParser;

    private static Logger logger = Logger.getLogger(ConnectionHandler.class.getName());

    public ConnectionHandler(Socket socket, HttpParser httpParser) {
        this.socket = socket;
        this.httpParser = httpParser;
    }

    @Override
    public void run() {
        try (socket) {
            HttpRequest request = httpParser.parse(socket.getInputStream());

            logger.info("Handling reuest: " + request.method() + " " + request.path());

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while handling request", e);
        }
    }
}
