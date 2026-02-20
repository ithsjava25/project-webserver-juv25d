package org.juv25d.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * A filter that implements rate limiting for incoming HTTP requests.
 * It uses a token bucket algorithm via Bucket4J to limit the number of requests per client IP.
 */
@Global(order = 3)
public class RateLimitingFilter implements Filter {

    private static final Logger logger = ServerLogging.getLogger();

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final long capacity;
    private final long refillTokens;
    private final Duration refillPeriod;

    /**
     * Constructs a new RateLimitingFilter.
     *
     * @param requestsPerMinute the number of requests allowed per minute for each IP
     * @param burstCapacity the maximum number of requests that can be handled in a burst
     * @throws IllegalArgumentException if requestsPerMinute or burstCapacity is not positive
     */
    public RateLimitingFilter(long requestsPerMinute, long burstCapacity) {
        if (requestsPerMinute <= 0) {
            throw new IllegalArgumentException("requestsPerMinute must be positive");
        }
        if (burstCapacity <= 0) {
            throw new IllegalArgumentException("burstCapacity must be positive");
        }

        this.capacity = burstCapacity;
        this.refillTokens = requestsPerMinute;
        this.refillPeriod = Duration.ofMinutes(1);

        logger.info(String.format(
            "RateLimitingFilter initialized - Limit: %d req/min, Burst: %d",
            requestsPerMinute, burstCapacity
        ));
    }

    /**
     * Applies the rate limiting logic to the incoming request.
     * If the rate limit is exceeded, a 429 Too Many Requests response is sent.
     *
     * @param req   the HTTP request
     * @param res   the HTTP response
     * @param chain the filter chain
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        String clientIp = getClientIp(req);

        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            logRateLimitExceeded(clientIp, req.method(), req.path());
            sendTooManyRequests(res, clientIp);
        }
    }

    private String getClientIp(HttpRequest req) {
        return req.remoteIp();
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
            capacity,
            Refill.intervally(refillTokens, refillPeriod));

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Returns the number of currently tracked IP addresses.
     *
     * @return the number of tracked IP addresses
     */
    public int getTrackedIpCount() {
        return buckets.size();
    }

    private void logRateLimitExceeded(String ip, String method, String path) {
        logger.warning(String.format(
            "Rate limit exceeded - IP: %s, Method: %s, Path: %s",
            ip, method, path
        ));
    }

    private void sendTooManyRequests(HttpResponse res, String ip) {
        byte[] body = ("429 Too Many Requests: Rate limit exceeded for IP " + ip + "\n")
            .getBytes(StandardCharsets.UTF_8);

        res.setStatusCode(429);
        res.setStatusText("Too Many Requests");
        res.setHeader("Content-Type", "text/plain; charset=utf-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setHeader("Retry-After", "60");
        res.setBody(body);
    }

    /**
     * Clears all tracked rate limiting buckets.
     */
    @Override
    public void destroy() {
        buckets.clear();
    }
}
