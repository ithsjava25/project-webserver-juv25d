package org.juv25d;

import org.juv25d.Server.Server;
import org.juv25d.Server.ServerBuilder;
import org.juv25d.filter.config.FilterConfiguration;
import org.juv25d.logging.ServerLogging;
import org.juv25d.plugin.NotFoundPlugin;
import org.juv25d.plugin.StaticFilesPlugin;
import org.juv25d.router.SimpleRouter;
import org.juv25d.util.ConfigLoader;

import java.util.logging.Logger;

public class App {

    public static void main(String[] args) {

        ConfigLoader config = ConfigLoader.getInstance();
        Logger logger = ServerLogging.getLogger();

        SimpleRouter router = new SimpleRouter();
        router.registerPlugin("/", new StaticFilesPlugin());
        router.registerPlugin("/*", new StaticFilesPlugin());
        router.registerPlugin("/notfound", new NotFoundPlugin());

        ServerBuilder builder = new ServerBuilder()
            .port(config.getPort())
            .logger(logger)
            .router(router);

        FilterConfiguration.configure(builder, config);

        Server server = builder.build();
        server.start();
    }
}
