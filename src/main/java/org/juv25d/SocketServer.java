package org.juv25d;

import org.juv25d.filter.FilterChainImpl;
import org.juv25d.http.HttpParser;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Temporary server implementation.
 *
 * TODO (#20):
 * Refactor into Server + ConnectionHandler.
 */
public class SocketServer {

    static void createSocket(Pipeline pipeline) {
        int port = 3000;

        try (ServerSocket serverSocket = new ServerSocket(port, 64)) {
            System.out.println("Server started at port: " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                Thread.ofVirtual().start(() -> handleClient(socket, pipeline));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void handleClient(Socket socket, Pipeline pipeline) {
        try (socket) {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            HttpRequest req = new HttpParser().parse(in);
            HttpResponse res = new HttpResponse(
                200,
                "OK",
                java.util.Map.of(),
                new byte[0]
            );

            FilterChainImpl chain = pipeline.createChain();
            chain.doFilter(req, res);

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
