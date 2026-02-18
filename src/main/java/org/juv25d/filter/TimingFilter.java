package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import java.io.IOException;
import java.util.logging.Logger;

public class TimingFilter implements Filter {

    private static final Logger logger = ServerLogging.getLogger();

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        long start = System.nanoTime();

        chain.doFilter(req, res);

        long durationMs = (System.nanoTime() - start) / 1_000_000;
        logger.info(req.method() + " " + req.path() + " took " + durationMs + " ms");
    }
}
