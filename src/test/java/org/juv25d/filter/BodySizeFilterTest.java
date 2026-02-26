package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
        assertThatThrownBy(() -> new BodySizeFilter(0))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new BodySizeFilter(-10))
            .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void shouldAllowRequest_whenMethodIsPut() throws IOException {
        // TODO: implementera filter
    }

    @Test
    void shouldAllowRequest_whenMethodIsPatch() throws IOException {
        // TODO: implementera filter
    }

    @Test
    void shouldBlockRequest_whenContentLengthInvalid() throws IOException {
        // TODO: implementera filter
    }





}
