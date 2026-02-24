package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForwardedHeaderFilterTest {

    @Mock
    private HttpRequest req;
    @Mock
    private HttpResponse res;
    @Mock
    private FilterChain chain;

    private final String expectedForwardedHeader = "127.0.0.1, 83.2.0.12";
    private final String expectedRemoteIp = "127.0.0.1";

    @Test
    void shouldSetRemoteIp_fromForwardedHeader() throws IOException {
        // Arrange
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        Mockito.when(req.headers()).thenReturn(Map.of("X-Forwarded-For", expectedForwardedHeader));

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain).doFilter(captor.capture(), Mockito.eq(res));
        assertEquals(expectedRemoteIp, captor.getValue().remoteIp());
    }

    @Test
    void shouldPassOnRequest_ifHeaderNotPresent() throws IOException {
        // Arrange
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        Mockito.when(req.remoteIp()).thenReturn("123.0.0.1");
        Mockito.when(req.headers()).thenReturn(Map.of());

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain).doFilter(captor.capture(), Mockito.eq(res));
        assertEquals("123.0.0.1", captor.getValue().remoteIp());
    }

    @Test
    void shouldPassOnRequest_ifHeaderIsBlank() throws IOException {
        // Arrange
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        Mockito.when(req.remoteIp()).thenReturn("123.0.0.1");
        Mockito.when(req.headers()).thenReturn(Map.of("X-Forwarded-For", ""));

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        // Act
        filter.doFilter(req, res, chain);

        // Assert
        verify(chain).doFilter(captor.capture(), Mockito.eq(res));
        assertEquals("123.0.0.1", captor.getValue().remoteIp());
    }

}
