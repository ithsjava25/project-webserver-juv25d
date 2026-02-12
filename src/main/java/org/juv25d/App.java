package org.juv25d;

import org.juv25d.logging.ServerLogging;
import org.juv25d.http.HttpParser;
import org.juv25d.plugin.StaticFilesPlugin;

import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {
        Logger logger = ServerLogging.getLogger();
        HttpParser httpParser = new HttpParser();

        Pipeline pipeline = new Pipeline();

        // Global filters: Applied to every request
        // These filters run on all routers regardless of path

        // Access log filter
        // pipeline.addGlobalFilter(new AccessLogFilter(), 100);

        // File format compression filter
        // pipeline.addGlobalFilter(new CompressionFilter(), 200);

        //Filter by IP
        // pipeline.addGlobalFilter(new IpFilter(), 300);


        // Add more global filters here
        // Example: Security headers, CORS, Request ID, etc.
        // pipeline.addGlobalFilter(new SecurityHeadersFilter(), 400);
        // pipeline.addGlobalFilter(new CorsFilter(), 500);
        // pipeline.addGlobalFilter(new RequestIdFilter(), 600);

        // ROUTE-SPECIFIC FILTERS - Only for matching paths
        // These filters only execute when the request path matches the pattern
        // Patterns:
        //   - "/api/*"    = any path starting with /api/
        //   - "/login"    = exact match only
        //   - "/admin/**" = recursive wildcard (if implemented)


        // Rate limiting filter
        // pipeline.addRouteFilter(new RateLimitFilter(5, 60000), 100, "/login");

        // Apply to API endpoints (generous rate limit)
        // pipeline.addRouteFilter(new RateLimitFilter(1000, 60000), 100, "/api/*");

        // Apply to static files (high rate limit)
        // pipeline.addRouteFilter(new RateLimitFilter(5000, 60000), 100, "/static/*");

        // Cancels requests that take too long to process
        // pipeline.addRouteFilter(new TimeoutFilter(5000), 200, "/reports/*");

        // Apply to export endpoints (large data)
        // pipeline.addRouteFilter(new TimeoutFilter(30000), 200, "/api/export/*");

        // Apply to default API endpoints
        // pipeline.addRouteFilter(new TimeoutFilter(10000), 200, "/api/*");


        // Authentication & Authorization
        // pipeline.addRouteFilter(new JwtAuthFilter(), 100, "/api/*");
        // pipeline.addRouteFilter(new BasicAuthFilter(), 100, "/admin/*");
        // pipeline.addRouteFilter(new ApiKeyFilter(), 100, "/partner/*");

        // Request Validation
        // pipeline.addRouteFilter(new BodyValidationFilter(), 300, "/api/*");
        // pipeline.addRouteFilter(new ContentTypeFilter(), 150, "/api/*");

        // Cache Control
        // pipeline.addRouteFilter(new CacheFilter(), 400, "/static/*");
        // pipeline.addRouteFilter(new EtagFilter(), 450, "/api/*");

        // Request Transformation
        // pipeline.addRouteFilter(new BodyParserFilter(), 50, "/api/*");
        // pipeline.addRouteFilter(new MultipartParserFilter(), 60, "/upload");

        // Plugin: Final request handler, application logic, executed after all filters pass.
        // Replace HelloPlugin with your actual application plugin

        pipeline.setPlugin(new StaticFilesPlugin());

        // Filter initialization
        // Calls init() on all registered filters
        // Some filters might need setup (loading config, connecting to DB, etc)


        // Server setup
        DefaultConnectionHandlerFactory handlerFactory =
            new DefaultConnectionHandlerFactory(httpParser, logger, pipeline);

        Server server = new Server(
            logger,
            handlerFactory,
            pipeline
        );

        server.start();
    }
}
