package org.juv25d.proxy;

/*
 * Maps a baseRoute with its respective proxy upstream target URL.
 * The mappings are initialized in ConfigLoader.java when reading them from
 * application-properties.
 *
 * Example:
 * baseRoute: /api/v1
 * upstreamUrl: https://external-server-url
 */
public class ProxyRoute {
    private final String baseRoute;
    private final String upstreamUrl;

    public ProxyRoute(String baseRoute, String upstreamUrl) {
        this.baseRoute = baseRoute;
        this.upstreamUrl = upstreamUrl;
    }

    public String buildUrl(String targetPath, String query) {
        String url = upstreamUrl + targetPath;
        if (query != null && !query.isEmpty()) url += "?" + query;

        return url;
    }

    public String getBaseRoute() {
        return baseRoute;
    }
}
