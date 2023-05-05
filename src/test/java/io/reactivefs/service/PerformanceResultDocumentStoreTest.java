package io.reactivefs.service;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivefs.RFSConfig;
import io.reactivefs.model.DocumentFileAccess;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.file.FileSystemException;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@QuarkusTest
public class PerformanceResultDocumentStoreTest {

    @PerformanceResult
    @Inject
    DocumentStore documentStore;

    @ConfigProperty(name = RFSConfig.PERFORMANCE_DOCUMENT_ROOT_DIRECTORY)
    String performanceDocumentRootDirectory;

    @Test
    void whenInvalidOrganizationThenUserHasNoAccessToPerformanceResult() {
        var subscriber = documentStore.read(new DocumentFileAccess("", "", ""))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void whenPerformanceResultFileDoesNotExistErrorShouldReturn() {
        var subscriber = documentStore.read(new DocumentFileAccess("orgId", "userId", ""))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(FileSystemException.class);
    }

    @Test
    void readExistingPerformanceResultFile() throws IOException {
        var userId = "1267890";
        var organizationId = "organizationId";
        var path = Files.createDirectories(Paths.get(performanceDocumentRootDirectory, organizationId.toLowerCase()));
        var tempFile = Files.createFile(path.resolve(userId));
        try {
            Files.write(tempFile, "fake".getBytes());
            var subscriber = documentStore.read(new DocumentFileAccess(organizationId, userId,""))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());
            subscriber.awaitItem().assertItem(Buffer.buffer("fake".getBytes()));
        } finally {
            Files.delete(tempFile);
        }
    }
}
