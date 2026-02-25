package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.util.ConfigLoader;

import java.io.IOException;
import java.util.List;

// Order is set to default / 0 until the filter order is finalized.
@Global(order = 0)
public class ForwardedHeaderFilter implements Filter {

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        List<String> trustedProxies = ConfigLoader.getInstance().getTrustedProxies();

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

        for (int i = parts.length - 1; i >= 0; i--) {
            String candidate = parts[i].trim();

            if (trustedProxies.contains(candidate) && i > 0) {
                return parts[i - 1].trim();
            }

        }
        // fallback: return the first element
        return parts[0].trim();
    }
}
