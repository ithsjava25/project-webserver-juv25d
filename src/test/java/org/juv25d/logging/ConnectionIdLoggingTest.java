package org.juv25d.logging;

import org.junit.jupiter.api.Test;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionIdLoggingTest {

    @Test
    void logMessageShouldIncludeConnectionId() {
        // Arrange
        Logger logger = Logger.getLogger("test.connectionid");
        logger.setUseParentHandlers(false);

        List<String> formattedMessages = new ArrayList<>();
        ServerLogFormatter formatter = new ServerLogFormatter();

        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                formattedMessages.add(formatter.format(record));
            }
            @Override
            public void flush() {}
            @Override
            public void close() throws SecurityException {}
        };
        logger.addHandler(handler);

        try {
            String testId = "test-123";
            LogContext.setConnectionId(testId);

            // Act
            logger.info("This is a test message");

            // Assert
            assertTrue(formattedMessages.get(0).contains("[" + testId + "]"),
                "Log message should contain the connection ID. Found: " + formattedMessages.get(0));
        } finally {
            LogContext.clear();
        }
    }
}
