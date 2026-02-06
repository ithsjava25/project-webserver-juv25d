package org.example.filter;

import org.example.plugin.Plugin;
import org.example.http.HttpRequest;
import org.example.http.HttpResponse;

import java.io.IOException;
import java.util.List;

public class FilterChain {
    private final List<Filter> filters;
    private final Plugin plugin;
    private int index = 0;

    public FilterChain(List<Filter> filters, Plugin plugin) {
        this.filters = filters;
        this.plugin = plugin;
    }

    public void doFilter(HttpRequest req, HttpResponse res) throws IOException {
        if (index < filters.size()) {
            Filter next = filters.get(index++);
            next.doFilter(req, res, this);
        } else {
            plugin.handle(req, res); // Done after we pass https through all the filters we have set up
        }
    }

}
