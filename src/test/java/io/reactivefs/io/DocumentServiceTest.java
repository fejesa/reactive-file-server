package io.reactivefs.io;

import io.reactivefs.RFSConfig;
import io.reactivefs.model.DocumentFileAccess;
import io.reactivefs.model.DocumentCreateRequest;
import io.reactivefs.model.DocumentRemoveRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.file.FileSystemException;
import io.vertx.mutiny.core.buffer.Buffer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;

@QuarkusTest
class DocumentServiceTest {

    @Inject
    DocumentService documentService;

    @ConfigProperty(name = RFSConfig.ROOT_DIRECTORY)
    String rootDirectory;

    @Test
    void invalidSingleFileRemovalError() {
        var subscriber = documentService.remove(new DocumentRemoveRequest("", "", "fake.pdf"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void documentBeRemoved() {
        var organizationId = "orgCode";
        var userId = "9999999";
        var fileName = "fakeForRemoval.pdf";
        var content = "payload";
        var payload = Base64.getEncoder().encodeToString(content.getBytes());

        documentService.write(new DocumentCreateRequest(organizationId, userId, fileName, payload))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem().assertCompleted();
        documentService.read(new DocumentFileAccess(organizationId, userId, fileName)).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem().assertItem(Buffer.buffer(content.getBytes()));

        documentService.remove(new DocumentRemoveRequest(organizationId, userId, fileName))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted();

        await().atMost(Duration.ofSeconds(5)).until(() -> {
            documentService.read(new DocumentFileAccess(organizationId, userId, fileName)).subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure().assertFailedWith(FileSystemException.class);
            return Boolean.TRUE;
        });
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
        var subscriber = documentService.write(new DocumentCreateRequest("orgCode", "1234", "fake.pdf", "payload"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void invalidPayloadNotToBeWritten() {
        var subscriber = documentService.write(new DocumentCreateRequest("orgCode", "123456", "fake.pdf", "cHJvc3RkZXY_YmxvZw=="))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void sameDocumentOverridePrevious() {
        var payload = Base64.getEncoder().encodeToString("payload".getBytes());
        var createMessage = new DocumentCreateRequest("orgCode", "1234567", "fake.pdf", payload);
        IntStream.range(0, 2).forEach(__ -> {
            documentService.write(createMessage)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();
        });
    }

    @Test
    void writeDocument() {
        var payload = Base64.getEncoder().encodeToString("payload".getBytes());
        var subscriber = documentService.write(new DocumentCreateRequest("orgCode", "1234567", "fake.pdf", payload))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem().assertCompleted();
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
        var subscriber = documentService.read(new DocumentFileAccess("orgCode", "userId", "doesNotExist"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(FileSystemException.class);
    }

    @Test
    void readExistDocument() throws IOException {
        var userId = "1267890";
        var userDirectory = userId.substring(5);
        var organizationId = "uniCode";
        var fileName = "message.pdf";
        var path = Files.createDirectories(Paths.get(rootDirectory, organizationId.toLowerCase(), userDirectory));
        var tempFile = Files.createFile(path.resolve(fileName));
        try {
            Files.write(tempFile, "fake".getBytes());
            var subscriber = documentService.read(new DocumentFileAccess(organizationId, userId, fileName))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());
            subscriber.awaitItem().assertItem(Buffer.buffer("fake".getBytes()));
        } finally {
            Files.delete(tempFile);
        }
    }
}