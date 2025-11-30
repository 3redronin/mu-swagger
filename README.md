mu-swagger
==========

A [Mu Server](https://muserver.io) plugin allowing JSON and YAML OpenAPI spec generation for 
JAX-RS resources to be created from Swagger annotations in the `io.swagger.v3.oas.annotations` 
package from the [Swagger Core](https://github.com/swagger-api/swagger-core/) project.

Note: this only generates JSON and YAML files describing your API, it does not provide a Swagger UI
to view and interact with the API.

**WARNING** this is a 0.x version of the plugin and the API may change in future releases.

Quick Start
-----------

Add the latest dependencies...

````xml
<dependency>
    <groupId>io.muserver</groupId>
    <artifactId>mu-server</artifactId>
    <version>RELEASE</version>
</dependency>
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-jaxrs2-jakarta</artifactId>
    <version>RELEASE</version>
</dependency>
````

...and then create a server with a JAX-RS rest handler.

````java
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
````

API Customization
-----------------

To add more information to your documentation, such as API title, version, contact details etc, you can
create an `io.swagger.v3.oas.models.OpenAPI` instance and pass it to the `MuOpenApiResourceBuilder` via
the `withOpenApi` method.

```java
var openApiInfo = new Info();
openApiInfo.setTitle("Example API");
openApiInfo.setDescription("Example API description");
var openApi = new OpenAPI();
openApi.setInfo(openApiInfo);

var muOpenApiResource = MuOpenApiResourceBuilder.muOpenApiResource()
    .withResources(resources)
    .withOpenApi(openApi)
    .build();
```

Example
-------

See [JacksonJsonExample.java](src/test/java/JacksonJsonExample.java) for an example that uses a
Jackson JSON provider, allowing endpoints to be executed from a swagger UI.

