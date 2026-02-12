package org.juv25d;

import org.junit.jupiter.api.Test;
import org.juv25d.plugin.HelloPlugin;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    @Test
    void throwsExceptionWhenSettingNullPlugin() {
        Pipeline pipeline = new Pipeline();
        assertThrows(IllegalArgumentException.class, () -> pipeline.setPlugin(null));
    }

    @Test
    void customPluginIsUsed() {
        Pipeline pipeline = new Pipeline();
        HelloPlugin hello = new HelloPlugin();

        pipeline.setPlugin(hello);

        assertEquals(hello, pipeline.getPlugin());
    }
}
