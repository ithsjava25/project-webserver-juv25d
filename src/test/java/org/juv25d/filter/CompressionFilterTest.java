package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class CompressionFilterTest {

    @Mock
    private HttpRequest req;
    @Mock
    private HttpResponse res;
    @Mock
    private FilterChain chain;

    @Test
    void shouldNotCompress_whenDisabled() throws IOException {
        CompressionFilter filter = new CompressionFilter(false, 1024);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoMoreInteractions(res);
    }
}
