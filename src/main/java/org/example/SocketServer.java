package org.example;

import org.example.filter.FilterChain;
import org.example.http.HttpParser;
import org.example.http.HttpRequest;
import org.example.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    static void handleClient(Socket socket, Pipeline pipeline) {
        try (socket) {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            HttpParser parser = new HttpParser();
            HttpRequest req = parser.parse(in);
            HttpResponse res = new HttpResponse();

            // Run filter/plugin
            FilterChain chain = pipeline.createChain();
            chain.doFilter(req, res);

            // Create a toBytes method in HttpResponse?
            out.write(res.toBytes());
            out.flush();

            System.out.println("Method: " + req.method());
            System.out.println("Path: " + req.path());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
