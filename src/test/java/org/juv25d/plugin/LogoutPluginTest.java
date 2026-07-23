package org.juv25d.plugin;

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

class LogoutPluginTest {

    @BeforeEach
    void resetStore() {
        SessionStore.getInstance().setIdleTimeoutSeconds(30L * 60L);
    }

    @Test
    void logout_invalidatesSession_clearsCookie_andRedirectsHome() throws IOException {
        // Arrange: create a valid session and send it as cookie
        Session s = SessionStore.getInstance().create("axel");
        String cookieHeader = "SID=" + java.net.URLEncoder.encode(s.getId(), StandardCharsets.UTF_8);

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookieHeader);
        HttpRequest req = new HttpRequest("POST", "/logout", null, "HTTP/1.1", headers, new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        LogoutPlugin plugin = new LogoutPlugin();

        // Act
        plugin.handle(req, res);

        // Assert response
        assertEquals(302, res.statusCode());
        assertEquals("/", res.getHeader("Location"));
        String setCookie = res.getHeader("Set-Cookie");
        assertNotNull(setCookie);
        assertTrue(setCookie.startsWith("SID="));
        assertTrue(setCookie.contains("Max-Age=0"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("SameSite=Lax"));
        assertTrue(setCookie.contains("Secure"));

        // Assert session invalidated
        assertNull(SessionStore.getInstance().get(s.getId()));
    }
}
