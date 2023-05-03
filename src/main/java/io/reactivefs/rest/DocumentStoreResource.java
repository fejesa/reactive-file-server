package io.reactivefs.rest;

import io.reactivefs.model.DocumentCreateRequest;
import io.reactivefs.model.DocumentRemoveRequest;
import io.reactivefs.service.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static io.reactivefs.ext.DocumentAccessResourceService.API_KEY_HEADER;

/**
 * Defines the endpoints for the File Server that enable applications to store document files.<p>
 * The File Server does not offer neither session management and nor state management, but it authorizes
 * each request using the provided token by calling the access checker service (ACL).<p>
 * The documents are stored on the local file system.
 */
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentStoreResource {

    @UserDocument
    @Inject
    DocumentStore userDocumentStore;

    @Attachment
    @Inject
    DocumentStore attachmentDocumentStore;

    @UserDocument
    @Inject
    DocumentRemoval documentRemoval;

    @Inject
    ApiKeyCache apiKeyCache;

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Operation(
        summary = "Deletes user document from the storage.",
        description = "Delete the given user document from the local file storage asynchronously, and if there is a failure it is discarded. It calls the ACL service for authorizing the caller.")
    @APIResponse(
        responseCode = "202",
        description = "True if the caller has permission for the deletion.",
        content = @Content(mediaType = "application/json"))
    @APIResponse(
        responseCode = "400",
        description = "If the caller has no authorized to access to the service",
        content = @Content(mediaType = "application/json"))
    @DELETE
    @Path("document/{organizationId}/{userId}/{fileName}")
    public Uni<RestResponse<Boolean>> removeUserDocument(
        @Parameter(description = "The key that identifies the caller")
        @NotNull
        @HeaderParam(API_KEY_HEADER) String apiKey,
        @PathParam("organizationId") String organizationId, @PathParam("userId") String userId, @PathParam("fileName") String fileName) {
        return apiKeyCache
            .checkOrSet(apiKey)
            .call(() -> documentRemoval.remove(new DocumentRemoveRequest(organizationId, userId, fileName)))
            .map(__ -> RestResponse.ResponseBuilder.ok(true).status(RestResponse.Status.ACCEPTED).build())
            .onFailure()
            .recoverWithUni(this::logAndEmptyWrite)
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @Operation(
        summary = "Stores the given user document in the file store.",
        description = "Stores the document on the local file system. The document must be in Base64 format. It calls the ACL service for authorizing the caller.")
    @APIResponse(
        responseCode = "201",
        description = "The caller has write permission and the file was successfully written to the storage.",
        content = @Content(mediaType = "application/json"))
    @APIResponse(
        responseCode = "400",
        description = "If the sent document cannot be stored, or the caller has no authorized to access to the service.",
        content = @Content(mediaType = "application/json"))
    @POST
    @Path("document")
    public Uni<RestResponse<Boolean>> createUserDocument(
        @Parameter(description = "The key that identifies the caller")
        @NotNull
        @HeaderParam(API_KEY_HEADER) String apiKey,
        @RequestBody(description = "Contains the document content and metadata for calculating the file path in the storage.",
            content = @Content(schema = @Schema(implementation = DocumentCreateRequest.class, properties = {
                @SchemaProperty(name = "organizationId", description = "The organization ID that identifies the subfolder under the base storage.", example = "SampleOrg"),
                @SchemaProperty(name = "userId", description = "The user ID that is used for calculation of the subfolder under the organization folder.", example = "23453456"),
                @SchemaProperty(name = "fileName", description = "The name of the file."),
                @SchemaProperty(name = "content", description = "The document content in Base64 format.")
                })))
            DocumentCreateRequest createRequest) {
        return writeFile(apiKey, createRequest, userDocumentStore);
    }

    @Operation(
        summary = "Stores the given attachment in the file store.",
        description = "Stores the document on the local file system. The document must be in Base64 format. It calls the ACL service for authorizing the caller.")
    @APIResponse(
        responseCode = "201",
        description = "The caller has write permission and the file was successfully written to the storage.",
        content = @Content(mediaType = "application/json"))
    @APIResponse(
        responseCode = "400",
        description = "If the sent attachment cannot be stored, or the caller has no authorized to access to the service.",
        content = @Content(mediaType = "application/json"))
    @POST
    @Path("attachment")
    public Uni<RestResponse<Boolean>> createAttachment(
        @Parameter(description = "The key that identifies the caller")
        @NotNull
        @HeaderParam(API_KEY_HEADER) String apiKey,
        @RequestBody(description = "Contains the attachment and metadata for calculating the file path in the storage.",
            content = @Content(schema = @Schema(implementation = DocumentCreateRequest.class, properties = {
                @SchemaProperty(name = "organizationId", description = "The organization ID that identifies the subfolder under the base storage.", example = "SampleOrg"),
                @SchemaProperty(name = "fileName", description = "The name of the file."),
                @SchemaProperty(name = "content", description = "The attachment content in Base64 format")
            })))
            DocumentCreateRequest createRequest) {
        return writeFile(apiKey, createRequest, attachmentDocumentStore);
    }

    private Uni<RestResponse<Boolean>> writeFile(String apiKey, DocumentCreateRequest createRequest, DocumentStore store) {
        return apiKeyCache
            .checkOrSet(apiKey)
            .call(() -> store.write(createRequest))
            .map(__ -> RestResponse.ResponseBuilder.ok(true).status(RestResponse.Status.CREATED).build())
            .onFailure()
            .recoverWithUni(this::logAndEmptyWrite)
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private Uni<RestResponse<Boolean>> logAndEmptyWrite(Throwable failure) {
        logger.error("Document write/delete error", failure);
        return Uni.createFrom().item(RestResponse.ResponseBuilder.ok(false).status(RestResponse.Status.BAD_REQUEST).build());
    }
}
