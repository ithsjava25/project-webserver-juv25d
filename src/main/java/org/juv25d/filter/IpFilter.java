package org.juv25d.filter;

import org.juv25d.config.IpFilterConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Global IP-based request filter.
 *
 * <p>The filter runs early in the request pipeline and determines whether a request
 * should be allowed to continue based on the client's IP address.</p>
 *
 * <p>Decision rules:</p>
 * <ul>
 *     <li>If an IP is present in the whitelist, it is allowed.</li>
 *     <li>If an IP is present in the blacklist, it is denied.</li>
 *     <li>If an IP is present in both lists, {@code allowByDefault} is used.</li>
 *     <li>If an IP is present in neither list, {@code allowByDefault} is used.</li>
 * </ul>
 *
 * <p>The default constructor loads configuration from {@link IpFilterConfig}.</p>
 */
@Global(order = 2)
public class IpFilter implements Filter {

    private final Set<String> whitelist = new HashSet<>();
    private final Set<String> blacklist = new HashSet<>();

    private final boolean allowByDefault;

    /**
     * Creates an {@code IpFilter} with explicit configuration.
     *
     * @param whitelist IP addresses that should be allowed (may be {@code null})
     * @param blacklist IP addresses that should be denied (may be {@code null})
     * @param allowByDefault fallback decision when an IP is not listed, or listed in both sets
     */

    public IpFilter(Set<String> whitelist, Set<String> blacklist,  boolean allowByDefault) {
        if (whitelist != null) {
            this.whitelist.addAll(whitelist);
        }
        if (blacklist != null) {
            this.blacklist.addAll(blacklist);
        }
        this.allowByDefault = allowByDefault;
    }

    /**
     * Creates an {@code IpFilter} using configuration loaded from {@link IpFilterConfig}
     */

    public IpFilter() {
        IpFilterConfig config = new IpFilterConfig();
        this.whitelist.addAll(config.whitelist());
        this.blacklist.addAll(config.blacklist());
        this.allowByDefault = config.allowByDefault();
    }

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        String clientIp = getClientIp(req);

        if (isAllowed(clientIp)) {
            chain.doFilter(req, res);
        } else {
            forbidden(res, clientIp);
        }
    }

    /**
     * Evaluates whether the given IP address should be allowed.
     *
     * @param ip client IP address
     * @return {@code true} if the request is allowed, {@code false} otherwise
     */

    public boolean isAllowed(String ip) {

        // If an IP exists in both lists, fall back to allowByDefault
        if (whitelist.contains(ip) && blacklist.contains(ip)) return allowByDefault;

        if (whitelist.contains(ip)) return true;

        if (blacklist.contains(ip)) return false;

        return allowByDefault;
    }

    private String getClientIp(HttpRequest req){
        return req.remoteIp();
    }

    private void forbidden(HttpResponse res, String ip) {
        byte[] body = ("403 Forbidden: IP not allowed (" + ip + ")\n")
            .getBytes(StandardCharsets.UTF_8);

        res.setStatusCode(403);
        res.setStatusText("Forbidden");
        res.setHeader("Content-Type", "text/plain; charset=utf-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setBody(body);
    }
}
