package org.juv25d;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    @Test
    void usesNotFoundPluginByDefault() throws IOException {
        Pipeline pipeline = new Pipeline();
        HttpRequest req = new HttpRequest("GET", "/", null, "HTTP/1.1", Map.of(), new byte[0]);
        HttpResponse res = new HttpResponse();

        pipeline.createChain().doFilter(req, res);

        assertEquals(404, res.statusCode());
    }

    @Test
    void throwsExceptionWhenSettingNullPlugin() {
        Pipeline pipeline = new Pipeline();
        assertThrows(IllegalArgumentException.class, () -> pipeline.setPlugin(null));
    }

    @Test
    void customPluginIsUsed() throws IOException {
        Pipeline pipeline = new Pipeline();
        pipeline.setPlugin((req, res) -> {
            res.setStatusCode(200);
            res.setBody("Custom".getBytes());
        });

        HttpRequest req = new HttpRequest("GET", "/", null, "HTTP/1.1", Map.of(), new byte[0]);
        HttpResponse res = new HttpResponse();

        pipeline.createChain().doFilter(req, res);

        assertEquals(200, res.statusCode());
        assertArrayEquals("Custom".getBytes(), res.body());
    }
}
