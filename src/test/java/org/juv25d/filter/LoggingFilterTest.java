package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LoggingFilterTest {

    @Test
    void callsNextFilterInChain() throws IOException {

        LoggingFilter filter = new LoggingFilter();
        HttpRequest req = mock(HttpRequest.class);
        HttpResponse res = mock(HttpResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    @Test
    void logsHttpMethodAndPath() throws IOException {
        LoggingFilter filter = new LoggingFilter();
        HttpRequest req = mock(HttpRequest.class);
        HttpResponse res = mock(HttpResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.method()).thenReturn("GET");
        when(req.path()).thenReturn("/test");

        java.util.logging.Logger logger = org.juv25d.logging.ServerLogging.getLogger();
        java.util.List<java.util.logging.LogRecord> records = new java.util.ArrayList<>();
        java.util.logging.Handler handler = new java.util.logging.Handler() {
            @Override
            public void publish(java.util.logging.LogRecord record) {
                records.add(record);
            }
            @Override
            public void flush() {}
            @Override
            public void close() throws SecurityException {}
        };
        logger.addHandler(handler);

        try {
            filter.doFilter(req, res, chain);

            boolean found = records.stream()
                .anyMatch(r -> r.getMessage().contains("GET /test"));
            assertTrue(found, "Logger should have captured the method and path");
        } finally {
            logger.removeHandler(handler);
        }
    }
}