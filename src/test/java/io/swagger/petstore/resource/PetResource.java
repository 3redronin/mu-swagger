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

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.callbacks.Callback;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.petstore.data.PetData;
import io.swagger.petstore.model.*;

import java.io.IOException;
import java.io.OutputStream;

@Path("/pet")
@SecuritySchemes(value = {
        @SecurityScheme(name = "petsApiKey", type = SecuritySchemeType.APIKEY,
                        description = "authentication needed to create a new pet profile for the store",
                        paramName = "createPetProfile", in = SecuritySchemeIn.HEADER),
        @SecurityScheme(name = "petsOAuth2", type = SecuritySchemeType.OAUTH2,
                        description = "authentication needed to delete a pet profile",
                        flows = @OAuthFlows(implicit = @OAuthFlow(authorizationUrl = "https://example.com/api/oauth/dialog",
                                                                  scopes = {}),
                                            authorizationCode = @OAuthFlow(authorizationUrl = "https://example.com/api/oauth/dialog",
                                                                           tokenUrl = "https://example.com/api/oauth/token",
                                                                           scopes = {}))),
        @SecurityScheme(name = "petsHttp", type = SecuritySchemeType.HTTP,
                        description = "authentication needed to update an exsiting record of a pet in the store",
                        scheme = "bearer", bearerFormat = "jwt")
})
public class PetResource {

    static private PetData petData = new PetData();

