package org.juv25d.filter;

import org.juv25d.http.HttpRequest;

import java.util.*;

public class FilterMatcher {

    private final FilterRegistry registry;

    public FilterMatcher(FilterRegistry registry) {
        this.registry = registry;
    }

    public List<Filter> match(HttpRequest request) {
        List<FilterRegistration> result = new ArrayList<>();
        result.addAll(registry.getGlobalFilters());

        String path = request.path();

        for (var entry : registry.getRouteFilters().entrySet()) {
            if (matches(entry.getKey(), path)) {
                result.addAll(entry.getValue());
            }
        }

        result.sort(Comparator.comparingInt(FilterRegistration::order));

        List<Filter> filters = new ArrayList<>();
        for (FilterRegistration reg : result) {
            filters.add(reg.filter());
        }
        return filters;
    }

    private boolean matches(String pattern, String path) {
        if (pattern.equals(path)) return true;
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return path.startsWith(prefix);
        }

        return false;
    }
}
