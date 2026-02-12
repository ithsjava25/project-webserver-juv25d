# ADR-001: Static File Serving Architecture

**Date:** 2026-02-11
**Status:** Proposed
**Deciders:** Team juv25d
**Technical Story:** Issue #18 - GET handling for static files

---

## Context

Our HTTP server needs the ability to serve static files (HTML, CSS, JavaScript, images, etc.) to support building complete web applications. Currently, our server can parse HTTP requests and send responses, but has no mechanism to serve files from the filesystem.

### Problem Statement

We need to implement a static file serving mechanism that:
- Serves files from a designated directory structure
- Maps URLs to filesystem paths safely
- Handles different file types with appropriate Content-Type headers
- Provides reasonable error handling (404, 500, etc.)
- Follows familiar conventions for ease of use

### Assumptions

- Static files will be bundled with the application at build time
- Files will be served from the classpath/resources directory
- We're building a development/learning server (not production-grade like Nginx)
- Performance requirements are moderate (not handling thousands of requests/second)

### Constraints

- Must work with our existing `HttpParser`, `HttpResponse`, and `HttpResponseWriter` classes
- Should integrate cleanly with the `SocketServer` connection handling
- Must run inside a Docker container with resources directory available
- Team is learning HTTP and web server concepts - architecture should be educational

---

## Decision

We will implement a **SpringBoot-style static file serving architecture** with the following design:

### Chosen Solution

**1. Directory Structure:**
```
src/main/resources/
└── static/
    ├── index.html
    ├── css/
    │   └── styles.css
    ├── js/
    │   └── app.js
    └── images/
        └── logo.png
```

**2. URL Mapping:**
- Files in `/resources/static/` are served at the root path
- Example: `/resources/static/css/styles.css` → `GET /css/styles.css`
- Root path `/` automatically serves `index.html` if it exists

**3. Core Components:**

```
StaticFileHandler
├── Validates request path (security)
├── Maps URL to resource path
├── Reads file from classpath
├── Determines MIME type
└── Creates HttpResponse with proper headers

MimeTypeResolver
└── Maps file extensions to Content-Type headers

Security validator
└── Prevents directory traversal attacks
```

**4. Security Measures:**
- Path normalization to prevent `../` attacks
- Whitelist only files within `/static/` directory
- Reject paths containing `..`, absolute paths, or suspicious patterns
- Return 403 Forbidden for security violations

**5. Error Handling:**
- 404 Not Found: File doesn't exist
- 403 Forbidden: Security violation detected
- 500 Internal Server Error: I/O errors

**6. MIME Type Handling:**
- Simple extension-based mapping (.html → text/html, .css → text/css, etc.)
- Default to `application/octet-stream` for unknown types
- Support common web file types (HTML, CSS, JS, PNG, JPG, SVG, etc.)

### Why This Solution?

1. **Familiar to developers:** SpringBoot convention is widely known and documented
2. **Simple mental model:** Root path maps to `/static/` - easy to understand
3. **Classpath-based:** Works well with JAR packaging and Docker containers
4. **Educational:** Clear separation of concerns teaches good architecture
5. **Extensible:** Easy to add features later (caching, compression, etc.)

---

## Consequences

### Positive Consequences

- **Developer Experience:** Developers familiar with SpringBoot will immediately understand the structure
- **Security by Design:** Explicit security validation prevents common vulnerabilities
- **Clean URLs:** No `/static/` prefix in URLs keeps them clean
- **Easy Testing:** Classpath resources are easy to test with JUnit
- **Docker-Friendly:** Resources directory is included in the container image
- **Clear Responsibility:** `StaticFileHandler` has a single, well-defined purpose

### Negative Consequences / Trade-offs

- **No Dynamic Content:** This approach only handles static files (but that's the requirement)
- **No Caching:** Every request reads from disk (acceptable for learning project)
- **Limited Performance:** Not optimized for high-traffic scenarios
- **Memory Usage:** Entire files loaded into memory before sending
- **No Range Requests:** Cannot handle partial content requests (HTTP 206)

### Risks

- **Large Files:** Loading very large files into memory could cause issues
    - *Mitigation:* Document file size limitations, implement streaming later if needed

- **MIME Type Accuracy:** Simple extension mapping might not always be correct
    - *Mitigation:* Cover most common web file types, extend mapping as needed

- **Classpath Resources:** Files must be in classpath at runtime
    - *Mitigation:* Clear documentation about where to place files

---

## Alternatives Considered

### Alternative 1: Filesystem-based serving (outside classpath)

**Description:** Serve files from a configurable filesystem directory outside the application.

**Pros:**
- Files can be updated without rebuilding
- More flexible for deployment
- Easier to handle very large files

**Cons:**
- More complex configuration
- Path handling is platform-dependent
- Docker volume mounting adds complexity
- Harder to test (need actual filesystem)

**Why not chosen:** Adds unnecessary complexity for a learning project. Classpath resources are simpler and work well in Docker.

### Alternative 2: Embedded file map (all files in memory)

**Description:** Load all static files into a HashMap at startup.

**Pros:**
- Fastest possible serving (no I/O)
- Very simple lookup logic
- Predictable memory usage

**Cons:**
- Cannot add files without restart
- High memory usage for many/large files
- Startup time increases
- Not representative of real web servers

**Why not chosen:** Not scalable and doesn't teach realistic server behavior. Reading from resources is fast enough.

### Alternative 3: Show /static/ in URLs

**Description:** Map `/static/file.html` → `/resources/static/file.html`

**Pros:**
- More explicit about what's being served
- Easier to implement (direct path mapping)
- Clear separation from dynamic routes

**Cons:**
- Less clean URLs
- Not how SpringBoot or most frameworks work
- Exposing internal structure in URLs

**Why not chosen:** Doesn't follow common web conventions. Clean URLs are expected behavior.

---

## Implementation Notes

### Phase 1: Core Implementation
1. Create `StaticFileHandler` class
2. Implement path validation and security checks
3. Create `MimeTypeResolver` utility
4. Integrate with existing `SocketServer` / connection handling
5. Add unit tests for all components

### Phase 2: Integration
6. Create example static files in `/resources/static/`
7. Update `SocketServer` to use `StaticFileHandler` for GET requests
8. Test with browser
9. Document usage in README

### Phase 3: Polish
10. Add logging for security violations
11. Create custom 404 error page
12. Add metrics/logging for file serving

### Example Usage (Future):

```java
// In connection handler:
if (request.method().equals("GET")) {
    HttpResponse response = StaticFileHandler.handleRequest(request);
    HttpResponseWriter.write(outputStream, response);
}
```

### File Structure After Implementation:

```
src/main/resources/static/
├── index.html          (served at GET /)
├── about.html          (served at GET /about.html)
├── css/
│   └── styles.css      (served at GET /css/styles.css)
└── js/
    └── app.js          (served at GET /js/app.js)
```

---

## References

- [Issue #18: GET handling for static files](https://github.com/your-repo/issues/18)
- [SpringBoot Static Content Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.static-content)
- [OWASP Path Traversal](https://owasp.org/www-community/attacks/Path_Traversal)
- [MDN HTTP Content-Type](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type)
- [Common MIME Types](https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types)

---

## Related ADRs

- ADR-002: (Future) Caching Strategy for Static Files
- ADR-003: (Future) Routing Architecture for Dynamic Handlers
