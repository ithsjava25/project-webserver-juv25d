package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.util.ConfigLoader;

import java.io.IOException;
import java.util.List;

/**
 * Resolves the real client IP when requests pass through reverse proxies.
 *
 * <p>Reads the <b>X-Forwarded-For</b> header and replaces the request's
 * remote IP with the first non-trusted address. Trusted proxy IPs are
 * configured via {@link ConfigLoader}.</p>
 *
 * <p>This prevents IP spoofing and ensures downstream filters (e.g. rate
 * limiting) see the correct client address.</p>
 *
 * <p>If the header is missing, the original remote IP is used.</p>
 */

// Order is set to default / 0 until the filter order is finalized.
@Global(order = 0)
public class ForwardedHeaderFilter implements Filter {

    private final ConfigLoader configLoader;

    public ForwardedHeaderFilter() {
        this(ConfigLoader.getInstance());
    }

    ForwardedHeaderFilter(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        List<String> trustedProxies = configLoader.getTrustedProxies();

        String forwardedFor = req.headers().get("X-Forwarded-For");

        if (forwardedFor == null || forwardedFor.isBlank()) {
            chain.doFilter(req, res);
            return;
        }

        String resolvedIp = resolveFromHeader(forwardedFor, trustedProxies);

        HttpRequest newReq = new HttpRequest(
            req.method(),
            req.path(),
            req.queryString(),
            req.httpVersion(),
            req.headers(),
            req.body(),
            resolvedIp,
            req.creationTimeNanos()
        );
        chain.doFilter(newReq, res);
    }

    private String resolveFromHeader(String header, List<String> trustedProxies) {
        String[] parts = header.split(",");

        if (trustedProxies == null || trustedProxies.isEmpty()) {
            return parts[0].trim();
        }

        int i = parts.length - 1;

        while (i >= 0 && trustedProxies.contains(parts[i].trim())) {
            i--;
        }

        if (i >= 0) {
            return parts[i].trim();
        }
        return parts[0].trim();
    }
}
