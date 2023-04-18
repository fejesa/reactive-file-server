package org.rfs.io;

import org.rfs.model.DocumentCreateMessage;
import io.smallrye.mutiny.Uni;

/**
 * Writes a document to the configured storage.
 */
public interface DocumentWriter {

    /**
     * Writes the pushed document to the configured storage. If the document path does not exist, then creates it.
     *
     * @param documentCreateMessage contains the information that is used for the building the file path and the document payload as well
     * @return The lazy asynchronous action that the caller can subscribe to.
     * @throws io.vertx.core.file.FileSystemException if the file cannot be written to the storage
     * @throws IllegalArgumentException               if file parameter is invalid
     */
    Uni<Void> write(DocumentCreateMessage documentCreateMessage);
}