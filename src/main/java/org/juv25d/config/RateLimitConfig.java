package org.juv25d.config;

import org.juv25d.util.ConfigLoader;

public class RateLimitConfig {

    private final long rpm;
    private final long burst;

    public RateLimitConfig() {
        ConfigLoader config = ConfigLoader.getInstance();
        this.rpm = config.getRequestsPerMinute();
        this.burst = config.getBurstCapacity();
    }

    public long rpm() { return rpm; }
    public long burst() { return burst; }
}
