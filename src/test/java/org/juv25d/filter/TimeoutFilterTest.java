package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeoutFilterTest {

    @Mock HttpRequest req;
    @Mock FilterChain chain;

    @Test
    void fastRequest_keepsDefault200() throws IOException {
        when(req.path()).thenReturn("/fast");
        HttpResponse res = new HttpResponse();
        TimeoutFilter filter = new TimeoutFilter();

        doNothing().when(chain).doFilter(req, res);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.statusCode()).isEqualTo(200);
    }

    @Test
    @Timeout(value = 4, unit = TimeUnit.SECONDS)
    void slowRequest_sets504() throws IOException {
        when(req.path()).thenReturn("/slow");
        HttpResponse res = new HttpResponse();
        TimeoutFilter filter = new TimeoutFilter();

        doAnswer(inv -> {
            Thread.sleep(3_000); // > 2000ms => timeout
            return null;
        }).when(chain).doFilter(req, res);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.statusCode()).isEqualTo(504);
        assertThat(res.statusText()).isEqualTo("Gateway Timeout");
        assertThat(new String(res.body(), StandardCharsets.UTF_8))
            .isEqualTo("504 - Gateway Timeout");
    }

    @Test
    void downstreamIOException_throwsRuntimeException() throws IOException {
        when(req.path()).thenReturn("/boom");
        HttpResponse res = new HttpResponse();
        TimeoutFilter filter = new TimeoutFilter();

        doThrow(new IOException("fail")).when(chain).doFilter(req, res);

        assertThatThrownBy(() -> filter.doFilter(req, res, chain))
            .isInstanceOf(RuntimeException.class);

        verify(chain).doFilter(req, res);
    }
}
