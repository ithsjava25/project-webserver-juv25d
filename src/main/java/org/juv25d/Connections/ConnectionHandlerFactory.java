package org.juv25d.Connections;

import org.juv25d.Server.Pipeline;

import java.net.Socket;

public interface ConnectionHandlerFactory {
    Runnable create(Socket socket, Pipeline pipeline);
}
