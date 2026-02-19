package org.juv25d.filter;

import org.juv25d.Server.Pipeline;
import org.juv25d.filter.annotation.Global;
import org.juv25d.filter.annotation.Route;

public class FilterScanner {

    public static void register(Object instance, Pipeline pipeline) {
        Class<?> filterClass = instance.getClass();

        if (!Filter.class.isAssignableFrom(filterClass)) {
            return;
        }

        Global global = filterClass.getAnnotation(Global.class);
        Route route = filterClass.getAnnotation(Route.class);

        if (global != null && route != null) {
            throw new IllegalStateException(
                filterClass.getName() +
                    "cannot be annotated with both @Global and @Route"
            );
        }

        if (global != null) {
            pipeline.addGlobalFilter((Filter) instance, global.order());
            return;
        }

        if (route != null) {
            for (String pattern : route.value()) {
                pipeline.addRouteFilter(
                    (Filter) instance,
                    route.order(),
                    pattern
                );
            }
            return;
        }
        throw new IllegalStateException(
            filterClass.getName() + "must be annotated with either @Global or @Route"
        );
    }
}
