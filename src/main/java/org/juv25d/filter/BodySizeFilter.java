package org.juv25d.filter;

import org.juv25d.config.BodySizeConfig;
import org.juv25d.config.FilterConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import java.io.IOException;
import java.util.logging.Logger;

@Global(order = 1)
public class BodySizeFilter implements Filter{

    private static final Logger logger = ServerLogging.getLogger();

    private final long maxSizeBytes;
    private final boolean enabled;

    public BodySizeFilter(long maxSizeMb) {
        if (maxSizeMb <= 0) {
            throw new IllegalArgumentException("maxSizeMb must be positive");
        }
        this.maxSizeBytes = maxSizeMb * 1024 * 1024;
        this.enabled = true;

        logger.info("BodySizeFilter initialized with max size: " + maxSizeMb + " MB");
    }

    public BodySizeFilter() {
        BodySizeConfig config = new BodySizeConfig();
        this.enabled = config.isEnabled();
        this.maxSizeBytes = config.getMaxSizeMb() * 1024 * 1024;
    }


    @Override
    public void init(FilterConfig filterConfig) {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        if (!enabled) {
            chain.doFilter(req, res);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
