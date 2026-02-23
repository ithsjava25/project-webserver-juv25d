package org.juv25d.filter;

import org.juv25d.config.FilterConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.filter.annotation.Route;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Scans a given package for {@link Filter} implementations and registers them
 * in the provided {@link FilterRegistry}.
 *
 * This scanner detects filters based on annotations:</p>
 *
 *   {@link Global} – registers the filter as a global filter
 *   {@link Route} – registers the filter for specific route patterns
 *
 * Each discovered filter is instantiated using the {@link FilterFactory},
 * initialized with a {@link FilterConfig}, and then registered with the registry.
 *
 * Invalid or failing filters are skipped, and errors are logged to stderr.
 */
public class FilterScanner {

    /**
     * Scans the given package for filters and registers them.
     *
     * @param packageName the base package to scan (e.g. {@code org.example})
     * @param registry the registry where discovered filters will be registered
     * @param factory factory used to create filter instances (supports DI)
     */
    public static void scan(
        String packageName,
        FilterRegistry registry,
        FilterFactory factory
    ) {

        Set<Class<?>> classes = ClassScanner.findClasses(packageName);

        for (Class<?> filterClass : classes) {

            // Only consider concrete Filter implementations
            if (!Filter.class.isAssignableFrom(filterClass)) continue;
            if (Modifier.isAbstract(filterClass.getModifiers())) continue;

            try {
                boolean isGlobal = filterClass.isAnnotationPresent(Global.class);
                boolean isRoute  = filterClass.isAnnotationPresent(Route.class);

                // Skip classes without relevant annotations
                if (!isGlobal && !isRoute) continue;
                if (isGlobal && isRoute) {
                    System.err.println("Filter " + filterClass.getName()
                        + " has both `@Global` and `@Route` — skipping. Use only one.");
                    continue;
                }
                Filter filter = factory.create(filterClass);

                Map<String, String> params = new HashMap<>();

                // Handle global filters
                if (isGlobal) {
                    Global g = filterClass.getAnnotation(Global.class);
                    params.put("scope", "global");
                    params.put("order", String.valueOf(g.order()));

                    filter.init(new FilterConfig(params));
                    registry.registerGlobal(filter, g.order());
                }

                // Handle route-specific filters
                if (isRoute) {
                    Route r = filterClass.getAnnotation(Route.class);

                    params.put("scope", "route");
                    params.put("patterns", String.join(",", r.value()));
                    params.put("order", String.valueOf(r.order()));

                    filter.init(new FilterConfig(params));

                    for (String pattern : r.value()) {
                        registry.registerRoute(filter, r.order(), pattern);
                    }
                }

            } catch (Exception e) {
                // Fail-safe: skip problematic filters but continue scanning
                System.err.println("Skipping filter: "
                    + filterClass.getName() + " due to " + e.getMessage());
            }
        }
    }
}
