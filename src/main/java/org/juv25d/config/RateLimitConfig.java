package org.juv25d.config;

import org.juv25d.util.ConfigLoader;

public class RateLimitConfig {

    private final long rpm;
    private final long burst;
    private final boolean enabled;

    public RateLimitConfig() {
        ConfigLoader config = ConfigLoader.getInstance();
        this.rpm = config.getRequestsPerMinute();
        this.burst = config.getBurstCapacity();
        this.enabled = config.isRateLimitingEnabled();
    }

    public long rpm() { return rpm; }
    public long burst() { return burst; }
    public boolean isEnabled() { return enabled; }
}
