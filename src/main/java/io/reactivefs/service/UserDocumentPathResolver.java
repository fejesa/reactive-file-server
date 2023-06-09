package io.reactivefs.service;

import io.reactivefs.RFSConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;
import java.nio.file.Paths;

@UserDocument
@ApplicationScoped
public class UserDocumentPathResolver implements DocumentPathResolver {

    @ConfigProperty(name = RFSConfig.USER_DOCUMENT_ROOT_DIRECTORY)
    String userDocumentRootDirectory;

    /**
     * Suppose the user ID is a fixed length of 7 characters, such as 2312345, and instead of creating
     * a separate folder for each user to store their documents, we will use the last 2 characters of the
     * user ID to name the folder. In this scenario, users 2312345 and 3423945 would share a common folder named "45".
     */
    @Override
    public Path resolve(String organizationId, String userId, String fileName) {
        return Paths.get(userDocumentRootDirectory, organizationId.toLowerCase(), userId.toLowerCase().substring(5), fileName);
    }
}
