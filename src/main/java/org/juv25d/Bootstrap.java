package org.juv25d;

import org.juv25d.di.Container;
import org.juv25d.filter.*;
import org.juv25d.router.Router;

public class Bootstrap {

    public static Pipeline init(Container container, String basePackage) {

        FilterRegistry registry = new FilterRegistry();
        FilterFactory factory = new FilterFactory(container);

        FilterScanner.scan(basePackage, registry, factory);

        FilterMatcher matcher = new FilterMatcher(registry);

        Router router = container.get(Router.class);

        container.get(org.juv25d.router.RouterConfig.class);

        return new Pipeline(matcher, router);
    }
}
