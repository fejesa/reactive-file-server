package io.reactivefs.io;

import io.reactivefs.model.DocumentCreateRequest;
import io.smallrye.mutiny.Uni;

/**
 * Writes a document to the configured storage.
 */
public interface DocumentWriter {

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