package org.juv25d.util;

import org.jspecify.annotations.Nullable;
import org.juv25d.proxy.ProxyRoute;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

public class ConfigLoader {
    @Nullable private static ConfigLoader instance;
    private int port;
    private String logLevel = "INFO";
    private String rootDirectory = "static";
    private long requestsPerMinute;
    private long burstCapacity;
    private boolean rateLimitingEnabled;
    private List<String> trustedProxies;
    private List<ProxyRoute> proxyRoutes = new ArrayList<>();

    private ConfigLoader() {
        loadConfiguration(getClass().getClassLoader()
            .getResourceAsStream("application-properties.yml"));
    }

    // new constructor for testing
    ConfigLoader(InputStream input) {
        loadConfiguration(input);
    }


    public static synchronized ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }

    private void loadConfiguration(InputStream input) {
        Yaml yaml = new Yaml();

        if (input == null) {
            throw new IllegalArgumentException("Did not find application-properties.yml");
        }
        try (input) {

            Map<String, Object> config = yaml.load(input);
            if (config == null) config = Map.of();

            // defaults always
            this.port = 8080;
            this.rootDirectory = "static";
            this.logLevel = "INFO";
            this.trustedProxies = List.of();

            // server
            Object serverObj = config.get("server");
            if (serverObj != null) {
                Map<String, Object> serverConfig = asStringObjectMap(serverObj);
                Object portValue = serverConfig.get("port");
                if (portValue instanceof Number n) this.port = n.intValue();

                Object root = serverConfig.get("root-dir");
                if (root != null) this.rootDirectory = String.valueOf(root);

                Object trustedProxiesValue = serverConfig.get("trusted-proxies");
                if (trustedProxiesValue instanceof List<?> list) {
                    this.trustedProxies = list.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                }

                // proxy routes
                Object proxyObj = serverConfig.get("proxy");
                if (proxyObj != null) {
                    Map<String, Object> proxyConfig = asStringObjectMap(proxyObj);
                    List<Map<String, Object>> routes = (List<Map<String, Object>>) proxyConfig.get("routes");
                    if (routes != null) {
                        for (Map<String, Object> route : routes) {
                            String baseRoute = String.valueOf(route.get("base-route"));
                            String upstreamUrl = String.valueOf(route.get("upstream-url"));
                            this.proxyRoutes.add(new ProxyRoute(baseRoute, upstreamUrl));
                        }
                    }
                }
            }

            // logging
            Object loggingObj = config.get("logging");
            if (loggingObj != null) {
                Map<String, Object> loggingConfig = asStringObjectMap(loggingObj);
                Object level = loggingConfig.get("level");
                if (level != null) this.logLevel = String.valueOf(level);
            }

            // rate-limiting
            // defaults (consistent pattern)
            this.rateLimitingEnabled = false;

            Object rateLimitObj = config.get("rate-limiting");
            if (rateLimitObj != null) {
                Map<String, Object> rateLimitingConfig = asStringObjectMap(rateLimitObj);
                this.rateLimitingEnabled =
                    Boolean.parseBoolean(String.valueOf(rateLimitingConfig.getOrDefault("enabled", false)));

                this.requestsPerMinute =
                    Long.parseLong(String.valueOf(rateLimitingConfig.getOrDefault("requests-per-minute", 60L)));

                this.burstCapacity =
                    Long.parseLong(String.valueOf(rateLimitingConfig.getOrDefault("burst-capacity", 100L)));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load application config", e);
        }
    }

    public int getPort() {
        return port;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asStringObjectMap(@Nullable Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    public long getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public List<String> getTrustedProxies() {
        return trustedProxies;
    }

    public long getBurstCapacity() {
        return burstCapacity;
    }

    public boolean isRateLimitingEnabled() {
        return rateLimitingEnabled;
    }

    public List<ProxyRoute> getProxyRoutes() {
        return Collections.unmodifiableList(proxyRoutes);
    }
}
