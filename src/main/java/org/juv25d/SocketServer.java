package org.juv25d;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

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
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
