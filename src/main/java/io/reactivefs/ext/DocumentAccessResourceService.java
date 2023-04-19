package io.reactivefs.ext;

import io.reactivefs.model.DocumentFileAccess;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
    Uni<DocumentFileAccess> getDocumentAccess(@HeaderParam(TOKEN_HEADER) String token, @PathParam("document") Long documentId);

    /**
     * Validates the provided apiKey sent by the application.
     *
     * @param apiKey The apiKey that must be validated.
     * @return lazy evaluated value: true if the key is valid
     */
    @GET
    @Path("key")
    Uni<Boolean> validateApiKey(@HeaderParam(API_KEY_HEADER) String apiKey);
}