package org.juv25d.filter;

import org.apache.commons.net.util.SubnetUtils;
import org.juv25d.config.IpFilterConfig;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Global(order = 2)
public class IpFilter implements Filter {

    private static final Logger logger = ServerLogging.getLogger();

    private final Set<String> whitelist = ConcurrentHashMap.newKeySet();
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    private final Map<String, SubnetUtils> whitelistSubnets = new ConcurrentHashMap<>();
    private final Map<String, SubnetUtils> blacklistSubnets = new ConcurrentHashMap<>();

    private final boolean allowByDefault;

    public IpFilter(Set<String> whitelist, Set<String> blacklist,  boolean allowByDefault) {
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
    }

    public IpFilter() {
        IpFilterConfig config = new IpFilterConfig();
        for (String entry : config.whitelist()) {
            addToWhitelist(entry);
        }
        for (String entry : config.blacklist()) {
            addToBlacklist(entry);
        }
        this.allowByDefault = config.allowByDefault();
    }

    public void addToWhitelist(String ipOrCidr) {
        if (ipOrCidr == null || ipOrCidr.isBlank()) return;

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

    public void addToBlacklist(String ipOrCidr) {
        if (ipOrCidr == null || ipOrCidr.isBlank()) return;

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

    public void removeFromWhitelist(String ipOrCidr) {
        if (ipOrCidr == null || ipOrCidr.isBlank()) return;

        if (ipOrCidr.contains("/")) {
            whitelistSubnets.remove(ipOrCidr);
        } else {
            whitelist.remove(ipOrCidr);
        }
    }

    public void removeFromBlacklist(String ipOrCidr) {
        if (ipOrCidr == null || ipOrCidr.isBlank()) return;

        if (ipOrCidr.contains("/")) {
            blacklistSubnets.remove(ipOrCidr);
        } else {
            blacklist.remove(ipOrCidr);
        }
    }

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        try {
            String clientIp = getClientIp(req);

            if (isAllowed(clientIp)) {
                chain.doFilter(req, res);
            } else {
                logger.fine("IP blocked: " + clientIp);
                forbidden(res, clientIp);
            }
        } catch (Exception e) {
            logger.severe("Error in IP filter: " + e.getMessage());
            forbidden(res, "error");
        }
    }

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

    private String getClientIp(HttpRequest req) {
        Map<String, String> headers = req.headers();

        String ip = headers.get("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        ip = headers.get("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }

        return req.remoteIp();
    }

    private void forbidden(HttpResponse res, String ip) {
        byte[] body = ("403 Forbidden: IP not allowed (" + ip + ")\n")
            .getBytes(StandardCharsets.UTF_8);

        res.setStatusCode(403);
        res.setStatusText("Forbidden");
        res.setHeader("Content-Type", "text/plain; charset=utf-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setBody(body);
    }

    public Set<String> getWhitelistIps() {
        return Set.copyOf(whitelist);
    }

    public Set<String> getBlacklistIps() {
        return Set.copyOf(blacklist);
    }

    public Set<String> getWhitelistSubnets() {
        return Set.copyOf(whitelistSubnets.keySet());
    }

    public Set<String> getBlacklistSubnets() {
        return Set.copyOf(blacklistSubnets.keySet());
    }

    public boolean getAllowByDefault() {
        return allowByDefault;
    }
}
