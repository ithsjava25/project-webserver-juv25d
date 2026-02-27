package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;
import org.juv25d.util.ConfigLoader;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Global(order = 1)
public class TimeoutFilter implements Filter {

    private static final long TIMEOUT_MS;
    static {
        // Load from config, with 2000ms as default
        TIMEOUT_MS = ConfigLoader.getInstance().getTimeoutMs(2000);
    }

    private static final ExecutorService executor =
        Executors.newCachedThreadPool();

    private static final Logger logger = ServerLogging.getLogger();

    @Override
    public void doFilter(HttpRequest req,
                         HttpResponse res,
                         FilterChain chain) throws IOException {

        logger.info("TimeoutFilter START for " + req.path());

        Future<?> future = executor.submit(() -> {
            try {
                chain.doFilter(req, res);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            logger.info("TimeoutFilter COMPLETED normally for " + req.path());

        } catch (TimeoutException e) {

            logger.warning("Timeout triggered for " + req.path());

            future.cancel(true);

            res.setStatusCode(504);
            res.setStatusText("Gateway Timeout");
            res.setBody("504 - Gateway Timeout".getBytes());

        } catch (Exception e) {

            future.cancel(true);
            throw new RuntimeException(e);
        }
    }
}
