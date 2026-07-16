package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoginPluginTest {

    private @org.jspecify.annotations.Nullable Path tempUsersFile;

    @BeforeEach
    void setup() throws IOException {
        System.clearProperty("users.file");
        System.clearProperty("users.dir");
        System.clearProperty("users.filename");
        // skapa standard Users-fil för de flesta tester
        tempUsersFile = Files.createTempFile("users", ".txt");
        Files.writeString(tempUsersFile, "axel:axem\n", StandardCharsets.UTF_8);
        System.setProperty("users.file", tempUsersFile.toString());
    }

    @AfterEach
    void cleanup() throws IOException {
        System.clearProperty("users.file");
        System.clearProperty("users.dir");
        System.clearProperty("users.filename");
        if (tempUsersFile != null) {
            Files.deleteIfExists(tempUsersFile);
            tempUsersFile = null;
        }
    }

    @Test
    void getLogin_returnsFormHtml() throws IOException {
        LoginPlugin plugin = new LoginPlugin();
        HttpRequest req = new HttpRequest("GET", "/login", null, "HTTP/1.1", Map.of(), new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(200, res.statusCode());
        assertEquals("text/html; charset=UTF-8", res.getHeader("Content-Type"));
        String setCookie = res.getHeader("Set-Cookie");
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("CSRF-TOKEN="));

        String body = new String(res.body(), StandardCharsets.UTF_8);
        assertTrue(body.contains("<form method=\"post\" action=\"/login\">"));
        assertTrue(body.contains("name=\"_csrf\""));
        assertTrue(body.contains("name=\"username\""));
        assertTrue(body.contains("name=\"password\""));
    }

    @Test
    void postLogin_withValidCredentials_setsSidCookie_and302() throws IOException {
        LoginPlugin plugin = new LoginPlugin();
        String token = "test-token";
        String form = "username=axel&password=axem&_csrf=" + token;
        HttpRequest req = new HttpRequest(
                "POST", "/login", null, "HTTP/1.1",
                Map.of("Content-Type", "application/x-www-form-urlencoded",
                       "Cookie", "CSRF-TOKEN=" + token),
                form.getBytes(StandardCharsets.UTF_8),
                "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(302, res.statusCode());
        assertEquals("/", res.getHeader("Location"));
        String setCookie = res.getHeader("Set-Cookie");
        assertNotNull(setCookie);
        assertTrue(setCookie.startsWith("SID="));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("SameSite=Lax"));
        assertTrue(setCookie.contains("Secure"));
        assertTrue(setCookie.contains("Path=/"));
        assertTrue(setCookie.contains("Max-Age=1800")); // 30 min default
    }

    @Test
    void postLogin_withInvalidCredentials_rendersFormWithError() throws IOException {
        LoginPlugin plugin = new LoginPlugin();
        String token = "test-token";
        String form = "username=axel&password=fel&_csrf=" + token;
        HttpRequest req = new HttpRequest(
                "POST", "/login", null, "HTTP/1.1",
                Map.of("Content-Type", "application/x-www-form-urlencoded",
                       "Cookie", "CSRF-TOKEN=" + token),
                form.getBytes(StandardCharsets.UTF_8),
                "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(200, res.statusCode());
        String body = new String(res.body(), StandardCharsets.UTF_8);
        assertTrue(body.contains("Logga in"));
        assertTrue(body.toLowerCase().contains("invalid credentials"));
        assertTrue(body.contains("name=\"_csrf\""));
    }
}
