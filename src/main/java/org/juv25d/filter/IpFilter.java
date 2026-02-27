package org.juv25d.filter;

import org.apache.commons.net.util.SubnetUtils;
import org.juv25d.config.IpFilterConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * IP-based access control filter that allows or denies HTTP requests based on client IP addresses.
 */
@Global(order = 2)
public class IpFilter implements Filter {

    private static final Logger logger = ServerLogging.getLogger();

    private final Set<String> whitelist = ConcurrentHashMap.newKeySet();
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    private final Map<String, SubnetUtils> whitelistSubnets = new ConcurrentHashMap<>();
    private final Map<String, SubnetUtils> blacklistSubnets = new ConcurrentHashMap<>();

    private final boolean allowByDefault;
    private final boolean trustProxyHeaders;

    /**
     * Constructs an IP filter with specified whitelist, blacklist, and default policy.
     * This constructor sets {@code trustProxyHeaders = false}
     * <p>
     * To specify proxy trusting use {@link #IpFilter(Set, Set, boolean, boolean)}
     *
     * @param whitelist     set of IPs/CIDR ranges to always allow (can be null)
     * @param blacklist     set of IPs/CIDR ranges to always block (can be null)
     * @param allowByDefault whether to allow IPs not in either list
     * */
    public IpFilter(@Nullable Set<String> whitelist, @Nullable Set<String> blacklist, boolean allowByDefault) {
        if (whitelist != null) {
            for (String entry : whitelist) {
                addToWhitelist(entry);
            }
        }
        if (blacklist != null) {
            for (String entry : blacklist) {
                addToBlacklist(entry);
            }
        }
        this.allowByDefault = allowByDefault;
        this.trustProxyHeaders = false;
    }

    /**
     * Constructs an IP filter with specified whitelist, blacklist, and default policy.
     *
     * @param whitelist     set of IPs/CIDR ranges to always allow (can be null)
     * @param blacklist     set of IPs/CIDR ranges to always block (can be null)
     * @param allowByDefault whether to allow IPs not in either list
     * @param trustProxyHeaders whether to trust proxy headers when extracting IP from request
     * */
    public IpFilter(@Nullable Set<String> whitelist, @Nullable Set<String> blacklist, boolean allowByDefault, boolean trustProxyHeaders) {
        if (whitelist != null) {
            for (String entry : whitelist) {
                addToWhitelist(entry);
            }
        }
        if (blacklist != null) {
            for (String entry : blacklist) {
                addToBlacklist(entry);
            }
        }
        this.allowByDefault = allowByDefault;
        this.trustProxyHeaders = trustProxyHeaders;
    }

    /**
     * Constructs an IP filter using configuration from {@link IpFilterConfig}.
     */
    public IpFilter() {
        IpFilterConfig config = new IpFilterConfig();
        for (String entry : config.whitelist()) {
            addToWhitelist(entry);
        }
        for (String entry : config.blacklist()) {
            addToBlacklist(entry);
        }
        this.allowByDefault = config.allowByDefault();
        this.trustProxyHeaders = config.trustProxyHeaders();
    }

    /**
     * Adds an IP address or CIDR range to the whitelist.
     *
     * @param ipOrCidr IP address (e.g., "192.168.1.1") or CIDR range (e.g., "10.0.0.0/8")
     */
    public void addToWhitelist(String ipOrCidr) {
        if (ipOrCidr == null || ipOrCidr.isBlank()) return;
        ipOrCidr = ipOrCidr.trim();

        if (ipOrCidr.contains("/")) {
            try {
                SubnetUtils subnet = new SubnetUtils(ipOrCidr);
                subnet.setInclusiveHostCount(true);
                whitelistSubnets.put(ipOrCidr, subnet);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid CIDR format for whitelist: " + ipOrCidr + ": " + e.getMessage());
            }
        } else {
            whitelist.add(ipOrCidr);
        }
    }

