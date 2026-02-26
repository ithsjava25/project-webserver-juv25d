package org.juv25d.config;

import java.util.Map;
import org.jspecify.annotations.Nullable;

public class FilterConfig {

    private final Map<String, String> params;

    public FilterConfig(Map<String, String> params) {
        this.params = params;
    }

    @Nullable public String get(String key) {
        return params.get(key);
    }
}
