package org.juv25d.filter;

import org.juv25d.auth.Session;
import org.juv25d.auth.SessionStore;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionAuthFilterTest {

    private SessionAuthFilter filter;

    @BeforeEach
    void setup() {
        filter = new SessionAuthFilter();
        // reset store timeout to default for deterministic Max-Age in other tests if needed
        SessionStore.getInstance().setIdleTimeoutSeconds(30L * 60L);
    }

    @Test
    void unauthenticatedGet_isRedirectedToLogin() throws IOException {
        Map<String, String> headers = new HashMap<>();
        HttpRequest req = new HttpRequest("GET", "/admin", null, "HTTP/1.1", headers, new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertEquals(302, res.statusCode());
        assertEquals("/login", res.getHeader("Location"));
        assertEquals("0", res.getHeader("Content-Length"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void unauthenticatedPost_gets401() throws IOException {
        Map<String, String> headers = new HashMap<>();
        HttpRequest req = new HttpRequest("POST", "/admin", null, "HTTP/1.1", headers, new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertEquals(401, res.statusCode());
        assertEquals("text/plain; charset=UTF-8", res.getHeader("Content-Type"));
        assertArrayEquals("Unauthorized".getBytes(StandardCharsets.UTF_8), res.body());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void authenticatedRequest_passesThrough_withoutIdentityHeaders() throws IOException {
        // Create a session and include it in Cookie
        Session s = SessionStore.getInstance().create("axel");
        String cookie = "SID=" + java.net.URLEncoder.encode(s.getId(), StandardCharsets.UTF_8);

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);
        HttpRequest req = new HttpRequest("GET", "/admin", null, "HTTP/1.1", headers, new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        assertNull(res.getHeader("X-Authenticated"));
        assertNull(res.getHeader("X-User"));
    }
}
