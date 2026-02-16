package org.juv25d.util;

import org.junit.jupiter.api.Test;

    class ConfigLoaderTest {

        @Test
        void loadsValuesFromYaml() throws Exception {
            String yaml = """
                server:
                  port: 3000
                  root-dir: public
                logging:
                  level: INFO
                """;

            Object config = loadConfigFromYamlInIsolatedClassLoader(yaml);
        }

        private Object loadConfigFromYamlInIsolatedClassLoader(String yamlContentOrNull) throws Exception {
            return null;
        }


        @Test
        void usesDefaultsWhenServerKeysMissing() {

        }

        @Test
        void throwsWhenYamlMissing() {

        }
    }
