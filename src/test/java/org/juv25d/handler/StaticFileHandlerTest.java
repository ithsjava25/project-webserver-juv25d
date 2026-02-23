package org.juv25d.handler;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StaticFileHandlerTest {

    @Test
    void shouldReturn200ForExistingFile() {
        HttpRequest request = createRequest("GET", "/index.html");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.statusText()).isEqualTo("OK");
    }

    @Test
    void shouldReturnCorrectContentTypeForHtml() {
        HttpRequest request = createRequest("GET", "/index.html");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.headers()).containsEntry("Content-Type", "text/html; charset=utf-8");
    }

    @Test
    void shouldReturnCorrectContentTypeForCss() {
        HttpRequest request = createRequest("GET", "/css/styles.css");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.headers()).containsEntry("Content-Type", "text/css; charset=utf-8");
    }

    @Test
    void shouldReturnCorrectContentTypeForJs() {
        HttpRequest request = createRequest("GET", "/js/app.js");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.headers()).containsEntry("Content-Type", "application/javascript; charset=utf-8");
    }

    @Test
    void shouldIncludeEtagAndCacheControlForSuccessfulStaticResponse() {
        HttpRequest request = createRequest("GET", "/index.html");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers()).containsKey("ETag");
        assertThat(response.headers()).containsEntry("Cache-Control", "public, max-age=5");
    }

    @Test
    void shouldReturn304WhenIfNoneMatchMatchesEtag() {
        HttpRequest first = createRequest("GET", "/index.html");
        HttpResponse firstResponse = StaticFileHandler.handle(first);

        assertThat(firstResponse.statusCode()).isEqualTo(200);
        String etag = firstResponse.headers().get("ETag");
        assertThat(etag).isNotBlank();

        Map<String, String> headers = new HashMap<>();
        headers.put("If-None-Match", etag);

        HttpRequest second = createRequest("GET", "/index.html", headers);
        HttpResponse secondResponse = StaticFileHandler.handle(second);

        assertThat(secondResponse.statusCode()).isEqualTo(304);
        assertThat(secondResponse.statusText()).isEqualTo("Not Modified");
        assertThat(secondResponse.body()).isEmpty();
        assertThat(secondResponse.headers()).containsEntry("ETag", etag);
        assertThat(secondResponse.headers()).containsEntry("Cache-Control", "public, max-age=5");
    }

    @Test
    void shouldReturn404ForNonExistingFile() {
        HttpRequest request = createRequest("GET", "/nonexistent.html");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.statusText()).isEqualTo("Not Found");
    }

    @Test
    void shouldReturn404ResponseWithHtmlContent() {
        HttpRequest request = createRequest("GET", "/missing.html");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.body()).isNotEmpty();
        assertThat(new String(response.body())).contains("404");
        assertThat(new String(response.body())).contains("Not Found");
    }

    @Test
    void shouldServeIndexHtmlForRootPath() {
        HttpRequest request = createRequest("GET", "/");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers()).containsEntry("Content-Type", "text/html; charset=utf-8");
        assertThat(new String(response.body())).contains("<!DOCTYPE html>");
    }

    @Test
    void shouldReturn403ForPathTraversalAttempt() {
        HttpRequest request = createRequest("GET", "/../../../etc/passwd");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.statusText()).isEqualTo("Forbidden");
    }

    @Test
    void shouldReturn403ForPathWithDoubleDots() {
        HttpRequest request = createRequest("GET", "/css/../../secrets.txt");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void shouldReturn403ForPathWithDoubleSlashes() {
        HttpRequest request = createRequest("GET", "//etc/passwd");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void shouldReturn403ForPathWithBackslashes() {
        HttpRequest request = createRequest("GET", "/css\\..\\secrets.txt");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void shouldReturn405ForNonGetRequest() {
        HttpRequest request = createRequest("POST", "/index.html");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(405);
        assertThat(response.statusText()).isEqualTo("Method Not Allowed");
    }

    @Test
    void shouldReturn405ForPutRequest() {
        HttpRequest request = createRequest("PUT", "/index.html");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(405);
    }

    @Test
    void shouldReturn405ForDeleteRequest() {
        HttpRequest request = createRequest("DELETE", "/index.html");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.statusCode()).isEqualTo(405);
    }

    @Test
    void shouldHandleValidNestedPaths() {
        HttpRequest request = createRequest("GET", "/css/styles.css");
        HttpResponse response = StaticFileHandler.handle(request);

        // Should either return 200 (if file exists) or 404 (if not), but NOT 403
        assertThat(response.statusCode()).isIn(200, 404);
    }

    @Test
    void shouldReturnNonEmptyBodyForSuccessfulRequest() {
        HttpRequest request = createRequest("GET", "/index.html");
        HttpResponse response = StaticFileHandler.handle(request);

        assertThat(response.body()).isNotEmpty();
        assertThat(response.body().length).isGreaterThan(0);
    }

    // Helper method to create HttpRequest objects
    private HttpRequest createRequest(String method, String path) {
        return new HttpRequest(
            method,
            path,
            null,
            "HTTP/1.1",
            new HashMap<>(),
            new byte[0],
            "UNKNOWN"
        );
    }

    private HttpRequest createRequest(String method, String path, Map<String, String> headers) {
        return new HttpRequest(
            method,
            path,
            null,
            "HTTP/1.1",
            headers != null ? headers : new HashMap<>(),
            new byte[0],
            "UNKNOWN"
        );
    }
}
