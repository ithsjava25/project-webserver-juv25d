package org.juv25d;

import org.juv25d.parser.HttpParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    private final HttpParser httpParser;

    public SocketServer(HttpParser httpParser) {
        this.httpParser = httpParser;
    }

    static void createSocket() {
        int port = 3000;

        try (ServerSocket serverSocket = new ServerSocket(port, 64)) {

            System.out.println("Server started at port: " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                Thread.ofVirtual().start(() -> handleClient(socket));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void handleClient(Socket socket) {
        try (socket) {
            InputStream in = socket.getInputStream();

            HttpParser parser = new HttpParser();
            HttpRequest request = parser.parse(in);

            System.out.println("Method: " + request.method());
            System.out.println("Path: " + request.path());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
