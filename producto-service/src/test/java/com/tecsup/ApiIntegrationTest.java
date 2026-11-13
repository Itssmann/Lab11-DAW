package com.tecsup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.net.URI;
import java.net.http.*;
import com.jayway.jsonpath.JsonPath;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiIntegrationTest {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    private final HttpClient client = HttpClient.newHttpClient();
    private HttpResponse<String> request(String method, String suffix, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/productos" + suffix))
            .header("Content-Type", "application/json").method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void cicloCrud() throws Exception {
        var created = request("POST", "", "{\"nombre\":\"Laptop Lenovo ThinkPad\",\"precio\":3500,\"stock\":15,\"categoriaId\":1}");
        assertThat(created.statusCode()).isEqualTo(201);
        Number id = JsonPath.read(created.body(), "$.id");
        String suffix = "/" + id.longValue();
        assertThat(created.headers().firstValue("Location")).contains("/api/productos" + suffix);
        assertThat(request("GET", suffix, null).statusCode()).isEqualTo(200);
        assertThat(request("GET", "", null).body()).contains("nombre");
        var changed = request("PUT", suffix, "{\"nombre\":\"Laptop Lenovo ThinkPad actualizado\",\"precio\":3500,\"stock\":15,\"categoriaId\":1}");
        assertThat(changed.statusCode()).isEqualTo(200);
        assertThat(changed.body()).contains("actualizado");
        assertThat(request("DELETE", suffix, null).statusCode()).isEqualTo(204);
        assertThat(request("GET", suffix, null).statusCode()).isEqualTo(404);
        
    }
    @Test void rechazaDatosInvalidos() throws Exception {
        var response = request("POST", "", "{}");
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("errores");
        
    }
    @Test void devuelve404EnActualizarYEliminarInexistente() throws Exception {
        assertThat(request("PUT", "/9223372036854775807", "{\"nombre\":\"Laptop Lenovo ThinkPad\",\"precio\":3500,\"stock\":15,\"categoriaId\":1}").statusCode()).isEqualTo(404);
        assertThat(request("DELETE", "/9223372036854775807", null).statusCode()).isEqualTo(404);
    }
    @Test void rechazaJsonYParametrosInvalidos() throws Exception {
        assertThat(request("POST", "", "{").statusCode()).isEqualTo(400);
        assertThat(request("GET", "/abc", null).statusCode()).isEqualTo(400);
        assertThat(request("GET", "/0", null).statusCode()).isEqualTo(400);
    }
}
