package com.tecsup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import com.jayway.jsonpath.JsonPath;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiIntegrationTest {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    private final HttpClient client = HttpClient.newHttpClient();
    private HttpResponse<String> request(String method, String suffix, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/pedidos" + suffix))
            .header("Content-Type", "application/json").method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void cicloCrud() throws Exception {
        var created = request("POST", "", "{\"fecha\":\"2026-06-10\",\"idCliente\":1,\"total\":3679.8,\"detalles\":[{\"idProducto\":1,\"cantidad\":1,\"precio\":3500},{\"idProducto\":2,\"cantidad\":2,\"precio\":89.9}]}");
        assertThat(created.statusCode()).isEqualTo(201);
        Number id = JsonPath.read(created.body(), "$.idPedido");
        String suffix = "/" + id.longValue();
        assertThat(created.headers().firstValue("Location")).contains("/api/pedidos" + suffix);
        assertThat(request("GET", suffix, null).statusCode()).isEqualTo(200);
        assertThat(request("GET", "", null).body()).contains("idPedido");
        var changed = request("PUT", suffix, "{\"fecha\":\"2026-06-10\",\"idCliente\":1,\"total\":179.8,\"detalles\":[{\"idProducto\":2,\"cantidad\":2,\"precio\":89.9}]}");
        assertThat(changed.statusCode()).isEqualTo(200);
        assertThat(((Number)JsonPath.read(changed.body(), "$.total")).doubleValue()).isEqualTo(179.80);
        assertThat((List<?>)JsonPath.read(request("GET", suffix, null).body(), "$.detalles")).hasSize(1);
        assertThat(jdbc.queryForObject("select count(*) from detalle_pedido where id_pedido = ?", Integer.class, id.longValue())).isEqualTo(1);
        assertThat(request("DELETE", suffix, null).statusCode()).isEqualTo(204);
        assertThat(request("GET", suffix, null).statusCode()).isEqualTo(404);
        assertThat(jdbc.queryForObject("select count(*) from detalle_pedido where id_pedido = ?", Integer.class, id.longValue())).isZero();
    }
    @Test void rechazaDatosInvalidos() throws Exception {
        var response = request("POST", "", "{}");
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("errores");
        assertThat(request("POST", "", "{\"fecha\":\"2026-06-10\",\"idCliente\":1,\"total\":1,\"detalles\":[{\"idProducto\":1,\"cantidad\":1,\"precio\":3500},{\"idProducto\":2,\"cantidad\":2,\"precio\":89.9}]}").statusCode()).isEqualTo(400);
    }
    @Test void devuelve404EnActualizarYEliminarInexistente() throws Exception {
        assertThat(request("PUT", "/9223372036854775807", "{\"fecha\":\"2026-06-10\",\"idCliente\":1,\"total\":3679.8,\"detalles\":[{\"idProducto\":1,\"cantidad\":1,\"precio\":3500},{\"idProducto\":2,\"cantidad\":2,\"precio\":89.9}]}").statusCode()).isEqualTo(404);
        assertThat(request("DELETE", "/9223372036854775807", null).statusCode()).isEqualTo(404);
    }
    @Test void rechazaJsonYParametrosInvalidos() throws Exception {
        assertThat(request("POST", "", "{").statusCode()).isEqualTo(400);
        assertThat(request("GET", "/abc", null).statusCode()).isEqualTo(400);
        assertThat(request("GET", "/0", null).statusCode()).isEqualTo(400);
    }
}
