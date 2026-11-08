package org.juv25d.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

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

    @Test
    void shouldNotCompress_whenNoAcceptEncoding() throws IOException {
        CompressionFilter filter = new CompressionFilter(true, 1024);
        when(req.headers()).thenReturn(Map.of());

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).setBody(any());
    }

    @Test
    void shouldNotCompress_whenAcceptEncodingIsNotGzip() throws IOException {
        CompressionFilter filter = new CompressionFilter(true, 1024);
        when(req.headers()).thenReturn(Map.of(
            "Accept-Encoding", "deflate, br"
        ));

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).setBody(any());
    }

    @Test
    void shouldCompress_whenAcceptEncodingIsGzip() throws IOException {
        CompressionFilter filter = new CompressionFilter(true, 100);
        when(req.headers()).thenReturn(Map.of(
            "Accept-Encoding", "gzip, deflate"
        ));

        byte[] body = "Hello, world!".repeat(100).getBytes();
        when(res.body()).thenReturn(body);

        filter.doFilter(req, res, chain);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(res).setBody(captor.capture());
        byte[] decompressed = gunzip(captor.getValue());
        assertArrayEquals(body, decompressed);
        verify(res).setHeader("Content-Encoding", "gzip");
        verify(res).setHeader("Vary", "Accept-Encoding");
    }

    @Test
    void shouldNotCompress_whenBodyIsSmallerThanThreshold() throws IOException {
        CompressionFilter filter = new CompressionFilter(true, 1024);
        when(req.headers()).thenReturn(Map.of(
            "Accept-Encoding", "gzip"
        ));

        when(res.body()).thenReturn("small".getBytes());

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).setBody(any());
    }

    @Test
    void shouldCompress_whenAcceptEncodingIsUpperCase() throws IOException {
        CompressionFilter filter = new CompressionFilter(true, 100);
        when(req.headers()).thenReturn(Map.of(
            "Accept-Encoding", "GZIP"
        ));

        byte[] body = "Hello, world!".repeat(100).getBytes();
        when(res.body()).thenReturn(body);

        filter.doFilter(req, res, chain);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(res).setBody(captor.capture());
        byte[] decompressed = gunzip(captor.getValue());
        assertArrayEquals(body, decompressed);
        verify(res).setHeader("Content-Encoding", "gzip");
    }

    @Test
    void shouldCompress_whenBodyIsExactlyThreshold() throws IOException {
        CompressionFilter filter = new CompressionFilter(true, 5);
        when(req.headers()).thenReturn(Map.of(
            "Accept-Encoding", "gzip"
        ));

        when(res.body()).thenReturn("Hello".getBytes());

        filter.doFilter(req, res, chain);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(res).setBody(captor.capture());
        byte[] decompressed = gunzip(captor.getValue());
        assertArrayEquals("Hello".getBytes(), decompressed);
        verify(res).setHeader("Content-Encoding", "gzip");
    }

    @Test
    void shouldNotCompress_whenGzipWithQualityZero() throws IOException {
        CompressionFilter filter = new CompressionFilter(true, 100);
        when(req.headers()).thenReturn(Map.of(
            "Accept-Encoding", "gzip;q=0"
        ));

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).setBody(any());
    }

    @Test
    void shouldCompress_whenGzipWithQualityAboveZero() throws IOException {
        CompressionFilter filter = new CompressionFilter(true, 100);
        when(req.headers()).thenReturn(Map.of(
            "Accept-Encoding", "gzip;q=0.5"
        ));
        byte[] body = "Hello, world!".repeat(100).getBytes();
        when(res.body()).thenReturn(body);

        filter.doFilter(req, res, chain);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(res).setBody(captor.capture());
        byte[] decompressed = gunzip(captor.getValue());
        assertArrayEquals(body, decompressed);
        verify(res).setHeader("Content-Encoding", "gzip");
    }

    private byte[] gunzip(byte[] gz) throws IOException {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gz));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toByteArray();
        }
    }
}
