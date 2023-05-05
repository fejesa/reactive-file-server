package io.reactivefs.service;

import io.reactivefs.RFSConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;
import java.nio.file.Paths;

@PerformanceResult
@ApplicationScoped
public class PerformanceResultPathResolver implements DocumentPathResolver {

    @ConfigProperty(name = RFSConfig.PERFORMANCE_DOCUMENT_ROOT_DIRECTORY)
    String performanceDocumentRootDirectory;

    /**
     * Resolves the given performance result file path.
     * <p>User performance result or report document is stored under the organization folder, and the user ID is the file name.
     *
     * @param organizationId The organization id that identifies the organization level owner of the document.
     * @param userId The user id is the user level owner of the document.
     * @param fileName The name of the file - not used
     * @return The absolute path of the given performance result document file.
     */
    @Override
    public Path resolve(String organizationId, String userId, String fileName) {
        return Paths.get(performanceDocumentRootDirectory, organizationId.toLowerCase(), userId.toLowerCase());
    }
}
