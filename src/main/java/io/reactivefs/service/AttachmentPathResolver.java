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

    @Override
    public Path resolve(String organizationId, String userId, String fileName) {
        return Paths.get(attachmentRootDirectory, organizationId.toLowerCase(), fileName);
    }
}
