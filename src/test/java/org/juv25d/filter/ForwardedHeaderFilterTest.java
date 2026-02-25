package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.util.ConfigLoader;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link ForwardedHeaderFilter} class.
 */
@ExtendWith(MockitoExtension.class)
class ForwardedHeaderFilterTest {

    @Mock
    private HttpRequest req;
    @Mock
    private HttpResponse res;
    @Mock
    private FilterChain chain;

    private final String expectedRemoteIp = "127.0.0.1";
    private final String singleForwardedHeader = "127.0.0.1";
    private final String multipleForwardedHeader = "127.0.0.1, 123.0.0.1, 83.2.0.12";
    private final List<String> trustedProxy = List.of("123.0.0.1", "83.2.0.12");

    @Test
    void shouldSetRemoteIp_fromForwardedHeader() throws IOException {
        // Arrange
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        when(req.headers()).thenReturn(Map.of("X-Forwarded-For", singleForwardedHeader));
        when(req.method()).thenReturn("GET");
        when(req.path()).thenReturn("/test");
        when(req.queryString()).thenReturn("");
        when(req.httpVersion()).thenReturn("HTTP/1.1");
        when(req.body()).thenReturn(new byte[0]);
        when(req.creationTimeNanos()).thenReturn(1000L);

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain).doFilter(captor.capture(), eq(res));
        assertEquals(expectedRemoteIp, captor.getValue().remoteIp());
        assertEquals("GET", captor.getValue().method());
        assertEquals("/test", captor.getValue().path());
        assertEquals("", captor.getValue().queryString());
        assertEquals("HTTP/1.1", captor.getValue().httpVersion());
        assertArrayEquals(new byte[0], captor.getValue().body());
        assertEquals(1000L, captor.getValue().creationTimeNanos());
    }

    @Test
    void shouldPassOnRequest_ifHeaderNotPresent() throws IOException {
        // Arrange
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        when(req.remoteIp()).thenReturn(expectedRemoteIp);
        when(req.headers()).thenReturn(Map.of());

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain).doFilter(captor.capture(), eq(res));
        assertEquals(expectedRemoteIp, captor.getValue().remoteIp());
    }

    @Test
    void shouldPassOnRequest_ifHeaderIsBlank() throws IOException {
        // Arrange
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        when(req.remoteIp()).thenReturn(expectedRemoteIp);
        when(req.headers()).thenReturn(Map.of("X-Forwarded-For", ""));

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain).doFilter(captor.capture(), eq(res));
        assertEquals(expectedRemoteIp, captor.getValue().remoteIp());
    }

    @Test
    void shouldReturnIp_toTheLeftOfTrustedProxy() throws IOException {
        // Arrange
        ConfigLoader configLoader = mock(ConfigLoader.class);
        when(configLoader.getTrustedProxies()).thenReturn(trustedProxy);
        when(req.headers()).thenReturn(Map.of("X-Forwarded-For", multipleForwardedHeader));

        ForwardedHeaderFilter filter = new ForwardedHeaderFilter(configLoader);

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain).doFilter(captor.capture(), eq(res));
        assertEquals(expectedRemoteIp, captor.getValue().remoteIp());
    }

    @Test
    void shouldReturnFirstIp_whenNoTrustedProxiesAreConfigured() throws IOException {
        // Arrange
        ConfigLoader configLoader = mock(ConfigLoader.class);
        when(configLoader.getTrustedProxies()).thenReturn(List.of());
        when(req.headers()).thenReturn(Map.of("X-Forwarded-For", multipleForwardedHeader));

        ForwardedHeaderFilter filter = new ForwardedHeaderFilter(configLoader);

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain).doFilter(captor.capture(), eq(res));
        assertEquals(singleForwardedHeader, captor.getValue().remoteIp());
    }

    @Test
    void shouldReturnFirstIp_whenAllIpsAreTrusted() throws IOException {
        // Arrange
        ConfigLoader configLoader = mock(ConfigLoader.class);
        when(configLoader.getTrustedProxies()).thenReturn(List.of("127.0.0.1", "123.0.0.1", "83.2.0.12"));
        when(req.headers()).thenReturn(Map.of("X-Forwarded-For", multipleForwardedHeader));

        ForwardedHeaderFilter filter = new ForwardedHeaderFilter(configLoader);

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain).doFilter(captor.capture(), eq(res));
        assertEquals(expectedRemoteIp, captor.getValue().remoteIp());
    }
}
