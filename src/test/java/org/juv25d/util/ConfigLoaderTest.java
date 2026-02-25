package org.juv25d.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
              trusted-proxies:
                - 1.1.1.1
            logging:
              level: "DEBUG"
            rate-limiting:
              enabled: true
              requests-per-minute: 20
              burst-capacity: 10
            """;

        ConfigLoader loader = new ConfigLoader(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(9090, loader.getPort());
        assertEquals("public", loader.getRootDirectory());
        assertEquals("1.1.1.1", loader.getTrustedProxies().getFirst());
        assertEquals("DEBUG", loader.getLogLevel());
        assertTrue(loader.isRateLimitingEnabled());
        assertEquals(20, loader.getRequestsPerMinute());
        assertEquals(10, loader.getBurstCapacity());
    }

    /**
     * Ensures that ConfigLoader falls back to its documented default values when the YAML input
     * omits server configuration keys. This test confirms that missing fields do not cause errors
     * and that default port and root directory values are applied as intended.
     */

    @Test
    void usesDefaultsWhenServerKeysMissing() {
        String yaml = """
            server: {}
            logging: {}
            rate-limiting: {}
            """;

        ConfigLoader loader = new ConfigLoader(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(8080, loader.getPort());
        assertEquals("static", loader.getRootDirectory());
        assertEquals("INFO", loader.getLogLevel());
        assertFalse(loader.isRateLimitingEnabled());
        assertEquals(List.of(), loader.getTrustedProxies());
    }

    /**
     * Confirms that ConfigLoader fails predictably when no YAML configuration is provided.
     * Passing a null InputStream should trigger a RuntimeException, indicating that the loader
     * cannot operate without configuration data.
     */

    @Test
    void throwsWhenYamlMissing() {
        assertThrows(RuntimeException.class, () ->
            new ConfigLoader(null));
    }

    @Test
    void shouldThrowWhenYamlIsMalformed() {
        String yaml = "server:\nBadYaml";
        InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> new ConfigLoader(input));

        assertTrue(ex.getMessage().contains("Failed to load application config"));
    }
}



