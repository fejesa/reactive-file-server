package io.reactivefs.service;

import io.reactivefs.RFSConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;
import java.nio.file.Paths;

@Attachment
@ApplicationScoped
public class AttachmentPathResolver implements DocumentPathResolver {

    @ConfigProperty(name = RFSConfig.ATTACHMENT_DOCUMENT_ROOT_DIRECTORY)
    String attachmentRootDirectory;

    /**
     * Resolves the given attachment file path.
     * <p>Attachment documents can be accessed a group of users within the organization, user ID is not used for file path identification.
     *
     * @param organizationId The organization id that identifies the organization level owner of the document.
     * @param userId The user id is the user level owner of the document.
     * @param fileName The name of the file.
     * @return The absolute path of the requested attachment file.
     */
    @Override
    public Path resolve(String organizationId, String userId, String fileName) {
        return Paths.get(attachmentRootDirectory, organizationId.toLowerCase(), fileName);
    }
}
