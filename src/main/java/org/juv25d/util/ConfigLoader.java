package org.juv25d.util;

import org.jspecify.annotations.Nullable;
import org.juv25d.proxy.ProxyRoute;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.*;


public class ConfigLoader {
    @Nullable private static ConfigLoader instance;
    private int port;
    private int minCompressSize;
    private String logLevel = "INFO";
    private String rootDirectory = "static";
    private long requestsPerMinute;
    private long burstCapacity;
    private long maxBodySizeMb;
    private boolean rateLimitingEnabled;
    private boolean compressionEnabled;
    private boolean requestBodySizeEnabled;
    private List<String> trustedProxies;
    private List<ProxyRoute> proxyRoutes = new ArrayList<>();
    private List <String> allowedOrigins = List.of();
    private List <String> allowedMethods = List.of();

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
            this.compressionEnabled = false;
            this.minCompressSize = 1024;
            this.allowedOrigins = List.of();
            this.allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

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

            Object compressionObj = config.get("compression");
            if (compressionObj != null) {
                Map<String, Object> compressionConfig = asStringObjectMap(compressionObj);
                this.compressionEnabled =
                    Boolean.parseBoolean(String.valueOf(compressionConfig.getOrDefault("enabled", false)));

                int parsedMinCompressSize =
                    Integer.parseInt(String.valueOf(compressionConfig.getOrDefault("min-compress-size", 1024)));
                this.minCompressSize = Math.max(100, parsedMinCompressSize);
            }

            //Cors
            Object corsObj = config.get("cors");
            if (corsObj != null) {
                Map<String, Object> corsConfig = asStringObjectMap(corsObj);

                //Allowed-origins
                Object origins = corsConfig.get("allowed-origins");
                if (origins instanceof List<?> list) {
                    this.allowedOrigins = list.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                }

                //Allowed-methods
                Object methods = corsConfig.get("allowed-methods");
                if (methods instanceof List<?> methodList) {
                    List <String> parsedMethods = methodList.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> s.toUpperCase(Locale.ROOT))
                        .toList();

                    this.allowedMethods = parsedMethods.isEmpty()
                        ? List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        : parsedMethods;
                }
            }
            // request body size
            this.requestBodySizeEnabled = false;
            this.maxBodySizeMb = 10L;

            Map<String, Object> bodySizeConfig = asStringObjectMap(config.get("request-body-size"));
            if (bodySizeConfig != null) {
                this.requestBodySizeEnabled =
                    Boolean.parseBoolean(String.valueOf(bodySizeConfig.getOrDefault("enabled", false)));
                this.maxBodySizeMb =
                    Long.parseLong(String.valueOf(bodySizeConfig.getOrDefault("max-size-mb", 10L)));
            }

            if (this.maxBodySizeMb <= 0) {
                throw new IllegalArgumentException(
                    "request-body-size.max-size-mb must be greater than 0, got: " + this.maxBodySizeMb
                );
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

    public boolean isBodySizeEnabled() {
        return requestBodySizeEnabled;
    }

    public long getMaxBodySizeMb() {
        return maxBodySizeMb;
    }

    public List<ProxyRoute> getProxyRoutes() {
        return Collections.unmodifiableList(proxyRoutes);
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public int getMinCompressSize() {
        return minCompressSize;
    }
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

}
