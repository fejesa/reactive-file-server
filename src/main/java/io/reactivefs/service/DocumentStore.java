package io.reactivefs.service;

import io.reactivefs.model.DocumentCreateRequest;
import io.reactivefs.model.DocumentFileAccess;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;

/**
 * Defines the API for storing and accessing documents.
 */
public interface DocumentStore {

    /**
     * Reads the given document from the storage that the downstream application can consume.<p>
     * @apiNote we assume that the average size of the documents is acceptable and does not cause memory issues.
     *
     * @param fileAccess contains the information that is used for the identification of the requested file
     * @return the file payload
     * @throws io.vertx.core.file.FileSystemException if the file does not exist
     * @throws IllegalArgumentException               if file access parameter is invalid
     */
    Uni<Buffer> read(DocumentFileAccess fileAccess);

    /**
     * Writes the provided document to the configured storage. If the document path does not exist, it creates automatically.
     *
     * @param createRequest contains the document payload and the information about the owner of the document, like organization and user
     * @return The lazy asynchronous action that the caller can subscribe to.
     * @throws io.vertx.core.file.FileSystemException if the file cannot be written to the storage
     * @throws IllegalArgumentException               if file parameter is invalid
     */
    Uni<Void> write(DocumentCreateRequest createRequest);
}
