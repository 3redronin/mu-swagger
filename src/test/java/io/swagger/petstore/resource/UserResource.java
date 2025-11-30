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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.petstore.data.UserData;
import io.swagger.petstore.model.User;

@Path("/user")
@Produces({"application/json", "application/xml"})
@SecuritySchemes(value = {
        @SecurityScheme(name = "userApiKey", type = SecuritySchemeType.APIKEY,
                        description = "authentication needed to create a new user profile for the store",
                        paramName = "createOrUpdateUserProfile1", in = SecuritySchemeIn.HEADER),
        @SecurityScheme(name = "userBasicHttp", type = SecuritySchemeType.HTTP,
                        description = "authentication needed to create a new user profile for the store",
                        scheme = "basic"),
        @SecurityScheme(name = "userBearerHttp", type = SecuritySchemeType.HTTP,
                        description = "authentication needed to create a new user profile for the store",
                        scheme = "bearer", bearerFormat = "JWT")
})
public class UserResource {
    private static UserData userData = new UserData();

    @POST
    @ApiResponse(description = "successful operation")
    @Operation(summary = "Create user", description = "This can only be done by the logged in user.")
    @SecurityRequirements(value = {
            @SecurityRequirement(name = "userApiKey"),
            @SecurityRequirement(name = "userBasicHttp"),
            @SecurityRequirement(name = "userBearerHttp")
    })
    public Response createUser(
            @RequestBody(description = "Created user object",
                         content = @Content(schema = @Schema(ref = "#/components/schemas/User"))) User user) {
        userData.addUser(user);
        return Response.ok().entity("").build();
    }

    @POST
    @Path("/createWithArray")
    @ApiResponse(description = "successful operation")
    @Operation(summary = "Creates list of users with given input array")
    public Response createUsersWithArrayInput(
            @RequestBody(description = "List of user object", required = true) User[] users) {
        for (User user : users) {
            userData.addUser(user);
        }
        return Response.ok().entity("").build();
    }

    @POST
    @Path("/createWithList")
    @ApiResponse(description = "successful operation")
    @Operation(summary = "Creates list of users with given input array")
    public Response createUsersWithListInput(
            @RequestBody(description = "List of user object", required = true) java.util.List<User> users) {
        for (User user : users) {
            userData.addUser(user);
        }
        return Response.ok().entity("").build();
    }

    @PUT
    @Path("/{username}")
    @ApiResponse(responseCode = "400", description = "Invalid user supplied")
    @ApiResponse(responseCode = "404", description = "User not found")
    @Operation(summary = "Updated user", description = "This can only be done by the logged in user.")
    @SecurityRequirements(value = {
            @SecurityRequirement(name = "userApiKey"),
            @SecurityRequirement(name = "userBearerHttp")
    })
    public Response updateUser(
            @Parameter(name = "username", description = "name that need to be deleted",
                       schema = @Schema(type = "string"),
                       required = true) @PathParam("username") String username,
            @RequestBody(description = "Updated user object") User user) {
        userData.addUser(user);
        return Response.ok().entity("").build();
    }

    @DELETE
    @Path("/{username}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid username supplied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @Operation(summary = "Delete user", description = "This can only be done by the logged in user.")
    public Response deleteUser(
            @Parameter(name = "username", description = "The name that needs to be deleted",
                       schema = @Schema(type = "string"),
                       required = true) @PathParam("username") String username) {
        if (userData.removeUser(username)) {
            return Response.ok().entity("").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{username}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                         content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid username supplied",
                         content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                         content = @Content(schema = @Schema(implementation = User.class)))
    })
    @Operation(summary = "Get user by user name")
    public Response getUserByName(
            @Parameter(name = "username", description = "The name that needs to be fetched. Use user1 for testing.",
                       schema = @Schema(type = "string"),
                       required = true) @PathParam("username") String username)
            throws NotFoundException {
        User user = userData.findUserByName(username);
        if (null != user) {
            return Response.ok().entity(user).build();
        } else {
            throw new NotFoundException("User not found");
        }
    }

    @GET
    @Path("/login")
    @ApiResponse(responseCode = "200", description = "successful operation",
                 content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400", description = "Invalid username/password supplied",
                 content = @Content(schema = @Schema(implementation = String.class)))
    @Operation(summary = "Logs user into the system")
    public Response loginUser(
            @Parameter(name = "username", description = "The user name for login",
                       schema = @Schema(type = "string"),
                       required = true) @QueryParam("username") String username,
            @Parameter(name = "password", description = "The password for login in clear text",
                       schema = @Schema(type = "string"),
                       required = true) @QueryParam("password") String password) {
        return Response.ok()
                .entity("logged in user session:" + System.currentTimeMillis())
                .build();
    }

    @GET
    @Path("/logout")
    @ApiResponse(description = "successful operation")
    @Operation(summary = "Logs out current logged in user session")
    public Response logoutUser() {
        return Response.ok().entity("").build();
    }
}
