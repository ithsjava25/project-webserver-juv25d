package org.juv25d.util;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {
    private static ConfigLoader instance;
    private int port;
    private String logLevel;
    private String rootDirectory;
    private long requestsPerMinute;
    private long burstCapacity;
    private boolean rateLimitingEnabled;

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
            }

            // logging
            Map<String, Object> loggingConfig = asStringObjectMap(config.get("logging"));
            if (loggingConfig != null) {
                Object level = loggingConfig.get("level");
                if (level != null) this.logLevel = String.valueOf(level);
            }

            // rate-limiting
            Map<String, Object> rateLimitingConfig = (Map<String, Object>) config.get("rate-limiting");
            if (rateLimitingConfig != null) {
                this.rateLimitingEnabled = (Boolean) rateLimitingConfig.getOrDefault("enabled", true);
                this.requestsPerMinute = ((Number) rateLimitingConfig.getOrDefault("requests-per-minute", 60L)).longValue();
                this.burstCapacity = ((Number) rateLimitingConfig.getOrDefault("burst-capacity", 100L)).longValue();
            } else {
                // rate-limiting is disabled if not present in the config file.
                this.rateLimitingEnabled = false;
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
        return null;
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
}
