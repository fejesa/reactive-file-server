package io.reactivefs.rest;

import io.reactivefs.ext.DocumentAccessResourceService;
import io.reactivefs.model.DocumentFileAccess;
import io.reactivefs.service.DocumentStore;
import io.reactivefs.service.UserDocument;
import io.reactivefs.RFSConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.function.BiFunction;

import static io.reactivefs.ext.DocumentAccessResourceService.TOKEN_HEADER;

/**
 * Defines the endpoints for the File Server that enable users to fetch user or organization level documents.<p>
 * The File Server does not offer session management and has no state management, but it authorizes
 * each request using the provided token by calling the access checker service (ACL).<p>
 * The documents are stored on the local file system. If the caller or the document cannot be identified,
 * or the document does not exist, or the user has no permission to access the document, then an empty response (HTTP 204) is returned.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class DocumentFileAccessResource {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** Initial interval, wait for the first retry */
    @ConfigProperty(name = RFSConfig.RETRY_INITIAL_BACKOFF_MS, defaultValue = "200")
    int RETRY_INITIAL_BACKOFF_MS;

    /** The absolute time in millis that specifies when to give up the retry */
    @ConfigProperty(name = RFSConfig.RETRY_EXPIRATION_MS, defaultValue = "2000")
    int RETRY_EXPIRATION_MS;

    @RestClient
    DocumentAccessResourceService fileAccessService;

    @Inject
    @UserDocument
    DocumentStore documentStore;

    @GET
    @Path("document/{documentId}")
    public Uni<RestResponse<byte[]>> getUserDocument(
            @Parameter(description = "Signed token in Base 64 format that used for identification of the user")
            @NotNull
            @HeaderParam(TOKEN_HEADER) String token,
            @Parameter(description = "Identifier of the requested message document")
            @PathParam("documentId") Long messageId) {
        return readFile(token, messageId, fileAccessService::getUserDocumentAccess, documentStore);
    }

    /**
     * This function verifies the access rights of the provided user for the given document
     * and retrieves the file content.<p>
     * The user is identified by the token provided through a remote service endpoint
     * of the Access Control List (ACL) server. If the user cannot be identified or does
     * not have permission to access the requested document, the function returns null.<p>
     * In the event that the remote endpoint call fails, the function will retry using
     * a configured exponential backoff. If the requested file is not available on the local file system, it also returns null.
     *
     * @param token         used for identification of the user
     * @param id            identifier of the requested document
     * @param fileAccess    defines the remote service endpoint call
     * @param documentStore used for reading the requested document from the local file system
     * @return document content or null if user has no permission or the file is not available
     */
    private Uni<RestResponse<byte[]>> readFile(String token, Long id, BiFunction<String, Long, Uni<DocumentFileAccess>> fileAccess, DocumentStore documentStore) {
        return fileAccess.apply(token, id)
            .onFailure()
            .retry()
            .withBackOff(Duration.ofMillis(RETRY_INITIAL_BACKOFF_MS))
            .expireIn(RETRY_EXPIRATION_MS)
            .flatMap(documentStore::read)
            .map(Buffer::getBytes)
            .map(b -> RestResponse.ResponseBuilder.ok(b).build())
            .onFailure()
            .recoverWithUni(this::logAndEmpty)
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private Uni<RestResponse<byte[]>> logAndEmpty(Throwable failure) {
        logger.error("User file access error", failure);
        return Uni.createFrom()
                .item(RestResponse.ResponseBuilder.ok(Buffer.buffer().getBytes()).status(RestResponse.Status.NOT_FOUND).build());
    }
}
