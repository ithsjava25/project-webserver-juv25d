package org.juv25d.util;

import org.juv25d.proxy.ProxyRoute;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConfigLoader {
    private static ConfigLoader instance;
    private int port;
    private String logLevel;
    private String rootDirectory;
    private long requestsPerMinute;
    private long burstCapacity;
    private boolean rateLimitingEnabled;
    private List<ProxyRoute> proxyRoutes = new ArrayList<>();

    private ConfigLoader() {
        loadConfiguration(getClass().getClassLoader()
            .getResourceAsStream("application-properties.yml")); }

    // new constructor for testing
    ConfigLoader(InputStream input) {
        loadConfiguration(input); }


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

            // server
            Map<String, Object> serverConfig = asStringObjectMap(config.get("server"));
            if (serverConfig != null) {
                Object portValue = serverConfig.get("port");
                if (portValue instanceof Number n) this.port = n.intValue();

                Object root = serverConfig.get("root-dir");
                if (root != null) this.rootDirectory = String.valueOf(root);

                // proxy routes
                Map<String, Object> proxyConfig = asStringObjectMap(serverConfig.get("proxy"));
                if (proxyConfig != null) {
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
            Map<String, Object> loggingConfig = asStringObjectMap(config.get("logging"));
            if (loggingConfig != null) {
                Object level = loggingConfig.get("level");
                if (level != null) this.logLevel = String.valueOf(level);
            }

            // rate-limiting
            // defaults (consistent pattern)
            this.rateLimitingEnabled = false;

            Map<String, Object> rateLimitingConfig = asStringObjectMap(config.get("rate-limiting"));
            if (rateLimitingConfig != null) {
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
    private static Map<String, Object> asStringObjectMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }
    public long getRequestsPerMinute() {
        return requestsPerMinute;
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
