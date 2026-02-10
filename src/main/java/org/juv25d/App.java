package org.juv25d;

import org.juv25d.filter.LoggingFilter;
import org.juv25d.plugin.HelloPlugin;

public class App {

    public static void main(String[] args) {

        Pipeline pipeline = new Pipeline();
        pipeline.addFilter(new LoggingFilter());
        pipeline.setPlugin(new HelloPlugin());

        pipeline.init();

        SocketServer.createSocket(pipeline);
    }
}
