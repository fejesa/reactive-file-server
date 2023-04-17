package io.fileserver.ext;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Delegates REST calls to the file access checker service (ACL).<p>
 */
@Path("/file-access")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "file-access-api")
public interface FileAccessResourceService {

    String TOKEN_HEADER = "Token";

    String API_KEY_HEADER = "ApiKey";

    /**
     * Checks if the given user can access to the given file.
     *
     * @param token used for the identification of the user
     * @param messageId identifier of the requested file
     * @return contains the information that is used for file identification or empty if the user has no access to the message
     */
    @GET
    @Path("file/{fileId}")
    Uni<UserFileAccess> getFileAccess(@HeaderParam(TOKEN_HEADER) String token, @PathParam("fileId") Long fileId);

    
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