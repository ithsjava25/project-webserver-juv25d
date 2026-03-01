package org.juv25d.config;

import org.juv25d.util.ConfigLoader;

public class BodySizeConfig {

    private final long maxSizeMb;
    private final boolean enabled;

    public BodySizeConfig() {
        ConfigLoader config = ConfigLoader.getInstance();
        this.enabled = config.isBodySizeEnabled();
        this.maxSizeMb = config.getMaxBodySizeMb();
    }
    public boolean isEnabled() {
        return enabled;
    }

    public long getMaxSizeMb() {
        return maxSizeMb;
    }
}
