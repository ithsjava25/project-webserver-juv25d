package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

@Global(order = 1)
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        System.out.println(req.method() + " " + req.path());
        chain.doFilter(req, res);
    }
}
