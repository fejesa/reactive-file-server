package io.reactivefs.model;

/**
 * Defines the file create message that contains the payload along with the owners of the document.
 *
 * @param organizationId The organization id that identifies the organization level owner of the document.
 * @param userId The user id is the user level owner of the document.
 * @param fileName The name of the file.
 * @param payload The payload in Base64 format.
 */
public record DocumentCreateMessage(String organizationId, String userId, String fileName, String payload) {
}