package org.juv25d.config;

import java.util.Set;

public class IpFilterConfig {

    public Set<String> whitelist() {
        return Set.of();
    }

    public Set<String> blacklist() {
        return Set.of();
    }

    private final boolean allowByDefault = true;

    private final boolean trustProxyHeaders = false;

    public boolean allowByDefault() {
        return allowByDefault;
    }

    public boolean trustProxyHeaders() {
        return trustProxyHeaders;
    }
}
