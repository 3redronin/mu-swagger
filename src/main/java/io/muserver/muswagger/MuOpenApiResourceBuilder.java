package io.muserver.muswagger;

import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A builder for creating {@link MuOpenApiResource} instances to serve OpenAPI documentation
 * for JAX-RS resources in a Mu Server.
 */
public class MuOpenApiResourceBuilder {

    private String contextId;
    private OpenAPI openApi;
    private Collection<Object> resources;

    /**
     * @see #muOpenApiResource()
     */
    private MuOpenApiResourceBuilder() {}

    /**
     * Gets the context ID to use for this OpenAPI resource.
     * @return the context ID, or null to use the default context ID
     */
    public String contextId() {
        return contextId;
    }

    /**
     * Sets the context ID to use for this OpenAPI resource so that multiple OpenAPI resources can
     * coexist in the same application. Leave as null to use the default context ID.
     *
     * @param contextId a context ID to use for this OpenAPI resource, or null to use the default
     * @return this builder
     */
    public MuOpenApiResourceBuilder withContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    /**
     * Gets the OpenAPI definition to use.
     * @return the OpenAPI definition, or null if none has been set
     */
    public OpenAPI openApi() {
        return openApi;
    }

    /**
     * Sets the OpenAPI definition to use.
     *
     * <p>This allows you to programmatically create and configure the OpenAPI definition of things
     * such as info and servers. The components and resource paths will be appeneded to this definition
     * automatically.</p>
     *
     * <p>If not set, then no APi metadata such as API title will be set.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * var openApiInfo = new Info();
     * openApiInfo.setTitle("Example API");
     * openApiInfo.setDescription("Example API description");
     * var openApi = new OpenAPI();
     * openApi.setInfo(openApiInfo);
     *
     * var muOpenApiResource = MuOpenApiResourceBuilder.muOpenApiResource()
     *     .withOpenApi(openApi)
     *     .build();
     * </code></pre>
     *
     * @param openApi an OpenAPI definition
     * @return this builder
     */
    public MuOpenApiResourceBuilder withOpenApi(OpenAPI openApi) {
        this.openApi = openApi;
        return this;
    }

    /**
     * Gets the JAX-RS resources to generate documentation for.
     *
     * @return the resources to generate documentation for or null if not set
     */
    public Collection<Object> resources() {
        return resources;
    }

    /**
     * Sets the JAX-RS resources to generate documentation for.
     *
     * <p>These should be instances of objects with classes having a <code>@Path</code> annotation.</p>
     *
     * @param resources the resources to generate documentation for
     * @return This builder
     */
    public MuOpenApiResourceBuilder withResources(Collection<Object> resources) {
        this.resources = resources;
        return this;
    }

    /**
     * Builds the {@link MuOpenApiResource} instance.
     *
     * @return the OpenAPI resource that can be added to a {@link io.muserver.rest.RestHandlerBuilder}
     * @throws IllegalStateException if no JAX-RS resources have been set
     */
    public MuOpenApiResource build() {
        if (resources == null) {
            throw new IllegalStateException("No JAX-RS resources have been set for the OpenAPI resource");
        }
        var swaggerConfig = new SwaggerConfiguration();
        if (openApi != null) {
            swaggerConfig.setOpenAPI(openApi);
        }
        var initialConfig = new ConcurrentHashMap<String, String>();
        if (contextId != null) {
            initialConfig.put(OpenApiContext.OPENAPI_CONTEXT_ID_KEY, contextId);
        }
        var containerAttributes = new ConcurrentHashMap<String, Object>();
        return new MuOpenApiResource(resources, initialConfig, containerAttributes, swaggerConfig);
    }

    /**
     * Creates a new builder for a {@link MuOpenApiResource}.
     *
     * @return a new builder
     */
    public static MuOpenApiResourceBuilder muOpenApiResource() {
        return new MuOpenApiResourceBuilder();
    }

}
