package io.reactivefs.service;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivefs.RFSConfig;
import io.reactivefs.model.DocumentCreateRequest;
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
import java.util.Base64;
import java.util.stream.IntStream;

@QuarkusTest
public class AttachmentDocumentStoreTest {

    @Attachment
    @Inject
    DocumentStore documentStore;

    @ConfigProperty(name = RFSConfig.ATTACHMENT_DOCUMENT_ROOT_DIRECTORY)
    String attachmentDocumentDirectory;

    @Test
    void whenInvalidOrganizationThenAttachmentWriteShouldFail() {
        var subscriber = documentStore.write(new DocumentCreateRequest("", "", "fake.tmp", "payload"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void whenInvalidAttachmentContentShouldNotBeWritten() {
        var subscriber = documentStore.write(new DocumentCreateRequest("orgId", "", "fake.tmp", "cHJvc3RkZXY_YmxvZw=="))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void whenSameAttachmentShouldOverrideThePrevious() {
        var payload = Base64.getEncoder().encodeToString("payload".getBytes());
        var createRequest = new DocumentCreateRequest("orgId", "", "fake.tmp", payload);
        IntStream.range(0, 2).forEach(__ -> {
            documentStore.write(createRequest)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertCompleted();
        });
    }

    @Test
    void writeAttachment() {
        var payload = Base64.getEncoder().encodeToString("payload".getBytes());
        var subscriber = documentStore.write(new DocumentCreateRequest("orgId", "", "fake.tmp", payload))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem().assertCompleted();
    }

    @Test
    void whenUserHasNoAccessToAttachmentErrorShouldReturn() {
        var subscriber = documentStore.read(new DocumentFileAccess("", "invalid", ""))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void whenAttachmentFileDoesNotExistErrorShouldReturn() {
        var subscriber = documentStore.read(new DocumentFileAccess("orgId", "", "doesNotExist"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(FileSystemException.class);
    }

    @Test
    void readExistAttachment() throws IOException {
        var organizationId = "FAKE";
        var fileName = "attachment.tmp";
        var path = Files.createDirectories(Paths.get(attachmentDocumentDirectory, organizationId.toLowerCase()));
        var tempFile = Files.createFile(path.resolve(fileName));
        try {
            Files.write(tempFile, "fake".getBytes());
            var subscriber = documentStore.read(new DocumentFileAccess(organizationId, "", fileName))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());
            subscriber.awaitItem().assertItem(Buffer.buffer("fake".getBytes()));
        } finally {
            Files.delete(tempFile);
        }
    }
}
