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
        String connectionId = java.util.UUID.randomUUID().toString().substring(0, 8);
        org.juv25d.logging.LogContext.setConnectionId(connectionId);
        try (socket) {
            var in = socket.getInputStream();
            var out = socket.getOutputStream();

            HttpRequest parsed = httpParser.parse(in);
            String remoteIp = socket.getInetAddress().getHostAddress();

            HttpRequest request = new HttpRequest(
                parsed.method(),
                parsed.path(),
                parsed.queryString(),
                parsed.httpVersion(),
                parsed.headers(),
                parsed.body(),
                remoteIp
            );

            HttpResponse response = new HttpResponse(
                200,
                "OK",
                java.util.Map.of(),
                new byte[0]
            );

            var chain = pipeline.createChain(request);
            chain.doFilter(request, response);

            HttpResponseWriter.write(out, response);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while handling request", e);
        } finally {
            org.juv25d.logging.LogContext.clear();
        }
    }
}
