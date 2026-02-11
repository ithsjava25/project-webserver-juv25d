package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.plugin.Plugin;

import java.io.IOException;
import java.util.List;

public class FilterChainImpl implements FilterChain {

    private final List<Filter> filters;
    private final Plugin plugin;
    private int index = 0;

    public FilterChainImpl(List<Filter> filters, Plugin plugin) {
        this.filters = filters;
        this.plugin = plugin;
    }

    @Override
    public void doFilter(HttpRequest req, HttpResponse res) throws IOException {
        if (index < filters.size()) {
            Filter next = filters.get(index++);
            next.doFilter(req, res, this);
        } else {
            plugin.handle(req, res);
        }
    }
}
