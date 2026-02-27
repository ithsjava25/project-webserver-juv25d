package org.juv25d.config;

public class CompressionConfig {
    private final boolean enabled;
    private final int minCompressSize;

    public CompressionConfig() {
        this.enabled = false;
        this.minCompressSize = 1024;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMinCompressSize() {
        return minCompressSize;
    }
}
