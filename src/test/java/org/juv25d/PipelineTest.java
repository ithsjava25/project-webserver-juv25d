package org.juv25d;

import org.junit.jupiter.api.Test;
import org.juv25d.plugin.HelloPlugin;
import org.juv25d.router.SimpleRouter; // New import

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    @Test
    void throwsExceptionWhenSettingNullRouter() { // Renamed test method
        Pipeline pipeline = new Pipeline();
        assertThrows(IllegalArgumentException.class, () -> pipeline.setRouter(null)); // Changed to setRouter
    }

    @Test
    void customRouterIsUsed() { // Renamed test method
        Pipeline pipeline = new Pipeline();
        SimpleRouter router = new SimpleRouter(); // Use SimpleRouter
        HelloPlugin hello = new HelloPlugin();
        router.registerPlugin("/hello", hello); // Register a plugin

        pipeline.setRouter(router); // Changed to setRouter

        assertEquals(router, pipeline.getRouter()); // Changed to getRouter
    }
}
