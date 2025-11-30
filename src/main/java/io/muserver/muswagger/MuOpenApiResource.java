package io.muserver.muswagger;

import io.muserver.MuRequest;
import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.*;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * A JAX-RS resource that serves the OpenAPI specification for the application.
 *
 * <p>This resource exposes two <code>GET</code> resources:</p>
 * <ul>
 *     <li><code>/openapi.json</code> - serves the OpenAPI specification in JSON format.</li>
 *     <li><code>/openapi.yaml</code> - serves the OpenAPI specification in YAML format.</li>
 * </ul>
 *
 * <p>Usage instructions:</p>
 *
 * <ol>
 *     <li>Create a {@link MuOpenApiResourceBuilder} with {@link MuOpenApiResourceBuilder#muOpenApiResource()}</li>
 *     <li>Add your JAX-RS singleton resources to the builer with {@link MuOpenApiResourceBuilder#withResources(Collection)}</li>
 *     <li><em>Optional:</em> set some extra API information with {@link MuOpenApiResourceBuilder#withOpenApi(OpenAPI)}</li>
 *     <li>Build the resource with {@link MuOpenApiResourceBuilder#build()}</li>
 *     <li>Add the resulting resource to your MuServer's {@link io.muserver.rest.RestHandlerBuilder} with the {@link io.muserver.rest.RestHandlerBuilder#addResource(Object...)} method.</li>
 * </ol>
 */
@Path("/openapi.{type:json|yaml}")
public class MuOpenApiResource extends BaseOpenApiResource {

    private final Application application;
    private final ConcurrentMap<String, String> initialConfig;
    private final ConcurrentMap<String, Object> containerAttributes;

    MuOpenApiResource(Collection<Object> singletons, ConcurrentMap<String, String> initialConfig, ConcurrentMap<String, Object> containerAttributes, SwaggerConfiguration swaggerConfig) {
        this.application = new ApplicationAdaptor(Set.copyOf(singletons));
        this.initialConfig = initialConfig;
        this.containerAttributes = containerAttributes;
        setOpenApiConfiguration(swaggerConfig);
    }

    // why javadoc? because the javadoc generator generates warnings for public methods on public classes like this
    /**
     * Handles GET requests to retrieve the OpenAPI specification in the requested format (JSON or YAML).
     *
     * @param headers request headers
     * @param uriInfo requested URI information
     * @param type the format type (json or yaml)
     * @param muRequest the MuRequest
     * @return a Response containing the OpenAPI specification
     * @throws Exception if an error occurs while generating the specification
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    @Operation(hidden = true)
    public Response getOpenApi(@Context HttpHeaders headers,
                               @Context UriInfo uriInfo,
                               @PathParam("type") String type,
                               @Context MuRequest muRequest) throws Exception {
        var servletContextAdaptor = new ServletContextAdaptor(muRequest, initialConfig, containerAttributes);
        var config = new ServletConfigAdaptor(initialConfig, servletContextAdaptor);
        return super.getOpenApi(headers, config, application, uriInfo, type);
    }

}
