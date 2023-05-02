package io.reactivefs;

public class RFSConfig {

    public static final String RETRY_INITIAL_BACKOFF_MS = "fs.retry.initial.backoff.ms";

    public static final String RETRY_EXPIRATION_MS = "fs.retry.expiration.ms";

    public static final String ORGANIZATION_DOCUMENT_ROOT_DIRECTORY = "fs.organization.document.dir";

    public static final String USER_DOCUMENT_ROOT_DIRECTORY = "fs.user.document.dir";

    private RFSConfig() {
    }
}