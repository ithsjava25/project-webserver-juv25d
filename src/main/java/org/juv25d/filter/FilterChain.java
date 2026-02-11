package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;

public interface FilterChain {
    void doFilter(HttpRequest req, HttpResponse res) throws IOException;
}
