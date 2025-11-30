import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import io.muserver.MuServer;
import io.muserver.muswagger.MuOpenApiResourceBuilder;
import io.muserver.rest.CORSConfigBuilder;
import io.swagger.petstore.resource.PetResource;
import io.swagger.petstore.resource.PetStoreResource;
import io.swagger.petstore.resource.UserResource;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import java.util.List;

import static io.muserver.MuServerBuilder.muServer;
import static io.muserver.rest.RestHandlerBuilder.restHandler;

public class JacksonJsonExample {

    public static void main(String[] args) {

        var resources = List.of(new PetStoreResource(), new PetResource(), new UserResource());
        var mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();
        var jsonProvider = new JacksonJsonProvider();
        jsonProvider.setMapper(mapper);

        var openApiInfo = new Info();
        openApiInfo.setTitle("Example API");
        openApiInfo.setDescription("Example API description");
        var openApi = new OpenAPI();
        openApi.setInfo(openApiInfo);

        var muOpenApiResource = MuOpenApiResourceBuilder.muOpenApiResource()
            .withResources(resources)
            .withOpenApi(openApi)
            .build();

        MuServer server = muServer()
            .withHttpPort(12080)
            .addHandler(restHandler(resources.toArray())
                .addResource(muOpenApiResource)
                .addCustomWriter(jsonProvider)
                .addCustomReader(jsonProvider)
                .withCORS(CORSConfigBuilder.corsConfig()
                    .withAllowedHeaders("content-type")
                    .withAllowedOrigins("https://petstore.swagger.io"))
            )
            .start();

        System.out.println("Started at " + server.uri().resolve("/openapi.json"));
        System.out.println("View it at https://petstore.swagger.io/?url=http://localhost:12080/openapi.json");

    }
}

