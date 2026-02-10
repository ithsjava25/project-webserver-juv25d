package org.juv25d;

import org.juv25d.filter.Filter;
import org.juv25d.filter.FilterChainImpl;
import org.juv25d.plugin.Plugin;

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

    public FilterChainImpl createChain() {
        return new FilterChainImpl(List.copyOf(filters), plugin);
    }

    public void init() {
        filters.forEach(Filter::init);
    }

    public void destroy() {
        filters.forEach(Filter::destroy);
    }
}
