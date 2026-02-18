package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.juv25d.Pipeline;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.plugin.Plugin;
import org.juv25d.router.SimpleRouter; // New import

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RouteFilterTests {

    @Test
    void routeFilter_shouldOnlyRun_whenRouteMatches() throws Exception {
        Pipeline pipeline = new Pipeline();

        RecordingFilter route = new RecordingFilter("route");
        pipeline.addRouteFilter(route, 1, "/api/*");

        // Configure SimpleRouter and set it in the pipeline
        SimpleRouter router = new SimpleRouter();
        router.registerPlugin("/", new NoOpPlugin()); // Register NoOpPlugin for the root path
        pipeline.setRouter(router); // Set the router in the pipeline

        execute(pipeline, "/api/test");
        assertTrue(route.wasExecuted());
        route.reset();
        execute(pipeline, "/home");
        assertFalse(route.wasExecuted());
    }

    @Test
    void routeFilter_shouldMatchExactPath() throws Exception {
        Pipeline pipeline = new Pipeline();

        // Configure SimpleRouter and set it in the pipeline
        SimpleRouter router = new SimpleRouter();
        router.registerPlugin("/", new NoOpPlugin()); // Register NoOpPlugin for the root path
        pipeline.setRouter(router); // Set the router in the pipeline

        RecordingFilter exact = new RecordingFilter("exact");
        pipeline.addRouteFilter(exact, 1, "/admin");
        execute(pipeline, "/admin");
        assertTrue(exact.wasExecuted());
        exact.reset();
        execute(pipeline, "/admin/settings");
        assertFalse(exact.wasExecuted());
    }

    private void execute(Pipeline pipeline, String path) throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        when(request.path()).thenReturn(path);
        HttpResponse response = mock(HttpResponse.class);
        var chain = pipeline.createChain(request);
        chain.doFilter(request, response);
    }

    static class RecordingFilter implements Filter {
        private final String name;
        private boolean executed = false;

        RecordingFilter(String name) {
            this.name = name;
        }

        @Override
        public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
            executed = true;
            chain.doFilter(req, res);
        }

        boolean wasExecuted() {return executed;}
        void reset() {executed = false;}
    }

    static class NoOpPlugin implements Plugin {
        @Override
        public void handle(HttpRequest req, HttpResponse res) throws IOException {}
    }
}
