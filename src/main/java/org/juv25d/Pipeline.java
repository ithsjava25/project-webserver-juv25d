package org.juv25d;

import org.juv25d.filter.Filter;
import org.juv25d.filter.FilterChainImpl;
import org.juv25d.http.HttpRequest;
import org.juv25d.router.Router; // New import

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Pipeline {

    private final List<FilterRegistration> globalFilters = new CopyOnWriteArrayList<>();
    private final Map<String, List<FilterRegistration>> routeFilters = new ConcurrentHashMap<>();
    private volatile List<Filter> sortedGlobalFilters = List.of();
    private volatile Router router; // Changed from Plugin plugin;

    public void addGlobalFilter(Filter filter, int order) {
        globalFilters.add(new FilterRegistration(filter, order, null));
        sortedGlobalFilters = globalFilters.stream()
            .sorted()
            .map(FilterRegistration::filter)
            .collect(Collectors.toUnmodifiableList());
    }

    public void addRouteFilter(Filter filter, int order, String pattern) {
        routeFilters.computeIfAbsent(pattern, k -> new CopyOnWriteArrayList<>())
            .add(new FilterRegistration(filter, order, pattern));
    }

    public void setRouter(Router router) {
        if (router == null) {
            throw new IllegalArgumentException("Router cannot be null");
        }
        this.router = router;
    }

    public FilterChainImpl createChain(HttpRequest request) {
        List<Filter> filters = new ArrayList<>();
        filters.addAll(sortedGlobalFilters);
        String path = request.path();
        List<FilterRegistration> exactMatches = routeFilters.get(path);
        if (exactMatches != null) {
            exactMatches.stream()
                .sorted()
                .map(FilterRegistration::filter)
                .forEach(filters::add);
        }

        for (Map.Entry<String, List<FilterRegistration>> entry : routeFilters.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.endsWith("*") && path.startsWith(pattern.substring(0, pattern.length() - 1))) {
                entry.getValue().stream()
                    .sorted()
                    .map(FilterRegistration::filter)
                    .forEach(filters::add);
            }
        }
        return new FilterChainImpl(filters, router); // Pass router instead of plugin
    }

    public List<Filter> getFilters() {
        return Collections.unmodifiableList(sortedGlobalFilters);
    }

    public Router getRouter() { // Renamed from getPlugin
        return router;
    }
}
