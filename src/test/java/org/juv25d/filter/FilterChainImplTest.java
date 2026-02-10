package org.juv25d.filter;

import org.juv25d.Pipeline;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class FilterChainImplTest {

    @Test
    void filters_areCalledInOrderAndPluginLast() throws IOException {

        List<String> calls = new ArrayList<>();

        Filter f1 = (req, res, chain) -> {
            calls.add("f1-before");
            chain.doFilter(req, res);
            calls.add("f1-after");
        };

        Filter f2 = (req, res, chain) -> {
            calls.add("f2-before");
            chain.doFilter(req, res);
            calls.add("f2-after");
        };

        Plugin plugin = (req, res) -> calls.add("plugin");

        FilterChainImpl chain = new FilterChainImpl(
            List.of(f1, f2),
            plugin
        );

        HttpRequest req = new HttpRequest(
            "GET",
            "/",
            null,
            "HTTP/1.1",
            Map.of(),
            new byte[0]
        );

        chain.doFilter(req, new HttpResponse());

        assertEquals(
            List.of(
                "f1-before",
                "f2-before",
                "plugin",
                "f2-after",
                "f1-after"
            ),
            calls
        );
    }

    @Test
    void filter_canStopChainExecution() throws IOException {

        List<String> calls = new ArrayList<>();

        Filter blockingFilter = (req, res, chain) -> {
            calls.add("blocked");
        };

        Plugin plugin = (req, res) -> calls.add("plugin");

        FilterChainImpl chain = new FilterChainImpl(
            List.of(blockingFilter),
            plugin
        );

        HttpRequest req = new HttpRequest(
            "GET", "/", null, "HTTP/1.1", Map.of(), new byte[0]
        );

        chain.doFilter(req, new HttpResponse());

        assertEquals(List.of("blocked"), calls);
    }

    @Test
    void pipeline_createsNewChainPerRequest() {

        Pipeline pipeline = new Pipeline();
        pipeline.addFilter((req, res, chain) -> {});
        pipeline.setPlugin((req, res) -> {});

        FilterChainImpl c1 = pipeline.createChain();
        FilterChainImpl c2 = pipeline.createChain();

        assertNotSame(c1, c2);
    }

    @Test
    void pipeline_initCallsFilterInit() {

        AtomicInteger counter = new AtomicInteger();

        Filter filter = new Filter() {
            @Override
            public void init() {
                counter.incrementAndGet();
            }

            @Override
            public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) {}
        };

        Pipeline pipeline = new Pipeline();
        pipeline.addFilter(filter);

        pipeline.init();

        assertEquals(1, counter.get());
    }

    @Test
    void plugin_isCalledWhenNoFiltersExist() throws IOException {

        List<String> calls = new ArrayList<>();

        Plugin plugin = (req, res) -> calls.add("plugin");

        FilterChainImpl chain = new FilterChainImpl(
            List.of(),
            plugin
        );

        HttpRequest req = new HttpRequest(
            "GET", "/", null, "HTTP/1.1", Map.of(), new byte[0]
        );

        chain.doFilter(req, new HttpResponse());

        assertEquals(List.of("plugin"), calls);
    }
}
