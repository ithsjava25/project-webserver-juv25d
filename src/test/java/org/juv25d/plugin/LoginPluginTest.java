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
import java.util.Base64;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.*;

class LoginPluginTest {

    private @Nullable Path tempUsersFile = null;

    @BeforeEach
    void setup() {
        System.clearProperty("users.file");
        System.clearProperty("users.dir");
        System.clearProperty("users.filename");
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
    void rendersHtml_whenAuthNotActive() throws IOException {
        // Ensure BasicAuth is not active by pointing to a non-existing users file
        Path missing = Files.createTempFile("users-missing", ".txt");
        Files.deleteIfExists(missing);
        System.setProperty("users.file", missing.toString());

        // No users file -> BasicAuth should be pass-through
        LoginPlugin plugin = new LoginPlugin();
        HttpRequest req = new HttpRequest("GET", "/login", null, "HTTP/1.1", Map.of(), new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(200, res.statusCode());
        assertEquals("text/html; charset=UTF-8", res.getHeader("Content-Type"));
        String body = new String(res.body(), StandardCharsets.UTF_8);
        assertTrue(body.contains("You are logged in"));
        assertTrue(body.contains("<meta http-equiv=\"refresh\""));
    }

    @Test
    void returns401_whenMissingAuth_andUsersExist() throws IOException {
        tempUsersFile = Files.createTempFile("users", ".txt");
        Files.writeString(tempUsersFile, "axel:axem\n", StandardCharsets.UTF_8);
        System.setProperty("users.file", tempUsersFile.toString());

        LoginPlugin plugin = new LoginPlugin();
        HttpRequest req = new HttpRequest("GET", "/login", null, "HTTP/1.1", Map.of(), new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(401, res.statusCode());
        assertNotNull(res.getHeader("WWW-Authenticate"));
    }

    @Test
    void rendersHtml_whenCorrectCredentials() throws IOException {
        tempUsersFile = Files.createTempFile("users", ".txt");
        Files.writeString(tempUsersFile, "axel:axem\n", StandardCharsets.UTF_8);
        System.setProperty("users.file", tempUsersFile.toString());

        LoginPlugin plugin = new LoginPlugin();
        String goodCred = Base64.getEncoder().encodeToString("axel:axem".getBytes(StandardCharsets.UTF_8));
        HttpRequest req = new HttpRequest(
                "GET", "/login", null, "HTTP/1.1",
                Map.of("Authorization", "Basic " + goodCred),
                new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(200, res.statusCode());
        assertEquals("text/html; charset=UTF-8", res.getHeader("Content-Type"));
        String body = new String(res.body(), StandardCharsets.UTF_8);
        assertTrue(body.contains("You are logged in"));
        assertTrue(body.contains("<meta http-equiv=\"refresh\""));
    }
}
