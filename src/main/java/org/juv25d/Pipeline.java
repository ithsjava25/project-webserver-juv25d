package org.juv25d;

import org.juv25d.filter.Filter;
import org.juv25d.filter.FilterChainImpl;
import org.juv25d.filter.FilterMatcher;
import org.juv25d.http.HttpRequest;
import org.juv25d.router.Router;

import java.util.List;

public class Pipeline {

    private final FilterMatcher matcher;
    private final Router router;

    public Pipeline(FilterMatcher matcher, Router router) {
        this.matcher = matcher;
        this.router = router;
    }

    public FilterChainImpl createChain(HttpRequest request) {
        List<Filter> filters = matcher.match(request);
        return new FilterChainImpl(filters, router);
    }

    public void stop() {
        matcher.destroy();
    }
}
