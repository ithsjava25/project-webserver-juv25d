package org.juv25d;

import org.juv25d.parser.HttpParser;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {


    private final HttpParser httpParser;

    public Server(HttpParser httpParser) {
        this.httpParser = httpParser;
    }


    public void start() {
        int port = 3000;


        try (ServerSocket serverSocket = new ServerSocket(port, 64)) {
            System.out.println("Server started at port: " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();

                ConnectionHandler handler = new ConnectionHandler(socket, this.httpParser);
                Thread.ofVirtual().start(handler);
            }

        } catch (IOException e) {
            throw new RuntimeException("Server error", e);
        }
    }
}
