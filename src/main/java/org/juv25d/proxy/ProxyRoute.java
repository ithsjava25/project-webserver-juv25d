package org.juv25d.proxy;

// Maps base route to upstream target URL
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
