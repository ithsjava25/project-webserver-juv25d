package org.juv25d.router;

import org.juv25d.di.Inject;
import org.juv25d.plugin.NotFoundPlugin;
import org.juv25d.plugin.StaticFilesPlugin;

public class RouterConfig {

    @Inject
    public RouterConfig(SimpleRouter router) {

        router.registerPlugin("/", new StaticFilesPlugin());
        router.registerPlugin("/*", new StaticFilesPlugin());
        router.registerPlugin("/notfound", new NotFoundPlugin());

        System.out.println("Router configured");
    }
}
