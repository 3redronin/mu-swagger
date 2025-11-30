import io.muserver.MuServer;
import io.muserver.muswagger.MuOpenApiResource;
import io.muserver.muswagger.MuOpenApiResourceBuilder;
import io.muserver.rest.CORSConfigBuilder;
import io.swagger.petstore.resource.PetResource;
import io.swagger.petstore.resource.PetStoreResource;
import io.swagger.petstore.resource.UserResource;

import java.util.List;

import static io.muserver.MuServerBuilder.muServer;
import static io.muserver.rest.RestHandlerBuilder.restHandler;

public class SmallExample {

    public static void main(String[] args) {

        List<Object> resources = List.of(new PetStoreResource(), new PetResource(), new UserResource());

        MuOpenApiResource muOpenApiResource = MuOpenApiResourceBuilder.muOpenApiResource()
            .withResources(resources)
            .build();

        MuServer server = muServer()
            .withHttpPort(12080)
            .addHandler(restHandler(resources.toArray())
                .addResource(muOpenApiResource)
                .withCORS(CORSConfigBuilder.corsConfig()
                    .withAllowedOrigins("https://petstore.swagger.io"))
            )
            .start();

        System.out.println("Started at " + server.uri().resolve("/openapi.json"));
        System.out.println("View it at https://petstore.swagger.io/?url=http://localhost:12080/openapi.json");
    }
}

