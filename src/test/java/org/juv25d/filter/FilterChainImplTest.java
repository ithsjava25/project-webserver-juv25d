package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
