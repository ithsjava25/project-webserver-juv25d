package org.juv25d.connections;


import java.net.Socket;

public interface ConnectionHandlerFactory {
    Runnable create(Socket socket);
}
