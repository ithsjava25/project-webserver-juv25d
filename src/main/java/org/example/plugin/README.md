
# 📄 **How to Create a New Plugin**

Plugins are responsible for generating the final HTTP response after all filters have completed.
A plugin is the "handler" or "controller" of the request.

Plugins run **after** all filters and are the final step in the request pipeline.

---

## Plugin Interface

All plugins must implement:

```java
public interface Plugin {
    void handle(HttpRequest req, HttpResponse res) throws IOException;
}
```

## Creating a plugin
- Create a new class in src/main/java/.../plugins/
- Implement the Plugin interface
- Write your request-handling logic inside handle
- Set status code, headers, and body on the HttpResponse

Example1: HelloPlugin

Example2:
```java
public class RouterPlugin implements Plugin {
    @Override
    public void handle(HttpRequest req, HttpResponse res) {
        if (req.path().equals("/")) {
            res.setStatus(200);
            res.setBody("Welcome!");
        } else if (req.path().equals("/about")) {
            res.setStatus(200);
            res.setBody("About page");
        } else {
            res.setStatus(404);
            res.setBody("Not Found");
        }
    }
}
```

## Register your plugin (src/org.example/App.java)
```
Pipeline pipeline = new Pipeline();
pipeline.setPlugin(new HelloPlugin);
```

## Plugin Execution flow
Client → Filters → Plugin → Response → Client

## Guidelines
- Plugins should contain the main request-handling logic
- Keep plugins cohesive (one plugin = one responsibility)
- Avoid cross-cutting concerns (logging, rate limiting → filters)
- Always set a status code and body
