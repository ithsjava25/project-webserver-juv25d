package org.juv25d.Server;

import org.juv25d.filter.Filter;
import org.juv25d.filter.FilterChainImpl;
import org.juv25d.filter.FilterRegistration;
import org.juv25d.http.HttpRequest;
import org.juv25d.router.Router;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Pipeline {

    private final List<FilterRegistration> globalFilters = new CopyOnWriteArrayList<>();
    private final Map<String, List<FilterRegistration>> routeFilters = new ConcurrentHashMap<>();

    private volatile Router router;

    public void addGlobalFilter(Filter filter, int order) {
        globalFilters.add(new FilterRegistration(filter, order, null));
    }

    public void addRouteFilter(Filter filter, int order, String pattern) {
        routeFilters
            .computeIfAbsent(pattern, k -> new CopyOnWriteArrayList<>())
            .add(new FilterRegistration(filter, order, pattern));
    }

    public void setRouter(Router router) {
        if (router == null) {
            throw new IllegalArgumentException("Router cannot be null");
        }
        this.router = router;
    }

    public Router getRouter() {
        return router;
    }

    public FilterChainImpl createChain(HttpRequest request) {
        if (router == null) {
            throw new IllegalStateException("Router not set");
        }

        String path = request.path();

        List<FilterRegistration> collected = new ArrayList<>();

        collected.addAll(globalFilters);

        for (Map.Entry<String, List<FilterRegistration>> entry : routeFilters.entrySet()) {
            String pattern = entry.getKey();

            if (matches(pattern, path)) {
                collected.addAll(entry.getValue());
            }
        }

        Collections.sort(collected);

        List<Filter> finalFilters = new ArrayList<>();
        Set<Filter> seen = new HashSet<>();

        for (FilterRegistration reg : collected) {
            if (seen.add(reg.filter())) {
                finalFilters.add(reg.filter());
            }
        }

        return new FilterChainImpl(finalFilters, router);
    }

    private boolean matches(String pattern, String path) {
        if (pattern.equals(path)) {
            return true;
        }

        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return path.startsWith(prefix);
        }

        return false;
    }

    public void initFilters() {
        getAllFilters().forEach(Filter::init);
    }

    public void destroyFilters() {
        for (Filter filter : getAllFilters()) {
            try {
                filter.destroy();
            } catch (Exception e) {
                System.err.println(
                    "Error destroying filter " +
                        filter.getClass().getName() +
                        ": " + e.getMessage()
                );
            }
        }
    }

    public List<Filter> getAllFilters() {
        List<FilterRegistration> all = new ArrayList<>(globalFilters);

        routeFilters.values().forEach(all::addAll);

        Collections.sort(all);

        List<Filter> result = new ArrayList<>();
        Set<Filter> seen = new HashSet<>();

        for (FilterRegistration reg : all) {
            if (seen.add(reg.filter())) {
                result.add(reg.filter());
            }
        }

        return result;
    }
}
