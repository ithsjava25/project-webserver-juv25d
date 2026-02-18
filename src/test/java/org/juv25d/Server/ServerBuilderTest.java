package org.juv25d.Server;

import org.junit.jupiter.api.Test;
import org.juv25d.router.Router;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ServerBuilderTest {

    @Test
    void buildShouldFailIfLoggerMissing() {
        ServerBuilder builder = new ServerBuilder()
            .router(mock(Router.class));

        assertThrows(NullPointerException.class, builder::build);
    }
}
