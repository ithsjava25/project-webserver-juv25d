package org.juv25d;

import java.net.Socket;

public interface ConnectionHandlerFactory {
    Runnable create(Socket socket, Pipeline pipeline);
}
