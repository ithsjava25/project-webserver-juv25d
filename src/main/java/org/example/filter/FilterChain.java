package org.example.filter;

import org.example.http.HttpRequest;
import org.example.http.HttpResponse;

import java.io.IOException;

public interface FilterChain {
    void doFilter(HttpRequest req, HttpResponse res) throws IOException;
}
