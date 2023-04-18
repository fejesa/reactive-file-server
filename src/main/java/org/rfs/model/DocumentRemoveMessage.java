package org.rfs.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines the file removal message that contains all data for idetifying the file that should be deleted from the storage.
 *
 * @param organizationId The university code that identifies the subfolder under the base storage.
 * @param userId The user identifier that is used for calculation of the subfolder under the organization folder.
 * @param fileName Name of the file that should be removed.
 */
public record DocumentRemoveMessage(String organizationId, String userId, String fileName) {

    public DocumentRemoveMessage {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        if (StringUtils.isBlank(organizationId)) {
            throw new IllegalArgumentException("organizationId must not be null or blank");
        }
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName must not be null or blank");
        }
    }
}