package org.juv25d.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HttpResponseTest {

    private HttpResponse response;

    @BeforeEach
    void setUp() {
        response = new HttpResponse();
    }

    @Test
    void shouldReturnDefaultStatusCode() {
        assertEquals(200, response.statusCode());
    }

    private HttpResponse response;

    @BeforeEach
    void setUp() {
        response = new HttpResponse();
    }

    @Test
    void defaultConstructor_hasSafeDefaults_andSetHeaderDoesNotThrow() {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.statusText()).isEqualTo("OK");
        assertThat(response.headers()).isNotNull();
        assertThat(response.headers()).isEmpty();
        assertThat(response.body()).isNotNull();
        assertThat(response.body()).isEmpty();
    }

    @Test
    void setHeader_setsEntry_andDoesNotThrow() {
        assertThatCode(() -> response.setHeader("Content-Type", "text/plain"))
            .doesNotThrowAnyException();

    @Test
    void shouldHaveEmptyBodyByDefault() {
        assertArrayEquals(new byte[0], response.body());
    }
}
