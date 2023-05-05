package io.reactivefs.ext;

import io.reactivefs.model.ApplicationAuth;
import io.reactivefs.model.DocumentFileAccess;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


/**
 * Delegates REST calls to the document access checker service (ACL).<p>
 */
@Path("/document-access")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "document-access-api")
public interface DocumentAccessResourceService {

    String TOKEN_HEADER = "Token";

    String API_KEY_HEADER = "ApiKey";

    /**
     * Checks if the given user has access right to the given document.
     *
     * @param token used for the identification of the user
     * @param documentId identifier of the requested document
     * @return contains the information that is used for document file identification or empty if the user has no access to it
     */
    @GET
    @Path("document/{documentId}")
    Uni<DocumentFileAccess> getUserDocumentAccess(@HeaderParam(TOKEN_HEADER) String token, @PathParam("documentId") Long documentId);

    /**
     * Checks if the given user can access to the given attachment file.
     *
     * @param token used for the identification of the user
     * @param attachmentId identifier of the requested attachment document
     * @return contains the information that is used for file identification or empty if the user has no access to the attachment
     */
    @GET
    @Path("attachment/{attachmentId}")
    Uni<DocumentFileAccess> getAttachmentAccess(@HeaderParam(TOKEN_HEADER) String token, @PathParam("attachmentId") Long attachmentId);

    /**
     * Checks if the given user can access to the given performance result file.
     *
     * @param token used for the identification of the user
     * @return contains the information that is used for file identification or empty if the user cannot be identified
     */
    @GET
    @Path("performance-document")
    Uni<DocumentFileAccess> getPerformanceResultAccess(@HeaderParam(TOKEN_HEADER) String token);

    /**
     * Validates the provided apiKey sent by the application.
     *
     * @param apiKey The apiKey that must be validated.
     * @return lazy evaluated value, the application is authorized to execute write operation if the key is valid
     */
    @GET
    @Path("key")
    Uni<ApplicationAuth> validateApiKey(@HeaderParam(API_KEY_HEADER) String apiKey);
}