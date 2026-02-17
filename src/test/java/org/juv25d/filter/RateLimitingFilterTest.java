package org.juv25d.filter;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private HttpRequest req;
    @Mock
    private HttpResponse res;
    @Mock
    private FilterChain chain;

    @Test
    void shouldAllowRequestWhenWithinRateLimit() throws IOException {
        // Arrange
        RateLimitingFilter filter = new RateLimitingFilter(60, 5);
        when(req.remoteIp()).thenReturn("127.0.0.1");

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain, times(1)).doFilter(req, res);
        verifyNoMoreInteractions(chain);
        verifyNoInteractions(res);
    }

    @Test
    void shouldBlockRequestWhenExceedingRateLimit() throws IOException {
        // Arrange
        RateLimitingFilter filter = new RateLimitingFilter(60, 5);
        when(req.remoteIp()).thenReturn("127.0.0.1");

        // Act
        for (int i = 0; i < 6; i++) {
            filter.doFilter(req, res, chain);
        }

        // Assert
        verify(chain, times(5)).doFilter(req, res);
        verifyNoMoreInteractions(chain);
        verify(res).setStatusCode(429);
        verify(res).setStatusText("Too Many Requests");
        verify(res).setHeader("Retry-After", "60");
    }

    @Test
    void shouldAllowRequestsFromDifferentIpsIndependently() throws IOException {
        // Arrange
        RateLimitingFilter filter = new RateLimitingFilter(60, 5);
        HttpRequest req2 = mock(HttpRequest.class);
        HttpResponse res2 = mock(HttpResponse.class);
        when(req.remoteIp()).thenReturn("127.0.0.1");
        when(req2.remoteIp()).thenReturn("192.168.1.1");

        // Act
        for (int i = 0; i < 6; i++) {
            filter.doFilter(req, res, chain);
        }
        for (int i = 0; i < 2; i++) {
            filter.doFilter(req2, res2, chain);
        }

        // Assert
        verify(chain, times(7)).doFilter(any(), any());
        verify(res).setStatusCode(429);
        verifyNoInteractions(res2);
    }

    @Test
    void destroyClearsBuckets() throws IOException {
        // Arrange
        RateLimitingFilter filter = new RateLimitingFilter(60, 5);
        when(req.remoteIp()).thenReturn("127.0.0.1");

        filter.doFilter(req, res, chain);
        assertThat(filter.getTrackedIpCount()).isEqualTo(1);

        // Act
        filter.destroy();

        // Assert
        assertThat(filter.getTrackedIpCount()).isZero();
    }
}
