package io.reactivefs.io;

import io.reactivefs.model.DocumentRemoveMessage;
import io.smallrye.mutiny.Uni;

/**
 * Deletes the requested document from the storage.
 */
public interface DocumentRemoval {

    /**
     * Clean up the requested document from the file storage.
     * If a document or subfolder does not exist it is skipped by the process.
     *
     * @param removeMessage contains the information about the file that should be deleted;
     *                      for example organizationId and user id can be used for calculation of the subfolder where
     *                      the given file stored
     * @return The lazy asynchronous action that the caller can subscribe to.
     */
    Uni<Void> remove(DocumentRemoveMessage removeMessage);
}