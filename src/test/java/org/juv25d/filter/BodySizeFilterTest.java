package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class BodySizeFilterTest {

    @Mock
    private HttpRequest req;
    @Mock
    private FilterChain chain;
    @Mock
    private HttpResponse res;

    @Test
    void shouldAllowRequest_whenBodySizeIsWithingLimit() throws IOException {
        // TODO: Implement filter and test
    }

    @Test
    void shouldBlockRequest_whenBodySizeExceedsLimit() throws IOException {
        // TODO: Implement filter and test
    }

    @Test
    void shouldAllowRequest_whenMethodHasNoBody() throws IOException {
        // TODO: Implement filter and test
    }

    @Test
    void shouldBlockRequest_whenMethodHasBodyButSizeIsZero() throws IOException {
        // TODO: Implement filter and test
    }

    @Test
    void shouldThrowException_whenInvalidConfiguration() {
        // TODO: Implement filter and test
    }





}
