package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForwardedHeaderFilterTest {

    @Mock
    private HttpRequest req;
    @Mock
    private HttpResponse res;
    @Mock
    private FilterChain chain;

    private final String expectedRemoteIp = "127.0.0.1";

    @Test
    void shouldSetRemoteIp_fromForwardedHeader() throws IOException {
        // Arrange
        String expectedForwardedHeader = "127.0.0.1, 83.2.0.12";
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        when(req.headers()).thenReturn(Map.of("X-Forwarded-For", expectedForwardedHeader));
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
}
