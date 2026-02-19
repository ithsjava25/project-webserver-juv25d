package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RedirectFilterTest {

    private FilterChain mockChain;

    @BeforeEach
    void setUp() {
        mockChain = Mockito.mock(FilterChain.class);
    }

    @Test
    void shouldRedirect301ForMatchingPath() throws IOException {
        // Given
        List<RedirectRule> rules = List.of(
            new RedirectRule("/old-page", "/new-page", 301)
        );
        RedirectFilter filter = new RedirectFilter(rules);

        HttpRequest request = createRequest("/old-page");
        HttpResponse response = new HttpResponse();

        // When
        filter.doFilter(request, response, mockChain);

        // Then
        assertThat(response.statusCode()).isEqualTo(301);
        assertThat(response.statusText()).isEqualTo("Moved Permanently");
        assertThat(response.headers().get("Location")).isEqualTo("/new-page");
        assertThat(response.headers().get("Content-Length")).isEqualTo("0");
        assertThat(response.body()).isEmpty();

        // Pipeline should be stopped
        verify(mockChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldRedirect302ForTemporaryRedirect() throws IOException {
        // Given
        List<RedirectRule> rules = List.of(
            new RedirectRule("/temp", "https://example.com/temporary", 302)
        );
        RedirectFilter filter = new RedirectFilter(rules);

        HttpRequest request = createRequest("/temp");
        HttpResponse response = new HttpResponse();

        // When
        filter.doFilter(request, response, mockChain);

        // Then
        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.statusText()).isEqualTo("Found");
        assertThat(response.headers().get("Location")).isEqualTo("https://example.com/temporary");

        // Pipeline should be stopped
        verify(mockChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldNotRedirectWhenNoMatchingRule() throws IOException {
        // Given
        List<RedirectRule> rules = List.of(
            new RedirectRule("/old-page", "/new-page", 301)
        );
        RedirectFilter filter = new RedirectFilter(rules);

        HttpRequest request = createRequest("/other-page");
        HttpResponse response = new HttpResponse();

        // When
        filter.doFilter(request, response, mockChain);

        // Then - pipeline should continue
        verify(mockChain, times(1)).doFilter(request, response);

        // No redirect headers set
        assertThat(response.headers().get("Location")).isNull();
    }

    @Test
    void shouldMatchWildcardPattern() throws IOException {
        // Given
        List<RedirectRule> rules = List.of(
            new RedirectRule("/docs/*", "/documentation/", 301)
        );
        RedirectFilter filter = new RedirectFilter(rules);

        HttpRequest request = createRequest("/docs/api");
        HttpResponse response = new HttpResponse();

        // When
        filter.doFilter(request, response, mockChain);

        // Then
        assertThat(response.statusCode()).isEqualTo(301);
        assertThat(response.headers().get("Location")).isEqualTo("/documentation/");

        verify(mockChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldMatchMultiplePathsWithWildcard() throws IOException {
        // Given
        List<RedirectRule> rules = List.of(
            new RedirectRule("/docs/*", "/documentation/", 301)
        );
        RedirectFilter filter = new RedirectFilter(rules);

        // Test multiple paths
        testWildcardMatch(filter, "/docs/api");
        testWildcardMatch(filter, "/docs/guide");
        testWildcardMatch(filter, "/docs/tutorial/advanced");
    }

    @Test
    void shouldEvaluateRulesInOrder() throws IOException {
        // Given - first matching rule should win
        List<RedirectRule> rules = List.of(
            new RedirectRule("/page", "/first-target", 301),
            new RedirectRule("/page", "/second-target", 302)  // This won't be used
        );
        RedirectFilter filter = new RedirectFilter(rules);

        HttpRequest request = createRequest("/page");
        HttpResponse response = new HttpResponse();

        // When
        filter.doFilter(request, response, mockChain);

        // Then - first rule should be applied
        assertThat(response.statusCode()).isEqualTo(301);
        assertThat(response.headers().get("Location")).isEqualTo("/first-target");
    }

    @Test
    void shouldHandleEmptyRulesList() throws IOException {
        // Given
        List<RedirectRule> rules = List.of();  // No rules
        RedirectFilter filter = new RedirectFilter(rules);

        HttpRequest request = createRequest("/any-page");
        HttpResponse response = new HttpResponse();

        // When
        filter.doFilter(request, response, mockChain);

        // Then - pipeline should continue
        verify(mockChain, times(1)).doFilter(request, response);
    }

    // Helper methods

    private void testWildcardMatch(RedirectFilter filter, String path) throws IOException {
        HttpRequest request = createRequest(path);
        HttpResponse response = new HttpResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.statusCode()).isEqualTo(301);
        assertThat(response.headers().get("Location")).isEqualTo("/documentation/");
        verify(chain, never()).doFilter(any(), any());
    }

    private HttpRequest createRequest(String path) {
        return new HttpRequest(
            "GET",
            path,
            "",
            "HTTP/1.1",
            Map.<String, String>of(),
            new byte[0],
            "127.0.0.1"
        );
    }
}
