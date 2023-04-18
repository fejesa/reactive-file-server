package org.rfs.model;

/**
 * Defines the file creational message that contains the payload along with the owner and organization.
 *
 * @param organizationId The organization id that identifies the subfolder under the base storage.
 * @param userId Used for calculation of the subfolder under the organization folder.
 * @param fileName The name of the file.
 * @param payload The payload in Base64 format.
 */
public record DocumentCreateMessage(String organizationId, String userId, String fileName, String payload) {
}