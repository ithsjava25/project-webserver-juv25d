package org.example;

import org.example.filter.LoggingFilter;
import org.example.plugin.HelloPlugin;

public class App {
    public static void main(String[] args) {
        SocketServer.createSocket();

        Pipeline pipeline = new Pipeline();

        pipeline.addFilter(new LoggingFilter());
        pipeline.setPlugin(new HelloPlugin());

        // startServer(pipeline);
    }
}
