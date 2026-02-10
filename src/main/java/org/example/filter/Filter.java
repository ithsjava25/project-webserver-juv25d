package org.example.filter;

import org.example.http.HttpRequest;
import org.example.http.HttpResponse;

import java.io.IOException;

public interface Filter {
    default void init() {}
    void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException;
    default void destroy() {}
}
