package org.juv25d.connections;

import org.juv25d.Server.Pipeline;

import java.net.Socket;

public interface ConnectionHandlerFactory {
    Runnable create(Socket socket, Pipeline pipeline);
}
