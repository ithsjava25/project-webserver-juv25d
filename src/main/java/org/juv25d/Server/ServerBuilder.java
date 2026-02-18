package org.juv25d.Server;

import org.juv25d.DefaultConnectionHandlerFactory;
import org.juv25d.Pipeline;
import org.juv25d.filter.Filter;
import org.juv25d.filter.FilterScanner;
import org.juv25d.http.HttpParser;
import org.juv25d.router.Router;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ServerBuilder {

    private int port = 8080;
    private Logger logger;
    private Router router;

    private final Pipeline pipeline = new Pipeline();

    public ServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public ServerBuilder logger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public ServerBuilder router(Router router) {
        this.router = router;
        return this;
    }

    public ServerBuilder addFilter(Filter filter) {
        FilterScanner.register(filter, pipeline);
        return this;
    }

    public ServerBuilder addFilterIf(boolean condition, Supplier<Filter> supplier) {
        if (condition) {
            addFilter(supplier.get());
        }
        return this;
    }

    public Server build() {
        Objects.requireNonNull(logger, "Logger must be configured");
        Objects.requireNonNull(router, "Router must be configured");

        pipeline.setRouter(router);

        DefaultConnectionHandlerFactory handlerFactory =
            new DefaultConnectionHandlerFactory(new HttpParser(), logger, pipeline);

        return new Server(
            port,
            logger,
            handlerFactory,
            pipeline
        );
    }
}
