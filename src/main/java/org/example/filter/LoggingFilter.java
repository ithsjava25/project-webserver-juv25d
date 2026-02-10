package org.example.filter;

import org.example.http.HttpRequest;
import org.example.http.HttpResponse;

import java.io.IOException;

public class LoggingFilter implements Filter {

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        System.out.println(req.method() + " " + req.path());
        chain.doFilter(req, res);
    }
}
