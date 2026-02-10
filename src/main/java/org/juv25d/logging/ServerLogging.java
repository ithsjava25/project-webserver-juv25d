package org.juv25d.logging;

import org.juv25d.SocketServer;
import org.juv25d.parser.HttpParser;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerLogging {
    private static final Logger logger = Logger.getLogger(SocketServer.class.getName());

    public ServerLogging(HttpParser httpParser) {
        // Configure logger for simple output
        logger.setUseParentHandlers(false); // Prevent logging to parent handlers
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        logger.setLevel(Level.INFO); // Set default logging level
    }

    public static Logger getLogger() {
        return logger;
    }
}
