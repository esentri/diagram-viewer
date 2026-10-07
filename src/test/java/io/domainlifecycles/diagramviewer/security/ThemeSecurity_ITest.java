package io.domainlifecycles.diagramviewer.security;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The stylesheets of the Vaadin themes are loaded before signing in - by the sign in view itself. Redirected to the
 * sign in view instead, the views lack their theme, and the browser ends in a redirect loop.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThemeSecurity_ITest extends BaseIntegrationTest {

    @Value("${local.server.port}")
    int port;

    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    @Test
    void Should_ServeTheLumoStylesheet_WithoutSigningIn() throws Exception {
        HttpResponse<String> response = get("/lumo/lumo.css");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(type -> assertThat(type).contains("css"));
    }

    @Test
    void Should_RedirectTheViewsToSigningIn() throws Exception {
        HttpResponse<String> response = get("/project/any");

        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("Location")).hasValueSatisfying(location -> assertThat(location).endsWith("/signin"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }
}
