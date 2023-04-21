package io.reactivefs.io;

import io.reactivefs.model.DocumentRemoveMessage;
import io.smallrye.mutiny.Uni;

/**
 * Deletes the requested document from the storage.
 */
public interface DocumentRemoval {

    /**
     * Removes the requested document from the file storage.<p>
     * If a document or subfolder does not exist then the process is skipped.
     *
     * @param message contains the information about the file that should be deleted
     * @return The lazy asynchronous action that the caller can subscribe to.
     */
    Uni<Void> remove(DocumentRemoveMessage message);
}