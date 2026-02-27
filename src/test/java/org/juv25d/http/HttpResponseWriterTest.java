package org.juv25d.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HttpResponseWriterTest {


    @Test
    @DisplayName("Should write a valid HTTP 200 OK response for GET ")
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
        HttpResponseWriter.write(out, "GET", response);

        // Assert
        String result = out.toString(StandardCharsets.UTF_8);

        assertThat(result).startsWith("HTTP/1.1 200 OK");
        assertThat(result).contains("Content-Type: text/plain");
        assertThat(result).contains("Content-Length: 11");
        assertThat(result).endsWith("Hello World");
    }

    @Test
    @DisplayName("Should write headers but NO body for HEAD request")
    void writesOnlyHeadersForHeadRequest() throws Exception {
        // Arrange
        byte[] bodyContent = "Hello World".getBytes(StandardCharsets.UTF_8);
        HttpResponse response = new HttpResponse(
            200,
            "OK",
            Map.of("Content-Type", "text/plain"),
            bodyContent
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Act - Skicka "HEAD"
        HttpResponseWriter.write(out, "HEAD", response);

        // Assert
        String result = out.toString(StandardCharsets.UTF_8);

        // 1. Statusrad och headers ska finnas
        assertThat(result).startsWith("HTTP/1.1 200 OK");
        assertThat(result).contains("Content-Type: text/plain");

        // 2. Content-Length ska vara 11 trots att ingen body skickas
        assertThat(result).contains("Content-Length: 11");

        // 3. Viktigast: Resultatet ska sluta med dubbla radbrytningar (\r\n\r\n) och INTE innehålla bodyn
        assertThat(result).endsWith("\r\n\r\n");
        assertThat(result).doesNotContain("Hello World");
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

        HttpResponseWriter.write(out, "GET",  response);

        String result = out.toString(StandardCharsets.UTF_8);

        assertThat(result).startsWith("HTTP/1.1 404 Not Found");
    }

    @Test
    @DisplayName("Should write 404 status but no body for HEAD request")
    void writes404HeadResponse() throws Exception {
        HttpResponse response = new HttpResponse(
            404,
            "Not Found",
            Map.of("Content-Type", "text/plain"),
            "Not found".getBytes(StandardCharsets.UTF_8)
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Act
        HttpResponseWriter.write(out, "HEAD", response);

        String result = out.toString(StandardCharsets.UTF_8);

        assertThat(result).startsWith("HTTP/1.1 404 Not Found");
        assertThat(result).contains("Content-Length: 9");
        assertThat(result).endsWith("\r\n\r\n");
        assertThat(result).doesNotContain("Not found");
    }

}
