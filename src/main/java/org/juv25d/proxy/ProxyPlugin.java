package org.juv25d.proxy;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.logging.ServerLogging;
import org.juv25d.plugin.Plugin;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.logging.Logger;

public class ProxyPlugin implements Plugin {
    private static final Logger logger = ServerLogging.getLogger();
    private final ProxyRoute proxyRoute;
    private final HttpClient httpClient;

    public ProxyPlugin(ProxyRoute proxyRoute) {
        this.proxyRoute = proxyRoute;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        String baseRoute = proxyRoute.getBaseRoute();
        String targetPath = req.path().substring(baseRoute.length());
        String fullUpstreamUrl = proxyRoute.buildUrl(targetPath, req.queryString());

        logger.info(String.format("Proxying %s %s?%s -> %s",
            req.method(), req.path(), req.queryString(), fullUpstreamUrl));
    }
}
