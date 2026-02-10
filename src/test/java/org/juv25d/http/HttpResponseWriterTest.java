package org.juv25d.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HttpResponseWriterTest {


    @Test
    @DisplayName("Should write a valid HTTP 200 OK response ")
    void writesValidHttp200Response() throws Exception {
        // Arrange
        HttpResponse response = new HttpResponse(
            200,
            "OK",
            Map.of("Content-Type", "text/plain"),
            "Hello World".getBytes(StandardCharsets.UTF_8)
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Act
        HttpResponseWriter.write(out, response);

        // Assert
        String result = out.toString(StandardCharsets.UTF_8);

        assertThat(result).startsWith("HTTP/1.1 200 OK");
        assertThat(result).contains("Content-Type: text/plain");
        assertThat(result).contains("Content-Length: 11");
        assertThat(result).endsWith("Hello World");
    }

    @Test
    @DisplayName("Should write a valid HTTP 404 Not Found Response")
    void writes404NotFoundResponse() throws Exception {
        HttpResponse response = new HttpResponse(
            404,
            "Not Found",
            Map.of("Content-Type", "text/plain"),
            "Not found".getBytes(StandardCharsets.UTF_8)
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpResponseWriter.write(out, response);

        String result = out.toString(StandardCharsets.UTF_8);

        assertThat(result).startsWith("HTTP/1.1 404 Not Found");
    }

}
