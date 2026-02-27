package org.juv25d.filter;

import org.juv25d.config.CompressionConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.logging.ServerLogging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


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

    private boolean acceptsGzip(HttpRequest req) {
        String acceptEncoding = req.headers().get("Accept-Encoding");
        if (acceptEncoding == null || acceptEncoding.isEmpty()) {
            return false;
        }
        return Arrays.stream(acceptEncoding.split(","))
            .map(String::trim)
            .anyMatch(e -> e.equalsIgnoreCase("gzip"));
    }

    private byte [] compress(byte [] data) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipstream = new GZIPOutputStream(byteStream);

        gzipstream.write(data);
        gzipstream.close();
        return byteStream.toByteArray();
    }
}
