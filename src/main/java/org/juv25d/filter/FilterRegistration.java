package org.juv25d.filter;

import org.jspecify.annotations.Nullable;

public record FilterRegistration(Filter filter, int order, @Nullable String pattern)
    implements Comparable<FilterRegistration> {

    @Override
    public int compareTo(FilterRegistration o) {
        return Integer.compare(this.order, o.order);
    }
}