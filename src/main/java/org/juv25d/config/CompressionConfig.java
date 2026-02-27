package org.juv25d.config;

import org.juv25d.util.ConfigLoader;

public class CompressionConfig {
    private final boolean enabled;
    private final int minCompressSize;

    public CompressionConfig() {
        ConfigLoader config = ConfigLoader.getInstance();
        this.enabled = config.isCompressionEnabled();
        this.minCompressSize = config.getMinCompressSize();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMinCompressSize() {
        return minCompressSize;
    }
}
