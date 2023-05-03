package io.reactivefs.model;

/**
 * Defines the file create request that contains the content along with the owners of the document.
 *
 * @param organizationId The organization id that identifies the organization level owner of the document.
 * @param userId The user id is the user level owner of the document.
 * @param fileName The name of the file.
 * @param content The content in Base64 format.
 */
public record DocumentCreateRequest(String organizationId, String userId, String fileName, String content) {
}