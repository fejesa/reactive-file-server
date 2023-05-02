package io.reactivefs.service;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivefs.model.DocumentCreateRequest;
import io.reactivefs.model.DocumentFileAccess;
import io.reactivefs.model.DocumentRemoveRequest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.file.FileSystemException;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;
import java.util.stream.IntStream;

@QuarkusTest
class UserDocumentStoreTest {

    @UserDocument
    @Inject
    UserDocumentStore userDocumentStore;

    private final String organizationId = "orgCodeDocumentServiceTest";

    @Test
    void invalidSingleFileRemovalError() {
        var subscriber = userDocumentStore.remove(new DocumentRemoveRequest("", "", "fake.tmp"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void documentBeRemoved() {
        var userId = "9999999";
        var fileName = "fakeForRemoval.tmp";
        var content = "content";
        var payload = Base64.getEncoder().encodeToString(content.getBytes());

        // Given a document
        userDocumentStore.write(new DocumentCreateRequest(organizationId, userId, fileName, payload))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();
        userDocumentStore.read(new DocumentFileAccess(organizationId, userId, fileName)).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
            .assertItem(Buffer.buffer(content.getBytes()));

        // When the document is removed
        userDocumentStore.remove(new DocumentRemoveRequest(organizationId, userId, fileName))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();

        // Then it should not be accessed
        userDocumentStore.read(new DocumentFileAccess(organizationId, userId, fileName)).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitFailure(Duration.ofMillis(500))
            .assertFailedWith(FileSystemException.class);
    }

    @Test
    void invalidDocumentNotToBeWritten() {
        var subscriber = userDocumentStore.write(new DocumentCreateRequest("", "", "fake.pdf", "content"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void invalidUserIdNotToBeWritten() {
        var subscriber = userDocumentStore.write(new DocumentCreateRequest(organizationId, "1234", "fake.pdf", "content"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void invalidPayloadNotToBeWritten() {
        var subscriber = userDocumentStore.write(new DocumentCreateRequest(organizationId, "123456", "fake.pdf", "cHJvc3RkZXY_YmxvZw=="))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void sameDocumentOverrideThePreviousOne() {
        IntStream.range(0, 2).forEach(i -> {
            var content = "content" + i;
            var payload = Base64.getEncoder().encodeToString(content.getBytes());
            var userId = "1234567";
            var fileName = "fake.tmp";
            var documentCreateRequest = new DocumentCreateRequest(organizationId, userId, fileName, payload);
            userDocumentStore.write(documentCreateRequest)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertCompleted();
            userDocumentStore.read(new DocumentFileAccess(organizationId, userId, fileName)).subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertItem(Buffer.buffer(content.getBytes()));
        });
    }

    @Test
    void hasNoAccessToDocument() {
        var subscriber = userDocumentStore.read(new DocumentFileAccess("", "", ""))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void documentFileDoesNotExist() {
        var subscriber = userDocumentStore.read(new DocumentFileAccess(organizationId, "userId", "doesNotExist"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(FileSystemException.class);
    }
}