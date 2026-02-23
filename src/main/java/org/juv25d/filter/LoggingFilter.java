package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import java.io.IOException;
import java.util.logging.Logger;

@Global(order = 1)
public class LoggingFilter implements Filter {
    private static final Logger logger = ServerLogging.getLogger();

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        logger.info(req.method() + " " + req.path());
        chain.doFilter(req, res);
    }
}
