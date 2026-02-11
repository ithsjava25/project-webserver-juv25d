# Creating a New Filter

Filters allow you to intercept and modify HTTP requests *before* they reach the plugin, and modify responses *before* they are sent back to the client.
They are executed in sequence through a `FilterChain`.

A filter can:
- Inspect or modify the incoming `HttpRequest`
- Inspect or modify the outgoing `HttpResponse`
- Stop the chain (e.g., return a 403 or 429)
- Allow the chain to continue by calling `chain.doFilter(req, res)`

---

## Filter Interface

All filters must implement:

```java
public interface Filter {
    void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException;
}
```
Example: LoggingFilter

## Creating a filter

- Create a new class in src/main/java/.../filters/
- Implement the Filter interface
- Add your logic inside doFilter
- Decide whether to continue the chain or stop it

## Register your filter (src/org.example/App.java)

Register your filter using:
```
    Pipeline pipeline = new Pipeline();
    pipeline.addFilter(new LoggingFilter());
```

## Filter execution flow

Client →
    Filter 1 → Filter 2 → ... → Filter N →
        Plugin → Response →
            back through filters → Client
