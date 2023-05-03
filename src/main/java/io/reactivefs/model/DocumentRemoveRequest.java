package io.reactivefs.model;

/**
 * Defines the file removal request that contains all data for identifying the file that should be deleted from the storage.
 *
 * @param organizationId The organization id that identifies the organization level owner of the document.
 * @param userId The user id is the user level owner of the document.
 * @param fileName Name of the file that should be removed.
 */
public record DocumentRemoveRequest(String organizationId, String userId, String fileName) {

    public DocumentRemoveRequest {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId must not be null");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null");
        }
    }
}