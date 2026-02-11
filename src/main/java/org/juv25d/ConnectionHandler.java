package org.juv25d;

import org.juv25d.http.HttpParser;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.http.HttpResponseWriter;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final HttpParser httpParser;
    private final Logger logger;
    private final Pipeline pipeline;

    public ConnectionHandler(Socket socket, HttpParser httpParser, Logger logger, Pipeline pipeline) {
        this.socket = socket;
        this.httpParser = httpParser;
        this.logger = logger;
        this.pipeline = pipeline;
    }

    @Override
    public void run() {
        try (socket) {
            var in = socket.getInputStream();
            var out = socket.getOutputStream();

            HttpRequest request = httpParser.parse(in);
            logger.info("Handling request: " + request.method() + " " + request.path());

            HttpResponse response = new HttpResponse(
                200,
                "OK",
                java.util.Map.of(),
                new byte[0]
            );

            var chain = pipeline.createChain();
            chain.doFilter(request, response);

            HttpResponseWriter.write(out, response);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while handling request", e);
        }
    }
}
