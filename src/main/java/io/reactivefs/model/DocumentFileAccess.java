package io.reactivefs.model;

import java.io.Serializable;

/**
 * Defines the document access parameters.<p>
 *
 * Every user belongs to an organization, and each organization has its own file directory located within the root folder.<p>
 * When the ACL service is contacted with the token and resource ID parameters, the response retrieved is the {@link DocumentFileAccess}
 * @apiNote If the user has no access to the given document then the {@link DocumentFileAccess} is empty.
 *
 * @param organizationId The organization id that identifies the organization level owner of the document.
 * @param userId The user id is the user level owner of the document.
 * @param fileName The name of the file that should be accessed.
 */
public record DocumentFileAccess(String organizationId, String userId, String fileName) implements Serializable {

    public DocumentFileAccess {
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

    @Override
    public String toString() {
        return "DocumentFileAccess [organizationId=" + organizationId + ",  userId=" + userId + ", fileName=" + fileName + "]";
    }

}
