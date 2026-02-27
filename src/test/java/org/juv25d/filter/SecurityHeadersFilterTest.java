package org.juv25d.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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

        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertEquals("0", response.getHeader("X-XSS-Protection"));
        assertEquals("no-referrer", response.getHeader("Referrer-Policy"));
    }

    @Test
    void shouldAddHeadersEvenIfChainThrowsException() throws IOException {

        HttpResponse response = new HttpResponse(200, "OK", new HashMap<>(), new byte[0]);
        doThrow(new IOException("Server Error")).when(mockChain).doFilter(any(), any());

        assertThrows(IOException.class, () -> filter.doFilter(mockRequest, response, mockChain));

        assertAll("Verify all security headers are present even after exception",
            () -> assertEquals("nosniff", response.getHeader("X-Content-Type-Options")),
            () -> assertEquals("DENY", response.getHeader("X-Frame-Options")),
            () -> assertEquals("0", response.getHeader("X-XSS-Protection")),
            () -> assertEquals("no-referrer", response.getHeader("Referrer-Policy"))

        );

    }

    @Test
    void shouldNotOverwriteExistingHeaders() throws IOException {
        // 1. Skapa en response där en header redan är satt
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Frame-Options", "SAMEORIGIN"); // En annan policy än filtrets "DENY"
        HttpResponse response = new HttpResponse(200, "OK", headers, new byte[0]);

        // 2. Kör filtret
        filter.doFilter(mockRequest, response, mockChain);

        // 3. Verifiera beteendet (här antar vi att vi vill behålla "SAMEORIGIN")
        assertEquals("SAMEORIGIN", response.getHeader("X-Frame-Options"),
            "Filtret ska inte skriva över en redan satt header");

        // Verifiera att de andra headers som saknades fortfarande läggs till
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    }
}
