package org.juv25d.filter;
import org.junit.jupiter.api.Test;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

class IpFilterTest {

    @Test
    void whitelist_allowsIp() throws IOException {
        IpFilter filter = new IpFilter(Set.of("127.0.0.1"), null);

        HttpRequest request = mock(HttpRequest.class);
        when(request.remoteIp()).thenReturn("127.0.0.1");

        HttpResponse response = mock(HttpResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNotEquals(403, response.statusCode());
    }
}
