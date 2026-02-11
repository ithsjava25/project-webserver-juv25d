package org.juv25d;

import org.juv25d.filter.Filter;
import org.juv25d.filter.FilterChainImpl;
import org.juv25d.http.HttpRequest;
import org.juv25d.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Pipeline {
    private final List<FilterRegistration> globalFilters = new ArrayList<>();
    private Plugin plugin;

    public void addGlobalFilter(Filter filter, int order) {
        globalFilters.add(new FilterRegistration(filter, order, null));
    }

    public void setPlugin(Plugin plugin) {this.plugin = plugin;}

    public FilterChainImpl createChain(HttpRequest request) {
        List<Filter> filters = new ArrayList<>();

        globalFilters.stream()
            .sorted()
            .forEach(fr -> filters.add(fr.filter()));

        return new FilterChainImpl(filters, plugin);
    }
}
