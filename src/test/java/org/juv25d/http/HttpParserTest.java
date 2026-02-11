package org.juv25d.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HttpParserTest {

    private HttpParser parser;

    @BeforeEach
    void setUp() {
        parser = new HttpParser();
    }

    @Test
    void parseValidGetRequest() throws IOException {
        // Arrange
        String request = "GET /index.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "Connection: close\r\n" + "\r\n";

        // Act
        HttpRequest result = parser.parse(createInputStream(request));

        // Assert
        assertThat(result.method()).isEqualTo("GET");
        assertThat(result.path()).isEqualTo("/index.html");
        assertThat(result.headers().get("Host")).isEqualTo("localhost:8080");
        assertThat(result.headers().get("Connection")).isEqualTo("close");

    }
    private InputStream createInputStream (String request) {
        return new ByteArrayInputStream(request.getBytes());

        //return new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
    }

}
