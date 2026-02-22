package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FilterRegistryTest {

    static class TestFilter implements Filter {
        int destroyCount = 0;

        @Override
        public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) {
        }

        @Override
        public void destroy() {
            destroyCount++;
        }
    }

    @Test
    void shouldRegisterGlobalFilter() {
        FilterRegistry registry = new FilterRegistry();
        TestFilter filter = new TestFilter();

        registry.registerGlobal(filter, 1);

        List<FilterRegistration> globals = registry.getGlobalFilters();

        assertEquals(1, globals.size());
        assertSame(filter, globals.get(0).filter());
    }

    @Test
    void shouldRegisterRouteFilter() {
        FilterRegistry registry = new FilterRegistry();
        TestFilter filter = new TestFilter();

        registry.registerRoute(filter, 1, "/test");

        Map<String, List<FilterRegistration>> routes = registry.getRouteFilters();

        assertTrue(routes.containsKey("/test"));
        assertEquals(1, routes.get("/test").size());
        assertSame(filter, routes.get("/test").get(0).filter());
    }

    @Test
    void shouldReturnUnmodifiableCollections() {
        FilterRegistry registry = new FilterRegistry();

        assertThrows(UnsupportedOperationException.class, () ->
            registry.getGlobalFilters().add(null)
        );

        assertThrows(UnsupportedOperationException.class, () ->
            registry.getRouteFilters().put("x", List.of())
        );
    }

    @Test
    void shouldDestroyFiltersOnlyOnce() {
        FilterRegistry registry = new FilterRegistry();
        TestFilter filter = new TestFilter();

        registry.registerGlobal(filter, 1);
        registry.registerRoute(filter, 1, "/test");

        registry.shutdown();

        assertEquals(1, filter.destroyCount);
    }
}
