package io.reactivefs.io;

import io.reactivefs.model.DocumentFileAccess;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;

/**
 * Reads a document from the configured storage.
 */
public interface DocumentReader {

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
}