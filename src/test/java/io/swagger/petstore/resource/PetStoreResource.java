/**
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package io.swagger.petstore.resource;

import io.swagger.petstore.data.PetData;
import io.swagger.petstore.data.StoreData;
import io.swagger.petstore.model.BadOrder;
import io.swagger.petstore.model.Order;
import io.swagger.petstore.model.Pet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/store")
@Produces({"application/json", "application/xml"})
@SecuritySchemes(value = {
        @SecurityScheme(name = "storeOpenIdConnect", type = SecuritySchemeType.OPENIDCONNECT,
                        description = "openId Connect authentication to access the pet store resource"
//                        openIdConnectUrl = "https://petstoreauth.com:4433/oidc/petstore/oidcprovider/authorize"
        ),
        @SecurityScheme(name = "storeHttp", type = SecuritySchemeType.HTTP,
                        description = "Basic http authentication to access the pet store resource", scheme = "basic")
})
@SecurityRequirements(value = {
        @SecurityRequirement(name = "storeOpenIdConnect", scopes = {"write:store", "read:store"}),
        @SecurityRequirement(name = "storeHttp")
})
public class PetStoreResource {
    static private StoreData storeData = new StoreData();
    static private PetData petData = new PetData();

    @GET
    @Path("/inventory")
    @Produces({"application/json", "application/xml"})
    @ApiResponses(extensions = @Extension(name = "x-responses-ext", properties = {@ExtensionProperty(name="x-responses-ext", value="test-responses-ext")}), value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                         extensions = {@Extension(name = "x-response-ext", properties = {@ExtensionProperty(name = "x-response-ext", value = "test-response-ext")})}),
            @ApiResponse(responseCode = "500", description = "server error", extensions = {}),
            @ApiResponse(responseCode = "503", description = "service not available",
                         content = @Content(extensions = @Extension(name = "x-notavailable-ext", properties = {@ExtensionProperty(name="x-notavailable-ext",value="true")}))),
    })
    @Operation(summary = "Returns pet inventories by status",
               description = "Returns a map of status codes to quantities")
    @Extension(name = "x-operation-ext", properties = {@ExtensionProperty(name="test-operation-ext", value="test-operation-ext")})
    public java.util.Map<String, Integer> getInventory() {
        return petData.getInventoryByStatus();
    }

    @GET
    @Path("/order/{orderId}")
    @Operation(summary = "Find purchase order by ID",
               description = "For valid response try integer IDs with value between the integers of 1 and 10. Other values will generated exceptions")
    @ApiResponse(responseCode = "200", description = "successful operation",
                 content = @Content(schema = @Schema(implementation = Order.class, allOf = {
                         Order.class, Pet.class}, not = BadOrder.class)))
    @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                 content = @Content(schema = @Schema(implementation = Order.class)))
    @ApiResponse(responseCode = "404", description = "Order not found",
                 content = @Content(schema = @Schema(implementation = Order.class, anyOf = {
                         Order.class,
                         BadOrder.class}, discriminatorProperty = "id",
                                                     discriminatorMapping = @DiscriminatorMapping(value = "0",
                                                                                                  schema = BadOrder.class))))
    @ApiResponse(responseCode = "599", description = "Invalid",
                 content = @Content(schema = @Schema(implementation = Order.class, hidden = true)))
    public Response getOrderById(
            @Parameter(name = "orderId", description = "ID of pet that needs to be fetched",
                       schema = @Schema(type = "integer", minimum = "1", maximum = "10"),
                       required = true) @PathParam("orderId") Long orderId)
            throws NotFoundException {
        Order order = storeData.findOrderById(orderId);
        if (null != order) {
            return Response.ok().entity(order).build();
        } else {
            throw new NotFoundException("Order not found");
        }
    }

    @POST
    @Path("/order")
    @Operation(summary = "Place an order for a pet")
    @ApiResponse(responseCode = "200", description = "successful operation")
    @ApiResponse(responseCode = "400", description = "Invalid Order")
    public Order placeOrder(
            @RequestBody(description = "order placed for purchasing the pet") Order order) {
        storeData.placeOrder(order);
        return storeData.placeOrder(order);
    }

    @DELETE
    @Path("/order/{orderId}")
    @ApiResponse(responseCode = "400", description = "Invalid ID supplied")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @Operation(summary = "Delete purchase order by ID",
               description = "For valid response try integer IDs with positive integer value. "
                       + "Negative or non-integer values will generate API errors")
    public Response deleteOrder(
            @Parameter(name = "orderId", description = "ID of the order that needs to be deleted",
                       schema = @Schema(type = "integer", minimum = "1"),
                       required = true) @PathParam("orderId") Long orderId) {
        if (storeData.deleteOrder(orderId)) {
            return Response.ok().entity("").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Order not found").build();
        }
    }
}
