package org.juv25d.config;

import java.util.Map;

public class FilterConfig {

    private final Map<String, String> params;

    public FilterConfig(Map<String, String> params) {
        this.params = params;
    }

    public String get(String key) {
        return params.get(key);
    }
}
