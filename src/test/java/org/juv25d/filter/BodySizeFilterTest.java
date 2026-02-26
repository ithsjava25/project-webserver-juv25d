package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

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
    void shouldAllowRequest_whenBodySizeIsWithinLimit() throws IOException {
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("POST");
        when(req.headers()).thenReturn(Map.of("Content-Length", "1048576"));

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        verifyNoMoreInteractions(chain);
        verifyNoInteractions(res);
    }

    @Test
    void shouldBlockRequest_whenBodySizeExceedsLimit() throws IOException {
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("POST");
        when(req.headers()).thenReturn(Map.of("Content-Length", "20971520"));

        filter.doFilter(req, res, chain);

        verifyNoInteractions(chain);
        verify(res).setStatusCode(413);
        verify(res).setStatusText("Payload Too Large");
        verify(res).setHeader("Content-Type", "text/plain; charset=utf-8");
        verify(res).setHeader(eq("Content-Length"), any());
        verify(res).setBody(any());
    }

    @Test
    void shouldAllowRequest_whenMethodHasNoBody() throws IOException {
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("GET");

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        verifyNoMoreInteractions(res);
    }

    @Test
    void shouldBlockRequest_whenContentLengthMissing() throws IOException {
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("POST");
        when(req.headers()).thenReturn(Map.of());

        filter.doFilter(req, res, chain);

        verifyNoInteractions(chain);
        verify(res).setStatusCode(413);
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
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("PUT");
        when(req.headers()).thenReturn(Map.of("Content-Length", "1048576"));

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    void shouldAllowRequest_whenMethodIsPatch() throws IOException {
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("PATCH");
        when(req.headers()).thenReturn(Map.of("Content-Length", "1048576"));

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    void shouldBlockRequest_whenContentLengthInvalid() throws IOException {
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("POST");
        when(req.headers()).thenReturn(Map.of("Content-Length", "invalid"));

        filter.doFilter(req, res, chain);

        verifyNoInteractions(chain);
        verify(res).setStatusCode(413);
    }

    @Test
    void shouldBlockRequest_whenContentLengthNegative() throws IOException {
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("POST");
        when(req.headers()).thenReturn(Map.of("Content-Length", "-1"));

        filter.doFilter(req, res, chain);

        verifyNoInteractions(chain);
        verify(res).setStatusCode(413);
    }

    @Test
    void shouldAllowRequest_whenContentLengthHeaderLowercase() throws IOException {
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("POST");
        when(req.headers()).thenReturn(Map.of("content-length", "5"));

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        verifyNoInteractions(res);
    }

    @Test
    void shouldAllowRequest_whenContentLengthHasSurroundingWhitespace() throws IOException {
        BodySizeFilter filter = new BodySizeFilter(10);
        when(req.method()).thenReturn("POST");
        when(req.headers()).thenReturn(Map.of("Content-Length", " 5 "));

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        verifyNoInteractions(res);
    }



}