    /**
     * Adds an IP address or CIDR range to the blacklist.
     *
     * @param ipOrCidr IP address (e.g., "192.168.1.1") or CIDR range (e.g., "10.0.0.0/8")
     */
    public void addToBlacklist(String ipOrCidr) {
        if (ipOrCidr == null || ipOrCidr.isBlank()) return;
        ipOrCidr = ipOrCidr.trim();

        if (ipOrCidr.contains("/")) {
            try {
                SubnetUtils subnet = new SubnetUtils(ipOrCidr);
                subnet.setInclusiveHostCount(true);
                blacklistSubnets.put(ipOrCidr, subnet);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid CIDR format for blacklist: " + ipOrCidr + ": " + e.getMessage());
            }
        } else {
            blacklist.add(ipOrCidr);
        }
    }

    /**
     * Removes an IP address or CIDR range from the whitelist.
     * <p>
     * The entry must match exactly (same format and value) to be removed.
     * Removing a CIDR range does not affect individual IPs within that range that were
     * added separately.
     *
     * @param ipOrCidr IP address or CIDR range to remove (must match exactly)
     * <p>
     * {@link #getWhitelistIps()}
     * {@link #getWhitelistSubnets()}
     */
    public void removeFromWhitelist(String ipOrCidr) {
        if (ipOrCidr == null || ipOrCidr.isBlank()) return;
        ipOrCidr = ipOrCidr.trim();

        if (ipOrCidr.contains("/")) {
            whitelistSubnets.remove(ipOrCidr);
        } else {
            whitelist.remove(ipOrCidr);
        }
    }

    /**
     * Removes an IP address or CIDR range from the blacklist.
     * <p>
     * The entry must match exactly (same format and value) to be removed.
     * Removing a CIDR range does not affect individual IPs within that range that were
     * added separately.
     *
     * @param ipOrCidr IP address or CIDR range to remove (must match exactly)
     * <p>
     * {@link #getBlacklistIps()}
     * {@link #getBlacklistSubnets()}
     */
    public void removeFromBlacklist(String ipOrCidr) {
        if (ipOrCidr == null || ipOrCidr.isBlank()) return;
        ipOrCidr = ipOrCidr.trim();

        if (ipOrCidr.contains("/")) {
            blacklistSubnets.remove(ipOrCidr);
        } else {
            blacklist.remove(ipOrCidr);
        }
    }

    /**
     * Filters an HTTP request based on the client's IP address.
     * <p>
     * <strong>Decision Logic:</strong>
     * <ol>
     *   <li>If IP is in whitelist → Allow (even if also in blacklist)</li>
     *   <li>If IP is in blacklist → Block</li>
     *   <li>Otherwise → Use default policy (allowByDefault)</li>
     * </ol>
     *
     * @param req   the HTTP request
     * @param res   the HTTP response
     * @param chain the filter chain to continue if allowed
     * @throws IOException if an I/O error occurs during filtering
     */
    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        String clientIp = getClientIp(req);

