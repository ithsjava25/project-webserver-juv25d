package org.juv25d.Server;

import org.junit.jupiter.api.Test;
import org.juv25d.filter.Filter;
import org.juv25d.filter.FilterChain;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.router.Router;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ServerBuilderTest {

    @Test
    void buildShouldFailIfLoggerMissing() {
        ServerBuilder builder = new ServerBuilder()
            .router(mock(Router.class));

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void buildShouldFailIfRouterMissing() {
        ServerBuilder builder = new ServerBuilder()
            .logger(Logger.getGlobal());

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void buildShouldInitializeAnnotatedFilters() {
        AtomicBoolean initialized = new AtomicBoolean(false);

        new ServerBuilder()
            .logger(Logger.getGlobal())
            .router(mock(Router.class))
            .addFilter(new TestGlobalFilter(initialized))
            .build();

        assertTrue(initialized.get(),
            "Annotated filter should be initialized during build()");
    }

    @Global(order = 1)
    static class TestGlobalFilter implements Filter {

        private final AtomicBoolean initialized;

        TestGlobalFilter(AtomicBoolean initialized) {
            this.initialized = initialized;
        }

        @Override
        public void init() {
            initialized.set(true);
        }

        @Override
        public void doFilter(HttpRequest request,
                             HttpResponse response,
                             FilterChain chain) {
        }

        @Override
        public void destroy() {
        }
    }
}
