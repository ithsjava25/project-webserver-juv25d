package org.juv25d.filter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FilterRegistry {

    private final List<FilterRegistration> global = new CopyOnWriteArrayList<>();
    private final Map<String, List<FilterRegistration>> routes = new ConcurrentHashMap<>();
    private volatile boolean isShutdown = false;

    public void registerGlobal(Filter filter, int order) {
        global.add(new FilterRegistration(filter, order, null));
    }

    public void registerRoute(Filter filter, int order, String pattern) {
        routes.computeIfAbsent(pattern, k -> new CopyOnWriteArrayList<>())
            .add(new FilterRegistration(filter, order, pattern));
    }

    public List<FilterRegistration> getGlobalFilters() {
        return Collections.unmodifiableList(global);
    }

    public Map<String, List<FilterRegistration>> getRouteFilters() {
        return Collections.unmodifiableMap(routes);
    }

    public void shutdown() {
        if (isShutdown) return;
        isShutdown = true;

        Set<Filter> destroyed = Collections.newSetFromMap(new IdentityHashMap<>());

        for (FilterRegistration reg : global) {
            destroyFilter(reg.filter(), destroyed);
        }

        for (List<FilterRegistration> list : routes.values()) {
            for (FilterRegistration reg : list) {
                destroyFilter(reg.filter(), destroyed);
            }
        }
    }

    private void destroyFilter(Filter filter, Set<Filter> destroyed) {
        if (destroyed.add(filter)) {
            try {
                filter.destroy();
            } catch (Exception e) {
                System.err.println("Error destroying filter "
                    + filter.getClass().getName() + ": " + e.getMessage());
            }
        }
    }
}
