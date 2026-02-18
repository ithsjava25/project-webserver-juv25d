package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.filter.annotation.Route;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;


class FilterScannerTest {


    @Global(order = 5)
    static class GlobalTestFilter implements Filter {
        public void init() {}
        public void doFilter(
            HttpRequest req,
            HttpResponse res,
            FilterChain chain) {}
        public void destroy() {}
    }

    @Route(value = {"/users", "/admin"}, order = 3)
    static class RouteTestFilter implements Filter {
        public void init() {}
        public void doFilter(
            HttpRequest req,
            HttpResponse res,
            FilterChain chain) {}
        public void destroy() {}
    }

    @Global(order = 1)
    @Route(value = {"/test"}, order = 1)
    static class InvalidAnnotatedFilter implements Filter {
        public void init() {}
        public void doFilter(
            HttpRequest req,
            HttpResponse res,
            FilterChain chain) {}
        public void destroy() {}
    }
}
