package org.juv25d.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class HttpResponseTest {

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

        assertThat(response.headers()).containsEntry("Content-Type", "text/plain");
    }
}
