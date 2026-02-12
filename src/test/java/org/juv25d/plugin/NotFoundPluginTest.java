package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundPluginTest {

    @Test
    void sets404StatusAndBody() throws IOException {
        NotFoundPlugin plugin = new NotFoundPlugin();
        HttpRequest req = new HttpRequest("GET", "/unknown", null, "HTTP/1.1", Map.of(), new byte[0], "UNKNOWN");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(404, res.statusCode());
        assertEquals("Not Found", res.statusText());
        assertArrayEquals("404 - Resource Not Found".getBytes(), res.body());
    }
}
