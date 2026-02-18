package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.juv25d.Server.Pipeline;
import org.juv25d.filter.annotation.Global;
import org.juv25d.filter.annotation.Route;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class FilterScannerTest {

    @Test
    void shouldRegisterGlobalFilter() {
        Pipeline pipeline = mock(Pipeline.class);
        Filter filter = new GlobalTestFilter();
        FilterScanner.register(filter, pipeline);
        verify(pipeline).addGlobalFilter(filter, 5);
        verifyNoMoreInteractions(pipeline);
    }

    @Test
    void shouldRegisterRouteFilterForEachPattern() {
        Pipeline pipeline = mock(Pipeline.class);
        Filter filter = new RouteTestFilter();
        FilterScanner.register(filter, pipeline);
        verify(pipeline).addRouteFilter(filter, 3, "/users");
        verify(pipeline).addRouteFilter(filter, 3, "/admin");
        verifyNoMoreInteractions(pipeline);
    }

    @Test
    void shouldThrowIfBothAnnotationsPresent() {
        Pipeline pipeline = mock(Pipeline.class);
        Filter filter = new InvalidAnnotatedFilter();
        assertThrows(IllegalStateException.class,
            () -> FilterScanner.register(filter, pipeline));
        verifyNoInteractions(pipeline);
    }

    @Test
    void shouldIgnoreNonFilterObjects() {
        Pipeline pipeline = mock(Pipeline.class);
        Object notAFilter = new Object();
        FilterScanner.register(notAFilter, pipeline);
        verifyNoInteractions(pipeline);
    }


    @Test
    void shouldRejectFilterWithNoAnnotation() {
        Pipeline pipeline = mock(Pipeline.class);
        Filter filter = new UnannotatedFilter();
        assertThrows(IllegalStateException.class,
            () -> FilterScanner.register(filter, pipeline));
        verifyNoInteractions(pipeline);
    }

    static class UnannotatedFilter implements Filter {
        public void init() {}
        public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) {}
        public void destroy() {}
    }

    @Global(order = 5)
    static class GlobalTestFilter implements Filter {
        public void init() {}
        public void doFilter(
            HttpRequest req,
            HttpResponse res,
            FilterChain chain) {}
        public void destroy() {}
    }

    @Route(value = {"/users", "/admin"}, order = 3)
    static class RouteTestFilter implements Filter {
        public void init() {}
        public void doFilter(
            HttpRequest req,
            HttpResponse res,
            FilterChain chain) {}
        public void destroy() {}
    }

    @Global(order = 1)
    @Route(value = {"/test"}, order = 1)
    static class InvalidAnnotatedFilter implements Filter {
        public void init() {}
        public void doFilter(
            HttpRequest req,
            HttpResponse res,
            FilterChain chain) {}
        public void destroy() {}
    }
}
