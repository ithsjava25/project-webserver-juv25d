package org.juv25d.router;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.juv25d.http.HttpRequest;
import org.juv25d.plugin.NotFoundPlugin;
import org.juv25d.plugin.Plugin;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SimpleRouterTest {

    private SimpleRouter router;
    private Plugin mockPluginA;
    private Plugin mockPluginB;
    private Plugin notFoundPlugin;

    @BeforeEach
    void setUp() {
        router = new SimpleRouter();
        mockPluginA = mock(Plugin.class);
        mockPluginB = mock(Plugin.class);
        notFoundPlugin = new NotFoundPlugin(); // Assuming NotFoundPlugin is a concrete class
    }

    @Test
    void resolve_returnsRegisteredPluginForExactPath() {
        router.registerPlugin("/pathA", mockPluginA);
        HttpRequest request = new HttpRequest("GET", "/pathA", null, "HTTP/1.1", Map.of(), new byte[0], "UNKNOWN");

        Plugin resolvedPlugin = router.resolve(request);
        assertEquals(mockPluginA, resolvedPlugin);
    }

    @Test
    void resolve_returnsNotFoundPluginForUnregisteredPath() {
        router.registerPlugin("/pathA", mockPluginA);
        HttpRequest request = new HttpRequest("GET", "/nonExistentPath", null, "HTTP/1.1", Map.of(), new byte[0], "UNKNOWN");

        Plugin resolvedPlugin = router.resolve(request);
        // Assuming SimpleRouter's constructor initializes notFoundPlugin
        // or there's a way to get it for assertion
        assertTrue(resolvedPlugin instanceof NotFoundPlugin);
    }

    @Test
    void resolve_returnsWildcardPluginForMatchingPath() {
        router.registerPlugin("/api/*", mockPluginA);
        HttpRequest request = new HttpRequest("GET", "/api/users", null, "HTTP/1.1", Map.of(), new byte[0], "UNKNOWN");

        Plugin resolvedPlugin = router.resolve(request);
        assertEquals(mockPluginA, resolvedPlugin);
    }

    @Test
    void resolve_returnsNotFoundPluginIfNoWildcardMatch() {
        router.registerPlugin("/admin/*", mockPluginA);
        HttpRequest request = new HttpRequest("GET", "/api/users", null, "HTTP/1.1", Map.of(), new byte[0], "UNKNOWN");

        Plugin resolvedPlugin = router.resolve(request);
        assertTrue(resolvedPlugin instanceof NotFoundPlugin);
    }

    @Test
    void resolve_prefersExactMatchOverWildcard() {
        router.registerPlugin("/api/users", mockPluginA);
        router.registerPlugin("/api/*", mockPluginB);
        HttpRequest request = new HttpRequest("GET", "/api/users", null, "HTTP/1.1", Map.of(), new byte[0], "UNKNOWN");

        Plugin resolvedPlugin = router.resolve(request);
        assertEquals(mockPluginA, resolvedPlugin);
    }

    @Test
    void resolve_handlesRootPath() {
        router.registerPlugin("/", mockPluginA);
        HttpRequest request = new HttpRequest("GET", "/", null, "HTTP/1.1", Map.of(), new byte[0], "UNKNOWN");

        Plugin resolvedPlugin = router.resolve(request);
        assertEquals(mockPluginA, resolvedPlugin);
    }

    @Test
    void resolve_handlesRootWildcardPath() {
        router.registerPlugin("/*", mockPluginA);
        HttpRequest request = new HttpRequest("GET", "/any/path/here", null, "HTTP/1.1", Map.of(), new byte[0], "UNKNOWN");

        Plugin resolvedPlugin = router.resolve(request);
        assertEquals(mockPluginA, resolvedPlugin);
    }

    @Test
    void resolve_returnsNotFoundPluginForEmptyRouter() {
        HttpRequest request = new HttpRequest("GET", "/anypath", null, "HTTP/1.1", Map.of(), new byte[0], "UNKNOWN");
        Plugin resolvedPlugin = router.resolve(request);
        assertTrue(resolvedPlugin instanceof NotFoundPlugin);
    }

    @Test
    void resolve_prefersMoreSpecificWildcardOverLessSpecific() {
        router.registerPlugin("/api/*", mockPluginA);
        router.registerPlugin("/api/users/*", mockPluginB);

        HttpRequest request = new HttpRequest(
            "GET",
            "/api/users/123",
            null,
            "HTTP/1.1",
            Map.of(),
            new byte[0],
            "UNKNOWN"
        );

        Plugin resolvedPlugin = router.resolve(request);
        assertEquals(mockPluginB, resolvedPlugin);
    }

}
