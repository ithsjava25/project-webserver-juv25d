package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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

        var originalOut = System.out;
        var out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        try {
            filter.doFilter(req, res, chain);

            String output = out.toString();
            assert output.contains("GET /test");
        } finally {
            System.setOut(originalOut);
        }
    }
}
