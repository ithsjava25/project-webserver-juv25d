# 🚀 Java HTTP Server – Team juv25d

A lightweight, modular HTTP server built from scratch in Java.

This project demonstrates how web servers and backend frameworks work internally — without using Spring, Tomcat, or other high-level frameworks.

The server is distributed as a Docker image via GitHub Container Registry (GHCR).

---

# 📌 Project Purpose

The goal of this project is to deeply understand:

- How HTTP works
- How requests are parsed
- How responses are constructed
- How middleware (filters) operate
- How backend frameworks structure request lifecycles
- How static file serving works
- How architectural decisions are documented (ADR)
- How Java services are containerized with Docker

This is an educational backend architecture project.

---

# ⚙ Requirements

- Java 21+ (uses Virtual Threads via Project Loom)
- Docker (for running the official container image)

---

# 🏗 Architecture Overview

## Request Lifecycle

```
Client
  ↓
ServerSocket
  ↓
ConnectionHandler (Virtual Thread)
  ↓
Pipeline
  ↓
FilterChain
  ↓
Plugin
  ↓
HttpResponseWriter
  ↓
Client
```

---

## 🧩 Core Components

### Server
- Listens on a configurable port
- Accepts incoming socket connections
- Spawns a virtual thread per request (`Thread.ofVirtual()`)

### ConnectionHandler
- Parses the HTTP request using `HttpParser`
- Creates a default `HttpResponse`
- Executes the `Pipeline`

### Pipeline
- Holds global filters
- Holds route-specific filters
- Creates and executes a `FilterChain`
- Executes the active plugin

### Filters
Used for cross-cutting concerns such as:
- Logging
- Authentication
- Rate limiting
- Validation
- Compression
- Security headers

### Plugin
Responsible for generating the final HTTP response.

### HttpParser
Custom HTTP request parser that:
- Parses request line
- Parses headers
- Handles `Content-Length`
- Extracts path and query parameters

### HttpResponseWriter
Responsible for:
- Writing status line
- Writing headers
- Automatically setting `Content-Length`
- Writing response body

---

# 🐳 Running the Server (Official Method)

The official way to run the server is via Docker using GitHub Container Registry.

Docker must be installed and running.

---

## Step 1 – Login to GHCR

```bash
docker login ghcr.io -u <your-github-username>
```

Use your GitHub Personal Access Token (classic) as password.

---

## Step 2 – Pull the latest image

```bash
docker pull ghcr.io/ithsjava25/project-webserver-juv25d:latest
```

---

## Step 3 – Run the container

```bash
docker run -p 8080:8080 ghcr.io/ithsjava25/project-webserver-juv25d:latest
```

---

## Step 4 – Open in browser

```
http://localhost:8080
```

The server runs on port **8080**.

---

# 🛠 Running in Development (IDE)

For development purposes, you can run the server directly from your IDE:

1. Open the project.
2. Run the class:

```
org.juv25d.App
```

3. Open:

```
http://localhost:8080
```

Note: The project is packaged as a fat JAR using the Maven Shade Plugin, so you can run it with `java -jar target/app.jar`.

---

# 🌐 Static File Serving

The `StaticFilesPlugin` serves files from:

```
src/main/resources/static/
```

### Example Mapping

| File | URL |
|------|------|
| index.html | `/` |
| css/styles.css | `/css/styles.css` |
| js/app.js | `/js/app.js` |

### Security Features

- Path traversal prevention
- MIME type detection
- 404 handling
- 403 handling
- Clean URLs (no `/static/` prefix)

For full architectural reasoning, see:

➡ `docs/adr/ADR-001-static-file-serving-architecture.md`

---

# 🔄 Creating a Filter

Filters intercept requests before they reach the plugin.

A filter can:

- Inspect or modify `HttpRequest`
- Inspect or modify `HttpResponse`
- Stop the chain (e.g., return 403)
- Continue processing by calling `chain.doFilter(req, res)`

---

## Filter Interface

```java
public interface Filter {
    void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException;
}
```

---

## Example: LoggingFilter

```java
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        System.out.println(req.method() + " " + req.path());
        chain.doFilter(req, res);
    }
}
```

---

## Registering a Global Filter

```java
pipeline.addGlobalFilter(new LoggingFilter(), 100);
```

Lower order values execute first.

---

# 🎯 Route-Specific Filters

Route filters only execute when the request path matches a pattern.

### Supported Patterns

- `/api/*` → matches paths starting with `/api/`
- `/login` → exact match
- `/admin/*` → wildcard support (prefix-based)

---

## Example

```java
pipeline.addRouteFilter(new JwtAuthFilter(), 100, "/api/*");
```

---

## Execution Flow

```
Client → Filter 1 → Filter 2 → ... → Plugin → Response → Client
```

---

# 🧠 Creating a Plugin

Plugins generate the final HTTP response.

They run after all filters have completed.

---

## Plugin Interface

```java
public interface Plugin {
    void handle(HttpRequest req, HttpResponse res) throws IOException;
}
```

---

## Example: HelloPlugin

```java
public class HelloPlugin implements Plugin {

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        res.setStatusCode(200);
        res.setStatusText("OK");
        res.setHeader("Content-Type", "text/plain");
        res.setBody("Hello from juv25d server".getBytes());
    }
}
```

---

## Registering a Plugin

```java
pipeline.setPlugin(new HelloPlugin());
```

---

# ⚙ Configuration

Configuration is loaded from:

```
application-properties.yml
```

Example:

```yaml
server:
    port: 8080
    root-dir: static

logging:
    level: INFO
```

---

# 📦 Features

- Custom HTTP request parser (`HttpParser`)
- Custom HTTP response writer (`HttpResponseWriter`)
- Mutable HTTP response model
- Filter chain architecture
- Plugin system
- Static file serving
- MIME type resolution
- Path traversal protection
- Virtual threads (Project Loom)
- YAML configuration (SnakeYAML)
- Dockerized distribution
- Published container image (GHCR)

---

# 📚 Documentation & Architecture Decisions

Additional technical documentation is available in the `docs/` directory.

## Architecture Decision Records (ADR)

Contains architectural decisions and their reasoning.

```
docs/adr/
```

Main index:

```
docs/adr/README.md
```

Includes:

- Static file serving architecture
- ADR template
- Future architecture decisions

---

## Technical Notes

Advanced filter configuration examples:

```
docs/notes/
```

---

# 🎓 Educational Value

This project demonstrates:

- How web servers work internally
- How middleware pipelines are implemented
- How static file serving works
- How architectural decisions are documented
- How Java services are containerized and distributed

---

# 👥 Team juv25d

Built as a learning project to deeply understand HTTP, backend systems, and modular server architecture.
