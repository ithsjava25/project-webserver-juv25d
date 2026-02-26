package org.juv25d.filter;

import org.juv25d.config.IpFilterConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
@Global(order = 2)
public class IpFilter implements Filter {

    private final Set<String> whitelist = new HashSet<>();
    private final Set<String> blacklist = new HashSet<>();

    private final boolean allowByDefault;

    public IpFilter(@Nullable Set<String> whitelist, @Nullable Set<String> blacklist, boolean allowByDefault) {
        if (whitelist != null) {
            this.whitelist.addAll(whitelist);
        }
        if (blacklist != null) {
            this.blacklist.addAll(blacklist);
        }
        this.allowByDefault = allowByDefault;
    }

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

    public boolean isAllowed(String ip) {

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
