package org.juv25d.filter;

import org.juv25d.config.CompressionConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.logging.ServerLogging;

import java.util.logging.Logger;


@Global(order = 10)
public class CompressionFilter {
    private static final Logger LOGGER = ServerLogging.getLogger();

    private final boolean enabled;
    private final int minCompressSize;

    public CompressionFilter() {
        CompressionConfig config = new CompressionConfig();
        this.enabled = config.isEnabled();
        this.minCompressSize = config.getMinCompressSize();
    }

    public CompressionFilter(boolean enabled, int minCompressSize) {
        this.enabled = enabled;
        this.minCompressSize = minCompressSize;
    }

}
