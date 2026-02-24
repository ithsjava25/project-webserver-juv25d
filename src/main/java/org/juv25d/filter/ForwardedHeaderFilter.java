package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

@Global(order = 0)
public class ForwardedHeaderFilter implements Filter {

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
