package org.juv25d.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerLogging {
    private static final Logger logger =
        Logger.getLogger(ServerLogging.class.getName());

    static {
        configure(logger);
    }

    static void configure(Logger logger) {
        logger.setUseParentHandlers(false);

        if (logger.getHandlers().length == 0) {
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
        }

        String levelName = System.getProperty(
            "log.level",
            System.getenv().getOrDefault("LOG_LEVEL", "INFO")
        );

        try {
        Level level = Level.parse(levelName.toUpperCase());
        logger.setLevel(level);
        } catch (IllegalArgumentException e) {
            logger.setLevel(Level.INFO);
            logger.warning("Invalid log level: '" + levelName + "', defaulting to INFO");
        }
    }

    private ServerLogging() {}

    public static Logger getLogger() {
        return logger;
    }
}

