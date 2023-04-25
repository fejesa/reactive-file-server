package io.reactivefs.service;

import java.nio.file.Path;

/**
 * This establishes the method for identifying the path of a document.<p>
 * Various document types may be stored in different folders, such as each user having their
 * own folder or multiple users sharing a common folder for their private documents.
 * Documents at the organizational level may be stored differently as well.
 */
@FunctionalInterface
public interface DocumentPathResolver {

    /**
     * Resolves the path of the given document file.
     *
     * @param organizationId The organization id that identifies the organization level owner of the document.
     * @param userId The user id is the user level owner of the document.
     * @param fileName The name of the file.
     * @return The path of the given document file on the file storage.
     */
    Path resolve(String organizationId, String userId, String fileName);
}
