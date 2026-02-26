package org.juv25d.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SecurityHeadersFilterTest {

    private SecurityHeadersFilter filter;
    private HttpRequest mockRequest;
    private FilterChain mockChain;

    @BeforeEach
    void setUp() {
        filter = new SecurityHeadersFilter();
        mockRequest = mock(HttpRequest.class);
        mockChain = mock(FilterChain.class);
    }

    @Test
    void shouldAddSecurityHeadersAfterChainExecution() throws IOException {
        Map<String, String> headers = new HashMap<>();
        HttpResponse response = new HttpResponse(200, "OK", headers, new byte[0]);

        filter.doFilter(mockRequest, response, mockChain);

        verify(mockChain, times(1)).doFilter(mockRequest, response);

        assertEquals("nosniff", response.headers().get("X-Content-Type-Options"));
        assertEquals("DENY", response.headers().get("X-Frame-Options"));
        assertEquals("0", response.headers().get("X-XSS-Protection"));
        assertEquals("no-referrer", response.headers().get("Referrer-Policy"));
    }

    @Test
    void shouldAddHeadersEvenIfChainThrowsException() throws IOException {

        HttpResponse response = new HttpResponse(200, "OK", new HashMap<>(), new byte[0]);

        doThrow(new IOException("Server Error")).when(mockChain).doFilter(any(), any());
        try {
            filter.doFilter(mockRequest, response, mockChain);
        } catch (IOException e) {

        }

        assertEquals("DENY", response.headers().get("X-Frame-Options"));
    }
}
