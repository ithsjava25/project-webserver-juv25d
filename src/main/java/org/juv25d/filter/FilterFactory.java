package org.juv25d.filter;

import org.juv25d.di.Container;

public class FilterFactory {

    private final Container container;

    public FilterFactory(Container container) {
        this.container = container;
    }

    public Filter create(Class<?> clazz) {
        return (Filter) container.get(clazz);
    }
}
