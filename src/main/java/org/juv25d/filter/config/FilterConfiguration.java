package org.juv25d.filter.config;

import org.juv25d.Server.ServerBuilder;
import org.juv25d.filter.*;
import org.juv25d.util.ConfigLoader;

import java.util.List;
import java.util.Set;

/**
 * Configures and registers HTTP filters for the server.
 * The order of registration defines the execution order in the pipeline.
 */
public final class FilterConfiguration {

    private FilterConfiguration() {}

    /**
     * Adds and configures all filters on the provided {@link ServerBuilder}.
     *
     * @param builder the server builder used to register filters
     * @param config  the configuration source for conditional filters
     */
    public static void configure(ServerBuilder builder, ConfigLoader config) {

        List<RedirectRule> redirectRules = List.of(
            new RedirectRule("/old-page", "/new-page", 301),
            new RedirectRule("/temp", "https://example.com/temporary", 302),
            new RedirectRule("/docs/*", "/documentation/", 301)
        );

        builder
            .addFilter(new RedirectFilter(redirectRules))
            .addFilter(new IpFilter(Set.of(), Set.of()))
            .addFilter(new LoggingFilter())
            .addFilterIf(
                config.isRateLimitingEnabled(),
                () -> new RateLimitingFilter(
                    config.getRequestsPerMinute(),
                    config.getBurstCapacity()
                )
            );
    }
}
