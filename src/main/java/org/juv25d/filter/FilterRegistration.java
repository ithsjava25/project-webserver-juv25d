package org.juv25d.filter;

public record FilterRegistration(Filter filter, int order, String pattern)
    implements Comparable<FilterRegistration> {

    @Override
    public int compareTo(FilterRegistration o) {
        return Integer.compare(this.order, o.order);
    }
}
