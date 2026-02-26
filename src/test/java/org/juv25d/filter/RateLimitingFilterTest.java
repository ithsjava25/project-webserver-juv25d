package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link RateLimitingFilter} class.
 */
@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private HttpRequest req;
    @Mock
    private HttpResponse res;
    @Mock
    private FilterChain chain;

    /**
     * Verifies that the filter allows requests when they are within the rate limit.
     */
    @Test
    void shouldAllowRequest_whenWithinRateLimit() throws IOException {
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

    /**
     * Verifies that the filter blocks requests when the rate limit is exceeded.
     */
    @Test
    void shouldBlockRequest_whenExceedingRateLimit() throws IOException {
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
        verify(res).setHeader("Content-Type", "text/html; charset=utf-8");
        verify(res).setHeader(eq("Content-Length"), any());
        verify(res).setHeader("Retry-After", "60");
        verify(res).setBody(any());
    }

    /**
     * Verifies that rate limits are tracked independently for different client IPs.
     */
    @Test
    void shouldAllowRequests_fromDifferentIpsIndependently() throws IOException {
        // Arrange
        RateLimitingFilter filter = new RateLimitingFilter(60, 5);
        HttpRequest req2 = mock(HttpRequest.class);
        HttpResponse res2 = mock(HttpResponse.class);
        when(req.remoteIp()).thenReturn("127.0.0.1");
        when(req2.remoteIp()).thenReturn("192.168.1.1");

        // Act
        for (int i = 0; i < 6; i++) { // Empty first bucket
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

    /**
     * Verifies that the internal bucket map is cleared when the filter is destroyed.
     */
    @Test
    void shouldClearBuckets_onDestroy() throws IOException {
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

    /**
     * Verifies that the constructor throws an exception for invalid configuration values.
     */
    @Test
    void shouldThrowException_whenInvalidConfiguration() {
        // Act & Assert
        assertThatThrownBy(() -> new RateLimitingFilter(0, 5))
            .isInstanceOf(IllegalArgumentException.class);

        // Act & Assert
        assertThatThrownBy(() -> new RateLimitingFilter(60, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
