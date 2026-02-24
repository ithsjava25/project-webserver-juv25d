package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

/**
 * Global filter that resolves the real client IP address from the
 * {@code X-Forwarded-For} header.
 *
 * <p>If the header is present, the first IP address in the comma-separated
 * list is treated as the originating client IP and a new {@link HttpRequest}
 * instance is created with that IP. If the header is missing or blank, the
 * original request is forwarded unchanged.</p>
 *
 * <p>This filter is typically used when the server is running behind a
 * reverse proxy or load balancer.</p>
 */
@Global(order = 0)
public class ForwardedHeaderFilter implements Filter {

    /**
     * Extracts the client IP from the {@code X-Forwarded-For} header (if present)
     * and forwards the request through the filter chain.
     *
     * @param req   the incoming HTTP request
     * @param res   the HTTP response
     * @param chain the filter chain to continue processing
     * @throws IOException if an I/O error occurs during filtering
     */
    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        String forwardedFor = req.headers().get("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            chain.doFilter(req, res);
        } else {
            String clientIp = forwardedFor.split(",")[0];
            HttpRequest newReq = new HttpRequest(
                req.method(),
                req.path(),
                req.queryString(),
                req.httpVersion(),
                req.headers(),
                req.body(),
                clientIp,
                req.creationTimeNanos()
            );
            chain.doFilter(newReq, res);
        }
    }
}
