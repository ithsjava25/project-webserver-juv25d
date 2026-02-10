package org.juv25d;

import org.juv25d.parser.HttpParser;
import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final HttpParser httpParser;


    public ConnectionHandler(Socket socket, HttpParser httpParser) {
        this.socket = socket;
        this.httpParser = httpParser;
    }

    @Override
    public void run() {
        try (socket) {
            HttpRequest request = httpParser.parse(socket.getInputStream());

            System.out.println("Method: " + request.method());
            System.out.println("Path: " + request.path());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
