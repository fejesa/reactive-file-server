package io.reactivefs.service;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivefs.RFSConfig;
import io.reactivefs.model.DocumentCreateRequest;
import io.reactivefs.model.DocumentFileAccess;
import io.reactivefs.model.DocumentRemoveRequest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.file.FileSystemException;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;

@QuarkusTest
public class UserDocumentStoreTest {

    @Inject
    @UserDocument
    UserDocumentStore documentStore;

    @ConfigProperty(name = RFSConfig.USER_DOCUMENT_ROOT_DIRECTORY)
    String userDocumentDirectory;

    @Test
    void whenInvalidSingleFileRemovalRequestShouldFail() {
        var subscriber = documentStore.remove(new DocumentRemoveRequest("", "", "fake.pdf"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void whenDocumentShouldBeRemoved() {
        var organizationId = "orgId";
        var userId = "9999999";
        var fileName = "fakeForRemoval.pdf";
        var content = "payload";
        var payload = Base64.getEncoder().encodeToString(content.getBytes());

        documentStore.write(new DocumentCreateRequest(organizationId, userId, fileName, payload))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem().assertCompleted();
        documentStore.read(new DocumentFileAccess(organizationId, userId, fileName)).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem().assertItem(Buffer.buffer(content.getBytes()));

        documentStore.remove(new DocumentRemoveRequest(organizationId, userId, fileName))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();

        await().atMost(Duration.ofSeconds(5)).until(() -> {
            documentStore.read(new DocumentFileAccess(userId, organizationId, fileName)).subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure().assertFailedWith(FileSystemException.class);
            return Boolean.TRUE;
        });
    }

    @Test
    void whenNotEncodedThenUserDocumentShouldNotBeWritten() {
        var subscriber = documentStore.write(new DocumentCreateRequest("", "", "fake.pdf", "payload"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void whenInvalidUserIdThenUserDocumentShouldNotBeWritten() {
        var subscriber = documentStore.write(new DocumentCreateRequest("orgId", "1234", "fake.pdf", "payload"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void whenInvalidEncodedContentShouldNotBeWritten() {
        var subscriber = documentStore.write(new DocumentCreateRequest("orgId", "123456", "fake.pdf", "cHJvc3RkZXY_YmxvZw=="))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void whenSameDocumentShouldOverridePrevious() {
        var payload = Base64.getEncoder().encodeToString("payload".getBytes());
        var createRequest = new DocumentCreateRequest("orgId", "1234567", "fake.pdf", payload);
        IntStream.range(0, 2).forEach(__ -> {
            documentStore.write(createRequest)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();
        });
    }

    @Test
    void whenUserDocumentShouldBeWritten() {
        var payload = Base64.getEncoder().encodeToString("payload".getBytes());
        var subscriber = documentStore.write(new DocumentCreateRequest("orgId", "1234567", "fake.pdf", payload))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem().assertCompleted();
    }

    @Test
    void whenUserHasNoAccessToDocumentErrorShouldReturn() {
        var subscriber = documentStore.read(new DocumentFileAccess("", "", ""))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void whenUserDocumentFileDoesNotExistErrorShouldReturn() {
        var subscriber = documentStore.read(new DocumentFileAccess("orgId", "userId", "doesNotExist"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(FileSystemException.class);
    }

    @Test
    void readExistUserDocument() throws IOException {
        var userId = "1267890";
        var userDir = userId.substring(5);
        var organizationId = "orgId";
        var fileName = "message.pdf";
        var path = Files.createDirectories(Paths.get(userDocumentDirectory, organizationId.toLowerCase(), userDir));
        var tempFile = Files.createFile(path.resolve(fileName));
        try {
            Files.write(tempFile, "fake".getBytes());
            var subscriber = documentStore.read(new DocumentFileAccess(organizationId, userId, fileName))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());
            subscriber.awaitItem().assertItem(Buffer.buffer("fake".getBytes()));
        } finally {
            Files.delete(tempFile);
        }
    }
}