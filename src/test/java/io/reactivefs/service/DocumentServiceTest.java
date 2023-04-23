package io.reactivefs.service;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivefs.io.UserDocument;
import io.reactivefs.model.DocumentCreateRequest;
import io.reactivefs.model.DocumentFileAccess;
import io.reactivefs.model.DocumentRemoveRequest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.file.FileSystemException;
import io.vertx.mutiny.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Base64;
import java.util.stream.IntStream;

@QuarkusTest
class DocumentServiceTest {

    @UserDocument
    @Inject
    DocumentService documentService;

    private final String organizationId = "orgCodeDocumentServiceTest";

    @Test
    void invalidSingleFileRemovalError() {
        var subscriber = documentService.remove(new DocumentRemoveRequest("", "", "fake.tmp"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void documentBeRemoved() {
        var userId = "9999999";
        var fileName = "fakeForRemoval.tmp";
        var content = "payload";
        var payload = Base64.getEncoder().encodeToString(content.getBytes());

        // Given a document
        documentService.write(new DocumentCreateRequest(organizationId, userId, fileName, payload))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();
        documentService.read(new DocumentFileAccess(organizationId, userId, fileName)).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
            .assertItem(Buffer.buffer(content.getBytes()));

        // When the document is removed
        documentService.remove(new DocumentRemoveRequest(organizationId, userId, fileName))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();

        // Then it should not be accessed
        documentService.read(new DocumentFileAccess(organizationId, userId, fileName)).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitFailure(Duration.ofMillis(500))
            .assertFailedWith(FileSystemException.class);
    }

    @Test
    void invalidDocumentNotToBeWritten() {
        var subscriber = documentService.write(new DocumentCreateRequest("", "", "fake.pdf", "payload"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void invalidUserIdNotToBeWritten() {
        var subscriber = documentService.write(new DocumentCreateRequest(organizationId, "1234", "fake.pdf", "payload"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void invalidPayloadNotToBeWritten() {
        var subscriber = documentService.write(new DocumentCreateRequest(organizationId, "123456", "fake.pdf", "cHJvc3RkZXY_YmxvZw=="))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void sameDocumentOverrideThePreviousOne() {
        IntStream.range(0, 2).forEach(i -> {
            var content = "payload" + i;
            var payload = Base64.getEncoder().encodeToString(content.getBytes());
            var userId = "1234567";
            var fileName = "fake.tmp";
            var createMessage = new DocumentCreateRequest(organizationId, userId, fileName, payload);
            documentService.write(createMessage)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertCompleted();
            documentService.read(new DocumentFileAccess(organizationId, userId, fileName)).subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertItem(Buffer.buffer(content.getBytes()));
        });
    }

    @Test
    void hasNoAccessToDocument() {
        var subscriber = documentService.read(new DocumentFileAccess("", "", ""))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void documentFileDoesNotExist() {
        var subscriber = documentService.read(new DocumentFileAccess(organizationId, "userId", "doesNotExist"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(FileSystemException.class);
    }
}