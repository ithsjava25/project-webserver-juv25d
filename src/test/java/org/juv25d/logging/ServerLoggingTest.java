package org.juv25d.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ServerLoggingTest {

    private Logger logger;

    //Arrange
    @BeforeEach
    void setUp() {
        logger = ServerLogging.getLogger();

        // Clean handlers
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
    }

    @Test
    @DisplayName("Logger should return same instance")
    void getLogger_shouldReturnSameInstance() {
        Logger logger1 = ServerLogging.getLogger();
        Logger logger2 = ServerLogging.getLogger();

        assertSame(logger1, logger2);
    }

    @Test
    @DisplayName("Logger should have console handler")
    void logger_shouldHaveConsoleHandler() {
        Logger logger = ServerLogging.getLogger();

        // Reset logger
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        // Explicit configure
        ServerLogging.configure(logger);

        boolean hasConsoleHandler = false;
        for (Handler handler : logger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                hasConsoleHandler = true;
                break;
            }
        }

        assertTrue(hasConsoleHandler, "Logger should have a Console Handler");
    }

    @Test
    @DisplayName("Logger should not add duplicate handlers")
    void logger_shouldNotAddDuplicateHandlers() {
        Logger logger = ServerLogging.getLogger();

        // Clean slate
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        ServerLogging.configure(logger);
        int handlerCountAfterFirst = logger.getHandlers().length;

        ServerLogging.configure(logger);
        int handlerCountAfterSecond = logger.getHandlers().length;

        assertEquals(1, handlerCountAfterFirst);
        assertEquals(handlerCountAfterFirst, handlerCountAfterSecond,
            "configure() should not add duplicate handlers");
    }

    @Test
    @DisplayName("Logger should have INFO level by default")
    void logger_shouldHaveInfoLevelByDefault() {
        Logger logger = ServerLogging.getLogger();

        assertEquals(Level.INFO, logger.getLevel());
    }

    @Test
    @DisplayName("Logger should not use parent handlers")
    void logger_shouldNotUseParentHandlers() {
        Logger logger = ServerLogging.getLogger();

        assertFalse(logger.getUseParentHandlers());
    }

    @Test
    @DisplayName("Logger should use log level from system property")
    void logger_shouldUseLogLevelFromSystemProperty() {
        String original = System.getProperty("log.level");

        try {
            System.setProperty("log.level", "WARNING");

            Logger testLogger = Logger.getLogger("test.logger");
            ServerLogging.configure(testLogger);

            assertEquals(Level.WARNING, testLogger.getLevel());

        } finally {
            // Reset system state
            if (original == null) {
                System.clearProperty("log.level");
            } else {
                System.setProperty("log.level", original);
            }
        }
    }

}
