package org.juv25d;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class AppIT {

    @Container
    @SuppressWarnings("resource")
    public static GenericContainer<?> server = new GenericContainer<>(
        new ImageFromDockerfile("java-http-server-test")
            .withFileFromPath(".", Paths.get("."))
    ).withExposedPorts(8080)
        .waitingFor(Wait.forHttp("/").forStatusCode(200));

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void shouldReturnIndexHtml() throws Exception {
        HttpResponse<String> response = get("/");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("🚀 Java HTTP Server");
        assertThat(response.headers().firstValue("Content-Type")).get().asString().contains("text/html");
    }

    @Test
    void shouldReturn404ForNonExistentPage() throws Exception {
        HttpResponse<String> response = get("/not-found.html");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("404");
    }

    private HttpResponse<String> get(String path) throws Exception {
        String url = "http://" + server.getHost() + ":" + server.getMappedPort(8080) + path;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
