package org.juv25d.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link HttpParser}.
 * <p>
 * Covers:
 * - Valid GET and POST requests
 * - Query string parsing
 * - Header parsing and validation
 * - Content-Length handling, including invalid and negative values
 * - Error handling for empty and malformed requests
 */
@DisplayName("HttpParser - unit tests")
class HttpParserTest {

    private HttpParser parser;

    @BeforeEach
    void setUp() {
        parser = new HttpParser();
    }

    /**
     * Parses a well-formed GET request without a body and extracts method, path,
     * HTTP version, and headers.
     */
    @DisplayName("Parses a valid GET request without body")
    @Test
    void parseValidGetRequest() throws IOException {
        // Arrange
        String request = "GET /index.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "Connection: close\r\n" +
            "\r\n";

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

    /**
     * Rejects empty requests (empty or only CRLF) with an informative IOException.
     */
    @DisplayName("Empty request → IOException with 'The request is empty'")
    @Test
    void parseEmptyRequest_throwsException() {
        // Arrange
        String emptyStringRequest = "";
        String emptyRequest = "\r\n";

        // Act + Assert
        assertThatThrownBy(() -> parser.parse(createInputStream(emptyStringRequest)))
            .isInstanceOf(IOException.class)
            .hasMessage("The request is empty");
        assertThatThrownBy(() -> parser.parse(createInputStream(emptyRequest)))
            .isInstanceOf(IOException.class)
            .hasMessage("The request is empty");
    }

    /**
     * Fails when the request line is malformed (missing HTTP version or parts).
     */
    @DisplayName("Malformed request line → IOException")
    @Test
    void parseMalformedRequest_throwsException() {
        // Arrange
        String request = "GET /index.html\r\n";

        // Act + Assert
        assertThatThrownBy(() -> parser.parse(createInputStream(request)))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Malformed request line");
    }

    /**
     * Fails when a header line is malformed (missing name/value separator).
     */
    @DisplayName("Malformed header line → IOException")
    @Test
    void parseMalformedHeader_throwsException() {
        // Arrange
        String request = "GET /index.html HTTP/1.1\r\n" +
            ":Host localhost:8080\r\n" +
            "Connection: close\r\n";

        // Act + Assert
        assertThatThrownBy(() -> parser.parse(createInputStream(request)))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Malformed header line");
    }

    /**
     * Extracts the query string and normalized path from the request target.
     */
    @DisplayName("Parses query string and normalizes path")
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

    /**
     * Parses a well-formed POST request with Content-Length and body.
     */
    @DisplayName("Parses a valid POST request with headers and body")
    @Test
    void parseValidPostRequest() throws IOException {
        // Arrange
        String body = "body";
        String request = "POST /users HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
            "\r\n" +
            body;

        // Act
        HttpRequest result = parser.parse(createInputStream(request));

        // Assert
        assertThat(result.method()).isEqualTo("POST");
        assertThat(result.path()).isEqualTo("/users");
        assertThat(result.httpVersion()).isEqualTo("HTTP/1.1");
        assertThat(result.headers().get("Host")).isEqualTo("localhost:8080");
        assertThat(result.headers().get("Content-Type")).isEqualTo("text/html");
        assertThat(result.body()).isEqualTo(body.getBytes());
    }

    /**
     * Rejects non-numeric Content-Length values.
     */
    @DisplayName("Invalid Content-Length (non-numeric) → IOException")
    @Test
    void parseInvalidContentLength_throwsException() {
        // Arrange
        String request = "POST /users HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: abc\r\n" +
            "\r\n";

        // Act + Assert
        assertThatThrownBy(() -> parser.parse(createInputStream(request)))
            .isInstanceOf(IOException.class)
            .hasMessage("Invalid Content-Length: abc");
    }

    /**
     * Rejects negative Content-Length values.
     */
    @DisplayName("Negative Content-Length → IOException")
    @Test
    void parseNegativeContentLength_throwsException() {
        // Arrange
        String request = "POST /users HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: -10\r\n" +
            "\r\n";

        // Act + Assert
        assertThatThrownBy(() -> parser.parse(createInputStream(request)))
            .isInstanceOf(IOException.class)
            .hasMessage("Negative Content-Length: -10");
    }

    /**
     * Utility to wrap a request string into an {@link InputStream}.
     */
    private InputStream createInputStream(String request) {
        return new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
    }
}
