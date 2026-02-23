package org.juv25d.filter;
import org.junit.jupiter.api.Test;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class IpFilterTest {

    HttpRequest req = mock(HttpRequest.class);
    HttpResponse res = new HttpResponse();
    FilterChain chain  = mock(FilterChain.class);

    @Test
    void whitelist_allowsIp() throws IOException {
        IpFilter filter = new IpFilter(Set.of("127.0.0.1"), null, false);

        when(req.remoteIp()).thenReturn("127.0.0.1");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals(200, res.statusCode());
    }

    @Test
    void blacklist_blocksIp() throws IOException {
        IpFilter filter = new IpFilter(null, Set.of("127.0.0.1"), true);

        when(req.remoteIp()).thenReturn("127.0.0.1");

        filter.doFilter(req, res, chain);
        verify(chain, never()).doFilter(req, res);

        assertEquals(403, res.statusCode());
        assertEquals("Forbidden", res.statusText());

    }

    @Test
    void allowsIP_inBothList_defaultTrue() throws IOException {
        IpFilter filter = new IpFilter(Set.of("127.0.0.1"), Set.of("127.0.0.1"), true);

        when(req.remoteIp()).thenReturn("127.0.0.1");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals(200, res.statusCode());
    }

    @Test
    void blocksIP_inNeitherList_defaultFalse() throws IOException {
        IpFilter filter = new IpFilter(null, null, false);

        when(req.remoteIp()).thenReturn("127.0.0.1");

        filter.doFilter(req, res, chain);
        verify(chain, never()).doFilter(req, res);

        assertEquals(403, res.statusCode());
        assertEquals("Forbidden", res.statusText());
    }
}
