package org.juv25d.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerLogging {
    private static final Logger logger = Logger.getLogger(ServerLogging.class.getName());

    static {
        // Configure logger for simple output
        logger.setUseParentHandlers(false); // Prevent logging to parent handlers

        if (logger.getHandlers().length == 0) {
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
          }

        String levelName = System.getProperty(
            "log.level",
            System.getenv().getOrDefault("LOG_LEVEL", "INFO")
            );

        Level level = Level.parse(levelName.toUpperCase());

        logger.setLevel(level); // Set default logging level
    }

    private ServerLogging() {
        // Utility class - prevent instantiation
    }

    public static Logger getLogger() {
        return logger;
    }
}
