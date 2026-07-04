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

class BasicAuthPluginTest {

    private @Nullable Path tempUsersFile = null;

    @BeforeEach
    void setup() throws IOException {
        // ensure no leftover property unless we explicitly set it in a test
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
    void passThrough_whenUsersFileMissing() throws IOException {
        // Point to a non-existing users file to ensure auth is inactive
        Path missing = Files.createTempFile("users-missing", ".txt");
        Files.deleteIfExists(missing);
        System.setProperty("users.file", missing.toString());

        BasicAuthPlugin plugin = new BasicAuthPlugin();
        HttpRequest req = new HttpRequest("GET", "/login", null, "HTTP/1.1", Map.of(), new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        // Should not enforce auth when users file is missing
        assertEquals(200, res.statusCode());
        assertNull(res.getHeader("WWW-Authenticate"));
    }

    @Test
    void unauthorized_whenUsersExist_butNoAuthorizationHeader() throws IOException {
        tempUsersFile = Files.createTempFile("users", ".txt");
        Files.writeString(tempUsersFile, "axel:axem\n", StandardCharsets.UTF_8);
        System.setProperty("users.file", tempUsersFile.toString());

        BasicAuthPlugin plugin = new BasicAuthPlugin();
        HttpRequest req = new HttpRequest("GET", "/login", null, "HTTP/1.1", Map.of(), new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(401, res.statusCode());
        String www = res.getHeader("WWW-Authenticate");
        assertNotNull(www);
        assertTrue(www.startsWith("Basic realm=\""));
        assertArrayEquals("Unauthorized".getBytes(StandardCharsets.UTF_8), res.body());
    }

    @Test
    void unauthorized_whenWrongPassword() throws IOException {
        tempUsersFile = Files.createTempFile("users", ".txt");
        Files.writeString(tempUsersFile, "axel:axem\n", StandardCharsets.UTF_8);
        System.setProperty("users.file", tempUsersFile.toString());

        BasicAuthPlugin plugin = new BasicAuthPlugin();
        String badCred = basic("axel", "wrong");
        HttpRequest req = new HttpRequest(
                "GET", "/login", null, "HTTP/1.1",
                Map.of("Authorization", "Basic " + badCred),
                new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(401, res.statusCode());
    }

    @Test
    void ok_whenCorrectCredentials() throws IOException {
        tempUsersFile = Files.createTempFile("users", ".txt");
        Files.writeString(tempUsersFile, "axel:axem\n", StandardCharsets.UTF_8);
        System.setProperty("users.file", tempUsersFile.toString());

        BasicAuthPlugin plugin = new BasicAuthPlugin();
        String goodCred = basic("axel", "axem");
        HttpRequest req = new HttpRequest(
                "GET", "/login", null, "HTTP/1.1",
                Map.of("Authorization", "Basic " + goodCred),
                new byte[0], "TEST");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        // Should remain 200 OK when credentials are valid
        assertEquals(200, res.statusCode());
        assertNull(res.getHeader("WWW-Authenticate"));
    }

    private static String basic(String user, String pass) {
        return Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
    }
}
