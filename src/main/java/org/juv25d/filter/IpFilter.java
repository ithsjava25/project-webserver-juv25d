package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Global(order = 2)
public class IpFilter implements Filter {

    private final Set<String> whitelist;
    private final Set<String> blacklist;

    public IpFilter(Set<String> whitelist, Set<String> blacklist) {
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        String clientIp = getClientIp(req);

        boolean allowed;
        if (whitelist != null && !whitelist.isEmpty()) {
            allowed = whitelist.contains(clientIp);
        } else if (blacklist != null && !blacklist.isEmpty()){
            allowed = !blacklist.contains(clientIp);
        } else {
            allowed = true;
        }

        if(!allowed){
            forbidden(res, clientIp);
            return;
        }
        chain.doFilter(req, res);
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
