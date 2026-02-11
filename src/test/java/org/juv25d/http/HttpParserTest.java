package org.juv25d.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
            "Connection: close\r\n";

        // Act
        HttpRequest result = parser.parse(createInputStream(request));

        // Assert
        assertThat(result.method()).isEqualTo("GET");
        assertThat(result.path()).isEqualTo("/index.html");
        assertThat(result.httpVersion()).isEqualTo("HTTP/1.1");
        assertThat(result.headers().get("Host")).isEqualTo("localhost:8080");
        assertThat(result.headers().get("Connection")).isEqualTo("close");
        assertThat(result.body()).isEmpty();
        assertThat(result.queryString()).isNull();
    }

    @Test
    void parseEmptyRequest_throwsException() {
        // Arrange
        String request = "";

        // Act + Assert
        assertThatThrownBy(() -> parser.parse(createInputStream(request)))
            .isInstanceOf(IOException.class)
            .hasMessage("The request is empty");
    }

    @Test
    void parseMalformedRequest_throwsException() {
        // Arrange
        String request = "GET /index.html\r\n";

        // Act + Assert
        assertThatThrownBy(() -> parser.parse(createInputStream(request)))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Malformed request line");
    }

    @Test
    void parseValidQueryString() throws IOException {
        // Arrange
        String request = "GET /search?q=java HTTP/1.1\r\n";

        // Act
        HttpRequest result = parser.parse(createInputStream(request));

        // Assert
        assertThat(result.path()).isEqualTo("/search");
        assertThat(result.queryString()).isEqualTo("q=java");
    }

    private InputStream createInputStream(String request) {
        return new ByteArrayInputStream(request.getBytes());
    }
}
