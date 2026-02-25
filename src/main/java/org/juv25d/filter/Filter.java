package org.juv25d.filter;

import org.juv25d.config.FilterConfig;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

public interface Filter {
    default void init(FilterConfig filterConfig) {}
    void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException;
    default void destroy() {}
}
