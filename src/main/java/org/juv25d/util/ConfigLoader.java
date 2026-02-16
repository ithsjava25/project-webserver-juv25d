package org.juv25d.util;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {
    private static ConfigLoader instance;
    private int port;
    private String logLevel;
    private String rootDirectory;

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

        try (input) {
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
}
