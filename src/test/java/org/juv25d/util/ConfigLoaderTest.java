package org.juv25d.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    /**
     * Verifies that ConfigLoader correctly reads and assigns configuration values when provided
     * with a valid YAML input stream. Ensures that all expected fields—server port, root directory,
     * and logging level—are populated with the values defined in the YAML content.
     */

    @Test
    void loadsValuesFromYaml() {
        String yaml = """
                server:
                  port: 9090
                  root-dir: "public"
                logging:
                  level: "DEBUG"
                """;

        ConfigLoader loader = new ConfigLoader(
            new ByteArrayInputStream(yaml.getBytes())
        );

        assertEquals(9090, loader.getPort());
        assertEquals("public", loader.getRootDirectory());
        assertEquals("DEBUG", loader.getLogLevel());
    }

    @Test
    void usesDefaultsWhenServerKeysMissing() {

    }

    @Test
    void throwsWhenYamlMissing() {

    }
}
