package org.juv25d.filter;

import io.github.bucket4j.Bucket;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitingFilter implements Filter {

    // Thread-safe map storing one bucket per IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final long capacity;          // Maximum tokens in the bucket (burst capacity)
    private final long refillTokens;      // How many tokens to refill
    private final Duration refillPeriod;  // How often to refill

    public RateLimitingFilter(long requestsPerMinute, long burstCapacity) {
        this.capacity = burstCapacity;
        this.refillTokens = requestsPerMinute;
        this.refillPeriod = Duration.ofMinutes(1);
    }

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
    }
}
