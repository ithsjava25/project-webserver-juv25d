package org.juv25d;

import org.juv25d.di.Container;
import org.juv25d.filter.*;
import org.juv25d.router.Router;

import java.util.List;
import java.util.logging.Logger;

public class Bootstrap {

    private static final Logger logger = Logger.getLogger(Bootstrap.class.getName());

    public static Pipeline init(Container container, String basePackage) {

        FilterRegistry registry = new FilterRegistry();
        FilterFactory factory = new FilterFactory(container);

        FilterScanner.scan(basePackage, registry, factory);

        logger.info("Filters initialized:");
        logger.info("  Global: " + registry.getGlobalFilters().stream()
            .sorted()
            .map(fr -> fr.filter().getClass().getSimpleName())
            .toList());
        logger.info("  Route-specific: " + registry.getRouteFilters().values().stream()
            .flatMap(List::stream)
            .distinct()
            .sorted()
            .map(fr -> fr.filter().getClass().getSimpleName())
            .toList());

        // Resolve base router and configure routes
        Router baseRouter = container.get(Router.class);
        container.get(org.juv25d.router.RouterConfig.class);

        // Use the base router directly. BasicAuthPlugin is now activated like other plugins
        // via RouterConfig (e.g., on specific paths such as "/login").
        return new Pipeline(new FilterMatcher(registry), baseRouter);
    }
}

