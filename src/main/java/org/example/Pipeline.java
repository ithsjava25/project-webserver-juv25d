package org.example;

import org.example.filter.Filter;
import org.example.filter.FilterChain;
import org.example.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Pipeline {
    private final List<Filter> filters = new ArrayList<>();
    private Plugin plugin;

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public FilterChain createChain() {
        return new FilterChain(filters, plugin);
    }

    public void init() {filters.forEach(Filter::init);}

    public void destroy() {
        filters.forEach(Filter::destroy);}
}

