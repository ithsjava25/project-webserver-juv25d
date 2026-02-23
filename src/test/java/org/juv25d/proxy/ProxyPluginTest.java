package org.juv25d.proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;

import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProxyPluginTest {

    @Nested
    class IntegrationTests {
        private ProxyRoute proxyRoute;
        private ProxyPlugin proxyPlugin;

        @DisplayName("should handle the request to an invalid upstream and return 502")
        @Test
        void handleInvalidDomain() throws IOException {
            this.proxyRoute = new ProxyRoute("/api", "https://invalid-upstream-domain-ex-juv25d.info");
            this.proxyPlugin = new ProxyPlugin(proxyRoute);

            HttpRequest req = new HttpRequest(
                "GET",
                "/api/users",
                null,
                "HTTP/1.1",
                Map.of("Content-Type", "application/json"),
                new byte[0],
                "127.0.0.1"
            );
            HttpResponse res = new HttpResponse();

            proxyPlugin.handle(req, res);

            // returns 502 Bad Gateway when connection fails
            assertEquals(502, res.statusCode());
        }

        @DisplayName("proxies the request to valid upstream target server but non existing resource path and relay 404")
        @Test
        void upstreamResourceNotFound() throws IOException {
            this.proxyRoute = new ProxyRoute("/api", "https://jsonplaceholder.typicode.com");
            this.proxyPlugin = new ProxyPlugin(proxyRoute);

            HttpRequest req = new HttpRequest(
                "GET",
                "/api/test-resource",
                null,
                "HTTP/1.1",
                Map.of("Content-Type", "application/json"),
                new byte[0],
                "127.0.0.1"
            );
            HttpResponse res = new HttpResponse();

            proxyPlugin.handle(req, res);

            assertEquals(404, res.statusCode());
        }

        @DisplayName("returns 200 with response body")
        @Test
        void successfulResponse() throws IOException {
            this.proxyRoute = new ProxyRoute("/api", "https://jsonplaceholder.typicode.com");
            this.proxyPlugin = new ProxyPlugin(proxyRoute);

            HttpRequest req = new HttpRequest(
                "GET",
                "/api/posts",
                null,
                "HTTP/1.1",
                Map.of("Content-Type", "application/json"),
                new byte[0],
                "127.0.0.1"
            );

            HttpResponse res = new HttpResponse();

            proxyPlugin.handle(req, res);

            assertEquals(200, res.statusCode());
            assertNotNull(res.body());
            assertTrue(res.body().length > 0);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class UnitTests {

        @Mock
        private HttpClient httpClient;

        @Mock
        private java.net.http.HttpResponse<byte[]> upstreamResponse;

        @Mock
        private HttpHeaders httpHeaders;

        private ProxyRoute proxyRoute;
        private ProxyPlugin proxyPlugin;

        @BeforeEach
        void setUp() {
            proxyRoute = new ProxyRoute("/api", "https://example.com");
            proxyPlugin = new ProxyPlugin(proxyRoute, httpClient);
        }

        @DisplayName("strips base route from path when building upstream URL")
        @Test
        void stripsBaseRouteFromPath() throws Exception {
            HttpRequest req = new HttpRequest(
                "GET", "/api/users", null, "HTTP/1.1",
                Map.of(), new byte[0], "127.0.0.1"
            );
            HttpResponse res = new HttpResponse();

            when(upstreamResponse.statusCode()).thenReturn(200);
            when(upstreamResponse.body()).thenReturn(new byte[0]);
            when(upstreamResponse.headers()).thenReturn(httpHeaders);
            when(httpHeaders.map()).thenReturn(Map.of());

            java.net.http.HttpRequest[] capturedRequest = new java.net.http.HttpRequest[1];
            when(httpClient.send(any(java.net.http.HttpRequest.class), any()))
                .thenAnswer(invocation -> {
                    capturedRequest[0] = invocation.getArgument(0);
                    return upstreamResponse;
                });

            proxyPlugin.handle(req, res);

            assertEquals(200, res.statusCode());
            assertEquals("https://example.com/users", capturedRequest[0].uri().toString());
            assertEquals("GET", capturedRequest[0].method());
        }

        @DisplayName("filters restricted headers from being proxied")
        @Test
        void filtersRestrictedHeaders() throws Exception {
            Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Connection", "keep-alive",
                "Host", "example.com"
            );
            HttpRequest req = new HttpRequest("GET", "/api/test/users", null, "HTTP/1.1",
                headers, new byte[0], "UNKNOWN");
            HttpResponse res = new HttpResponse();

            when(upstreamResponse.statusCode()).thenReturn(200);
            when(upstreamResponse.body()).thenReturn(new byte[0]);
            when(upstreamResponse.headers()).thenReturn(
                HttpHeaders.of(Map.of(), (name, value) -> true));
            doReturn(upstreamResponse).when(httpClient).send(
                any(java.net.http.HttpRequest.class),
                any(java.net.http.HttpResponse.BodyHandler.class)
            );

            proxyPlugin.handle(req, res);

            assertThat(res.statusCode()).isEqualTo(200);
        }

        @DisplayName("proxies request and received 200 response with JSON data")
        @Test
        void successfullyProxiesRequest() throws Exception {
            HttpRequest req = new HttpRequest("GET", "/api/test/users", null, "HTTP/1.1",
                Map.of(), new byte[0], "UNKNOWN");
            HttpResponse res = new HttpResponse();

            when(upstreamResponse.statusCode()).thenReturn(200);
            when(upstreamResponse.body()).thenReturn("{\"id\":1}".getBytes());
            when(upstreamResponse.headers()).thenReturn(
                HttpHeaders.of(Map.of("Content-Type", List.of("application/json")),
                    (name, value) -> true));

            doReturn(upstreamResponse).when(httpClient).send(
                any(java.net.http.HttpRequest.class),
                any(java.net.http.HttpResponse.BodyHandler.class)
            );

            proxyPlugin.handle(req, res);

            assertThat(res.statusCode()).isEqualTo(200);
            assertThat(new String(res.body())).isEqualTo("{\"id\":1}");
            assertThat(res.headers()).containsKey("Content-Type");
        }

        @DisplayName("returns 502 response if ConnectException is caught due to failed connection to upstream server")
        @Test
        void handlesConnectionException() throws Exception {
            HttpRequest req = new HttpRequest("GET", "/api/test/users", null, "HTTP/1.1",
                Map.of(), new byte[0], "UNKNOWN");
            HttpResponse res = new HttpResponse();

            doThrow(new ConnectException("Connection refused")).when(httpClient).send(
                any(java.net.http.HttpRequest.class),
                any(java.net.http.HttpResponse.BodyHandler.class)
            );

            proxyPlugin.handle(req, res);

            assertThat(res.statusCode()).isEqualTo(502);
            assertThat(res.statusText()).isEqualTo("Bad Gateway");
        }

        @DisplayName("return 504 when request to upstream times out")
        @Test
        void handlesTimeoutException() throws Exception {
            HttpRequest req = new HttpRequest("GET", "/api/test/users", null, "HTTP/1.1",
                Map.of(), new byte[0], "UNKNOWN");
            HttpResponse res = new HttpResponse();

            doThrow(new HttpTimeoutException("Request timed out")).when(httpClient).send(
                any(java.net.http.HttpRequest.class),
                any(java.net.http.HttpResponse.BodyHandler.class)
            );

            proxyPlugin.handle(req, res);

            assertThat(res.statusCode()).isEqualTo(504);
            assertThat(res.statusText()).isEqualTo("Gateway Timeout");
        }

        @DisplayName("generic exception return 502 Bad Gateway as default")
        @Test
        void handlesGenericException() throws Exception {
            HttpRequest req = new HttpRequest("GET", "/api/test/users", null, "HTTP/1.1",
                Map.of(), new byte[0], "UNKNOWN");
            HttpResponse res = new HttpResponse();

            doThrow(new RuntimeException("Unexpected error")).when(httpClient).send(
                any(java.net.http.HttpRequest.class),
                any(java.net.http.HttpResponse.BodyHandler.class)
            );

            proxyPlugin.handle(req, res);

            assertThat(res.statusCode()).isEqualTo(502);
            assertThat(res.statusText()).isEqualTo("Bad Gateway");
        }
    }
}

