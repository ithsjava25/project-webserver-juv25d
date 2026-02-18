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
        loadConfiguration();
    }

    public static synchronized ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }

    private void loadConfiguration() {
        Yaml yaml = new Yaml();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application-properties.yml")) {
            if (input == null) {
                throw new IllegalArgumentException("Did not find application-properties.yml");
            }

            Map<String, Object> config = yaml.load(input);

            // server
            Map<String, Object> serverConfig = (Map<String, Object>) config.get("server");
            if (serverConfig != null) {
                this.port = (Integer) serverConfig.getOrDefault("port", 8080);
                this.rootDirectory = (String) serverConfig.getOrDefault("root-dir", "static");
            }

            // logging
            Map<String, Object> loggingConfig = (Map<String, Object>) config.get("logging");
            if (loggingConfig != null) {
                this.logLevel = (String) loggingConfig.get("level");
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
            throw new RuntimeException("Failed to load application config");
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