    @GET
    @Path("/{petId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                         content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "404", description = "Pet not found", content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "200",
                         description = "Pet found",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(type = "object", implementation = Pet.class,
                                                             oneOf = {
                                                                     Cat.class, Dog.class, Lizard.class},
                                                             readOnly = true)))
    })
    @Operation(summary = "Find pet by ID", description = "Returns a pet when ID is less than or equal to 10")
    public Response getPetById(
            @Parameter(name = "petId", description = "ID of pet that needs to be fetched", required = true,
                       example = "1",
                       schema = @Schema(implementation = Long.class, maximum = "101", exclusiveMaximum = true,
                                        minimum = "9", exclusiveMinimum = true,
                                        multipleOf = 10)) @PathParam("petId") Long petId)
            throws NotFoundException {
        Pet pet = petData.getPetById(petId);
        if (pet != null) {
            return Response.ok().entity(pet).build();
        } else {
            throw new NotFoundException("Pet not found");
        }
    }

    @GET
    @Path("/{petId}/download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                         content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "404", description = "Pet not found", content = @Content(mediaType = "none"))
    })
    @Operation(summary = "Find pet by ID and download it",
               description = "Returns a pet when ID is less than or equal to 10")
    public Response downloadFile(
            @Parameter(name = "petId", description = "ID of pet that needs to be fetched", required = true,
                       schema = @Schema(implementation = Long.class, maximum = "10",
                                        minimum = "1")) @PathParam("petId") Long petId)
            throws NotFoundException {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                try {
                    // TODO: write file content to output;
                    output.write("hello, world".getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return Response.ok(stream, "application/force-download")
                .header("Content-Disposition", "attachment; filename = foo.bar")
                .build();
    }

    @DELETE
    @Path("/{petId}")
    @SecurityRequirement(name = "petsOAuth2", scopes = "write:pets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    @Operation(summary = "Deletes a pet by ID", description = "Returns a pet when ID is less than or equal to 10")
    public Response deletePet(
            @Parameter(name = "apiKey", description = "authentication key to access this method",
                       schema = @Schema(type = "string", implementation = String.class, maxLength = 256,
                                        minLength = 32)) @HeaderParam("apiKey") String apiKey,
            @Parameter(name = "petId", description = "ID of pet that needs to be fetched", required = true,
                       schema = @Schema(implementation = Long.class, maximum = "10",
                                        minimum = "1")) @PathParam("petId") Long petId) {
        if (petData.deletePet(petId)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    @SecurityRequirement(name = "petsApiKey")
    @ApiResponse(responseCode = "400", description = "Invalid input",
                 content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiMetaResponse.class)))
    @SuppressWarnings("checkstyle:linelength")
    @RequestBody(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Pet.class),
                                    examples = @ExampleObject(ref = "http://example.org/petapi-examples/openapi.json#/components/examples/pet-example")),
                 description = "example of a new pet to add")
    @Operation(summary = "Add pet to store", description = "Add a new pet to the store")
    public Response addPet(
        @RequestBody(description = "Attribute to update existing pet record",
            content = @Content(schema = @Schema(implementation = Pet.class)))
        Pet pet) {
        Pet updatedPet = petData.addPet(pet);
        return Response.ok().entity(updatedPet).build();
    }

    @PUT
    @Consumes({"application/json", "application/xml"})
    @SecurityRequirement(name = "petsHttp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                         content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Pet not found",
                         content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "405", description = "Validation exception",
                         content = @Content(mediaType = "application/json"))
    })
    @Operation(summary = "Update an existing pet", description = "Update an existing pet with the given new attributes")
    public Response updatePet(
            @RequestBody(description = "Attribute to update existing pet record",
                         content = @Content(schema = @Schema(implementation = Pet.class))) Pet pet) {
        Pet updatedPet = petData.addPet(pet);
        return Response.ok().entity(updatedPet).build();
    }

    @GET
    @Path("/findByTags")
    @Produces("application/json")
    @Callback(name = "tagsCallback", callbackUrlExpression = "http://petstoreapp.com/pet",
              operation ={ @Operation(method = "GET", summary = "Finds Pets by tags",
                                              description = "Find Pets by tags; Muliple tags can be provided with comma seperated strings. "
                                                      + "Use tag1, tag2, tag3 for testing.",
                                              responses = {
                                                      @ApiResponse(responseCode = "400",
                                                                   description = "Invalid tag value",
                                                                   content = @Content(mediaType = "none")),
                                                      @ApiResponse(responseCode = "200",
                                                                   description = "Pet callback successfully processed",
                                                                   content = @Content(mediaType = "application/json",
                                                                                      schema = @Schema(type = "array",
                                                                                                       implementation = Pet.class)))
                                              })})
    @Schema(implementation = Pet[].class)
    @Deprecated
    public Response findPetsByTags(
            @HeaderParam("apiKey") String apiKey,
            @Parameter(name = "tags", description = "Tags to filter by", required = true, deprecated = true,
                       schema = @Schema(implementation = String.class, deprecated = true,
                                        externalDocs = @ExternalDocumentation(description = "Pet Types",
                                                                              url = "http://example.com/pettypes"),
                                        allowableValues = {
                                                "Cat", "Dog", "Lizard"},
                                        defaultValue = "Dog")) @QueryParam("tags") String tags) {
        return Response.ok(petData.findPetByTags(tags)).build();
    }

    @POST
    @Path("/{petId}")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @ApiResponse(responseCode = "405", description = "Validation exception", content = @Content(mediaType = "none"))
    @Operation(summary = "Updates a pet in the store with form data")
    public Response updatePetWithForm(
            @Parameter(name = "petId", description = "ID of pet that needs to be updated",
                       required = true) @PathParam("petId") Long petId,
            @Parameter(name = "name", description = "Updated name of the pet") @FormParam("name") String name,
            @Parameter(name = "status", description = "Updated status of the pet") @FormParam("status") String status) {
        Pet pet = petData.getPetById(petId);
        if (pet != null) {
            if (name != null && !"".equals(name)) {
                pet.setName(name);
            }
            if (status != null && !"".equals(status)) {
                pet.setStatus(status);
            }
            petData.addPet(pet);
            return Response.ok().build();
        } else {
            return Response.status(404).build();
        }
    }

    @POST
    @Path("/{petId}")
    @Consumes({"text/csv"})
    @Produces({"text/csv"})
    @ApiResponse(content = {@Content(schema = @Schema(implementation = Pet.class))}, responseCode = "204")
    @Operation(summary = "Updates a pet in the store with CSV data")
    public Response updatePetWithCsv(
            @Parameter(name = "petId", description = "ID of pet that needs to be updated",
                       required = true) @PathParam("petId") Long petId,
            @Schema(implementation = Pet.class) String commaSeparatedValues) {
        Pet pet = petData.getPetById(petId);
        if (pet != null) {
            String[] values = commaSeparatedValues.split(",");
            String name = values[2];
            String status = values[5];

            if (name != null && !"".equals(name)) {
                pet.setName(name);
            }
            if (status != null && !"".equals(status)) {
                pet.setStatus(status);
            }
            petData.addPet(pet);
            return Response.ok().build();
        } else {
            return Response.status(404).build();
        }
    }
}