        if (isAllowed(clientIp)) {
            chain.doFilter(req, res);
        } else {
            logger.fine("IP blocked: " + clientIp);
            forbidden(res, clientIp);
        }
    }

    /**
     * Checks if an IP address should be allowed.
     * <p>
     * <strong>Decision Logic:</strong>
     * <ol>
     *   <li>Null or blank IPs → Deny</li>
     *   <li>Whitelist (exact or subnet match) → Allow</li>
     *   <li>Blacklist (exact or subnet match) → Deny</li>
     *   <li>Not in either list → Use default policy</li>
     * </ol>
     *
     * @param ip the IP address to check
     * @return true if the IP should be allowed, false otherwise
     */
    public boolean isAllowed(String ip) {
        if (ip == null || ip.isBlank()) {
            logger.finer("Null or blank IP address, denying access");
            return false;
        }

        // Whitelist prio
        if (whitelist.contains(ip) || isInSubnets(ip, whitelistSubnets)) return true;

        if (blacklist.contains(ip) || isInSubnets(ip, blacklistSubnets)) return false;

        return allowByDefault;
    }

    /**
     * Checks if an IP address falls within any of the given subnets.
     *
     * @param ip      the IP address to check
     * @param subnets map of CIDR notations to SubnetUtils instances
     * @return true if the IP is within any subnet, false otherwise
     */
    private boolean isInSubnets(String ip, Map<String, SubnetUtils> subnets) {
        for (SubnetUtils subnet : subnets.values()) {
            try {
                if (subnet.getInfo().isInRange(ip)) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                logger.finer("Invalid IP format for range check: " + ip);
            }
        }
        return false;
    }

    /**
     * Extracts the client's IP address from the request.
     * <p>
     * <strong>Security Note:</strong> Proxy headers (X-Forwarded-For, X-Real-IP) can be
     * spoofed by clients. Only enable {@code trustProxyHeaders} when deployed behind a
     * trusted reverse proxy or load balancer that strips/overwrites these headers.
     * <p>
     * When {@code trustProxyHeaders = true}, checks headers in this order:
     * <ol>
     *   <li>{@code X-Forwarded-For} - takes first IP in comma-separated list</li>
     *   <li>{@code X-Real-IP} - single IP value</li>
     *   <li>Direct connection IP from {@code req.remoteIp()}</li>
     * </ol>
     * When {@code trustProxyHeaders = false}, always uses {@code req.remoteIp()}.
     *
     * @param req the HTTP request
     * @return the client's IP address
     */
    private String getClientIp(HttpRequest req) {
        if (!trustProxyHeaders) {
            return req.remoteIp();
        }

        Map<String, String> headers = req.headers();

        String ip = getHeaderIgnoreCase(headers, "X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        ip = getHeaderIgnoreCase(headers, "X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }

        return req.remoteIp();
    }

    /**
     * Retrieves a header value using case-insensitive name matching.
     *
     * @param headers the header map to search
     * @param name    the header name
     * @return the header value, or {@code null} if not found
     */
    private @Nullable String getHeaderIgnoreCase(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Sends a 403 Forbidden response to the client.
     *
     * @param res the HTTP response
     * @param ip  the blocked IP address
     */
    private void forbidden(HttpResponse res, String ip) {
        byte[] body = ("403 Forbidden: IP not allowed (" + ip + ")\n")
            .getBytes(StandardCharsets.UTF_8);

        res.setStatusCode(403);
        res.setStatusText("Forbidden");
        res.setHeader("Content-Type", "text/plain; charset=utf-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setBody(body);
    }

    /**
     * Returns an immutable copy of the exact IP whitelist (excludes subnets).
     *
     * @return immutable set of whitelisted IP addresses
     */
    public Set<String> getWhitelistIps() {
        return Set.copyOf(whitelist);
    }

    /**
     * Returns an immutable copy of the exact IP blacklist (excludes subnets).
     *
     * @return immutable set of blacklisted IP addresses
     */
    public Set<String> getBlacklistIps() {
        return Set.copyOf(blacklist);
    }

    /**
     * Returns an immutable copy of the whitelisted CIDR ranges.
     *
     * @return immutable set of whitelisted CIDR notations (e.g., "10.0.0.0/8")
     */
    public Set<String> getWhitelistSubnets() {
        return Set.copyOf(whitelistSubnets.keySet());
    }

    /**
     * Returns an immutable copy of the blacklisted CIDR ranges.
     *
     * @return immutable set of blacklisted CIDR notations (e.g., "192.168.0.0/16")
     */
    public Set<String> getBlacklistSubnets() {
        return Set.copyOf(blacklistSubnets.keySet());
    }

    /**
     * Returns the default policy for IPs not in either list.
     *
     * @return true if unknown IPs are allowed, false if denied
     */
    public boolean getAllowByDefault() {
        return allowByDefault;
    }

    /**
     * Returns the default policy for trusting proxy headers.
     *
     * @return true if proxy headers are trusted, false if not
     */
    public boolean getTrustProxyHeaders() {
        return trustProxyHeaders;
    }
}
