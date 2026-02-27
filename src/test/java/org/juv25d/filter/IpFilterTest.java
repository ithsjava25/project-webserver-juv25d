package org.juv25d.filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpFilterTest {

    HttpRequest req;
    HttpResponse res;
    FilterChain chain;

    @BeforeEach
    void setUp() {
        req = mock(HttpRequest.class);
        when(req.headers()).thenReturn(Map.of());
        when(req.remoteIp()).thenReturn("127.0.0.1");

        res = new HttpResponse();
        chain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Allow ip only in whitelist")
    void whitelist_allowsIp() throws IOException {
        IpFilter filter = new IpFilter(Set.of("127.0.0.1"), null, false, false);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals(200, res.statusCode());
    }

    @Test
    @DisplayName("Allow ip from CIDR range only in whitelist")
    void whitelist_allowsIpInRange() throws IOException {
        IpFilter filter = new IpFilter(Set.of("127.0.0.0/24"), null, false);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals(200, res.statusCode());
    }

    @Test
    @DisplayName("Block ip only in blacklist")
    void blacklist_blocksIp() throws IOException {
        IpFilter filter = new IpFilter(null, Set.of("127.0.0.1"), true);

        filter.doFilter(req, res, chain);
        verify(chain, never()).doFilter(req, res);

        assertEquals(403, res.statusCode());
        assertEquals("Forbidden", res.statusText());
    }

    @Test
    @DisplayName("Block ip from CIDR range only in blacklist")
    void blacklist_blocksIpInRange() throws IOException {
        IpFilter filter = new IpFilter(null, Set.of("127.0.0.0/24"), true);

        filter.doFilter(req, res, chain);
        verify(chain, never()).doFilter(req, res);

        assertEquals(403, res.statusCode());
        assertEquals("Forbidden", res.statusText());
    }

    @Test
    @DisplayName("Allow ip in both list (whitelist prio)")
    void whitelist_overrides_blacklist() throws IOException {
        IpFilter filter = new IpFilter(Set.of("127.0.0.1"), Set.of("127.0.0.0/24"), false);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals(200, res.statusCode());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Follow default when ip in neither list")
    void Ip_inNeitherList_followsDefault(boolean allowByDefault) throws IOException {
        IpFilter filter = new IpFilter(null, null, allowByDefault);

        filter.doFilter(req, res, chain);

        if(allowByDefault) {
            verify(chain).doFilter(req, res);
            assertEquals(200, res.statusCode());
        }
        else  {
            verify(chain, never()).doFilter(req, res);
            assertEquals(403, res.statusCode());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "127.0.0.0/24"})
    @DisplayName("Allow IP or CIDR range added in existing filter")
    void addIpOrRange_whitelist(String ipOrCidr) throws IOException {
        IpFilter filter = new IpFilter(null, null, false);
        filter.addToWhitelist(ipOrCidr);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals(200, res.statusCode());

        assertTrue((filter.getWhitelistIps().contains(ipOrCidr)) || filter.getWhitelistSubnets().contains(ipOrCidr));
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "127.0.0.0/24"})
    @DisplayName("Block IP or CIDR range added in existing filter")
    void addIpOrRange_blacklist(String ipOrCidr) throws IOException {
        IpFilter filter = new IpFilter(null, null, false);
        filter.addToBlacklist(ipOrCidr);

        filter.doFilter(req, res, chain);

        verify(chain, never()).doFilter(req, res);
        assertEquals(403, res.statusCode());

        assertTrue((filter.getBlacklistIps().contains(ipOrCidr)) || filter.getBlacklistSubnets().contains(ipOrCidr));
    }

    @Test
    @DisplayName("Adding IP or CIDR range already in filter doesn't create duplicates")
    void doesNotAddDuplicates() {
        IpFilter filter = new IpFilter(Set.of("127.0.0.1", "127.0.0.0/24"), null, false);

        filter.addToWhitelist("127.0.0.1");
        filter.addToWhitelist("127.0.0.0/24");

        assertEquals(1, filter.getWhitelistIps().size());
        assertEquals(1, filter.getWhitelistSubnets().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "127.0.0.0/24"})
    @DisplayName("Fall back on blacklist/default after removing IP or CIDR range from whitelist")
    void removeIpOrRange_whitelist(String ipOrCidr) throws IOException {
        IpFilter filter = new IpFilter(Set.of(ipOrCidr), null, false);
        filter.removeFromWhitelist(ipOrCidr);

        filter.doFilter(req, res, chain);

        verify(chain, never()).doFilter(req, res);
        assertEquals(403, res.statusCode());

        assertFalse((filter.getWhitelistIps().contains(ipOrCidr)) || filter.getWhitelistSubnets().contains(ipOrCidr));
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "127.0.0.0/24"})
    @DisplayName("Fall back on whitelist/default after removing IP or CIDR range from blacklist")
    void removeIpOrRange_blacklist(String ipOrCidr) throws IOException {
        IpFilter filter = new IpFilter(null, Set.of(ipOrCidr), true);
        filter.removeFromBlacklist(ipOrCidr);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals(200, res.statusCode());

        assertFalse((filter.getBlacklistIps().contains(ipOrCidr)) || filter.getBlacklistSubnets().contains(ipOrCidr));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Block null or empty IP")
    void nullOrBlankIp_blocked(String ip) throws IOException {
        IpFilter filter = new IpFilter(null, null, true);

        when(req.remoteIp()).thenReturn(ip);

        filter.doFilter(req, res, chain);

        verify(chain, never()).doFilter(req, res);
        assertEquals(403, res.statusCode());
    }

    @Test
    @DisplayName("Ignore incorrectly formatted CIDR range")
    void invalidCidr_loggedAndIgnored() {
        IpFilter filter = new IpFilter(null, null, false);

        filter.addToWhitelist("not-a-cidr/99");

        assertEquals(0, filter.getWhitelistSubnets().size());
    }

    @Test
    @DisplayName("Get methods return immutable copies")
    void get_returnsImmutableCopy() {
        IpFilter filter = new IpFilter(null, null, false);

        assertThrows(UnsupportedOperationException.class, () ->
            filter.getWhitelistIps().add("test"));
        assertThrows(UnsupportedOperationException.class, () ->
            filter.getWhitelistSubnets().add("test"));
        assertThrows(UnsupportedOperationException.class, () ->
            filter.getBlacklistIps().add("test"));
        assertThrows(UnsupportedOperationException.class, () ->
            filter.getBlacklistSubnets().add("test"));
    }

    @Test
    @DisplayName("Use clientIp when not trusting proxies")
    void ignoreXForwarded_whenTrustProxyHeadersFalse() throws IOException {
        IpFilter filter = new IpFilter(Set.of("1.2.3.4"), null, false, false);

        when(req.headers()).thenReturn(Map.of("X-Forwarded-For", "1.2.3.4"));

        filter.doFilter(req, res, chain);

        verify(chain, never()).doFilter(req, res);
        assertEquals(403, res.statusCode());
    }

    @Test
    @DisplayName("Evaluates original client IP from X-Forwarded-For when proxied")
    void xForwardedFor_takesFirstIp() throws IOException {
        IpFilter filter = new IpFilter(Set.of("1.2.3.4"), null, false, true);

        when(req.headers()).thenReturn(Map.of("X-Forwarded-For", "1.2.3.4, 5.6.7.8"));
        when(req.remoteIp()).thenReturn("5.6.7.8");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals(200, res.statusCode());
    }

    @Test
    @DisplayName("Uses X-Real-IP header when X-Forwarded-For is absent")
    void xRealIp_overridesRemoteIp() throws IOException {
        IpFilter filter = new IpFilter(Set.of("1.2.3.4"), null, false, true);

        when(req.headers()).thenReturn(Map.of("X-Real-IP", "1.2.3.4"));
        when(req.remoteIp()).thenReturn("5.6.7.8");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Prioritizes X-Forwarded-For over X-Real-IP when both present")
    void xForwardedFor_priorityOverXRealIp() throws IOException {
        IpFilter filter = new IpFilter(Set.of("1.2.3.4"), null, false, true);

        when(req.headers()).thenReturn(Map.of(
            "X-Forwarded-For", "1.2.3.4",
            "X-Real-IP", "5.6.7.8"
        ));
        when(req.remoteIp()).thenReturn("9.9.9.9");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
    }
}
