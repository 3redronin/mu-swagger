package io.muserver.muswagger;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.muserver.MuServerBuilder.muServer;
import static io.muserver.rest.RestHandlerBuilder.restHandler;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MuOpenApiResourceTest {

    @Test
    void documentsOnlyResourcesRegisteredWithTheRestHandler() throws Exception {
        List<Object> resources = List.of(new RegisteredResource());
        var openApiResource = MuOpenApiResourceBuilder.muOpenApiResource()
            .withContextId(UUID.randomUUID().toString())
            .withResources(resources)
            .build();

        var server = muServer()
            .withHttpPort(0)
            .addHandler(restHandler(resources.toArray()).addResource(openApiResource))
            .start();
        try (var client = HttpClient.newHttpClient()) {
            var registeredResponse = get(client, server.uri().resolve("/registered"));
            assertEquals(200, registeredResponse.statusCode());
            assertEquals("registered", registeredResponse.body());
            assertEquals(404, get(client, server.uri().resolve("/unregistered")).statusCode());

            var response = get(client, server.uri().resolve("/openapi.json"));
            assertEquals(200, response.statusCode());
            var openApi = Json.mapper().readValue(response.body(), OpenAPI.class);
            assertEquals(Set.of("/registered"), openApi.getPaths().keySet(),
                "OpenAPI must only document registered resources, excluding other classpath resources");
        } finally {
            server.stop();
        }
    }

    private static HttpResponse<String> get(HttpClient client, URI uri) throws Exception {
        return client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    @Path("/registered")
    public static class RegisteredResource {
        @GET
        @Produces("text/plain")
        public String get() {
            return "registered";
        }
    }

    // Deliberately on the classpath but never added to either builder.
    @Path("/unregistered")
    public static class UnregisteredResource {
        @GET
        @Produces("text/plain")
        public String get() {
            return "unregistered";
        }
    }
}
