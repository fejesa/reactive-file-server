package org.rfs.io;

import org.rfs.FSConfig;
import org.rfs.model.DocumentFileAccess;
import org.rfs.model.DocumentCreateMessage;
import org.rfs.model.DocumentRemoveMessage;
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
class PdfDocumentHandlershould {

    @Pdf
    @Inject
    PdfDocumentHandler documentHandler;

    @ConfigProperty(name = FSConfig.ROOT_DIRECTORY)
    String rootDirectory;

    @Test
    void invalidSingleFileRemovalMessageError() {
        var subscriber = documentHandler.remove(new DocumentRemoveMessage("", "", "fake.pdf"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void messageBeRemoved() {
        var organizationId = "orgCode";
        var userId = "9999999";
        var fileName = "fakeForRemoval.pdf";
        var content = "payload";
        var payload = Base64.getEncoder().encodeToString(content.getBytes());

        documentHandler.write(new DocumentCreateMessage(organizationId, userId, fileName, payload))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem().assertCompleted();
        documentHandler.read(new DocumentFileAccess(userId, organizationId, fileName)).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem().assertItem(Buffer.buffer(content.getBytes()));

        documentHandler.remove(new DocumentRemoveMessage(organizationId, userId, fileName))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted();

        await().atMost(Duration.ofSeconds(5)).until(() -> {
            documentHandler.read(new DocumentFileAccess(userId, organizationId, fileName)).subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure().assertFailedWith(FileSystemException.class);
            return Boolean.TRUE;
        });
    }

    @Test
    void invalidFileNotToBeWritten() {
        var subscriber = documentHandler.write(new DocumentCreateMessage("", "", "fake.pdf", "payload"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void invalidUserIdNotToBeWritten() {
        var subscriber = documentHandler.write(new DocumentCreateMessage("orgCode", "1234", "fake.pdf", "payload"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(StringIndexOutOfBoundsException.class);
    }

    @Test
    void invalidPayloadNotToBeWritten() {
        var subscriber = documentHandler.write(new DocumentCreateMessage("orgCode", "123456", "fake.pdf", "cHJvc3RkZXY_YmxvZw=="))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void sameDocumentOverridePrevious() {
        var payload = Base64.getEncoder().encodeToString("payload".getBytes());
        var createMessage = new DocumentCreateMessage("orgCode", "1234567", "fake.pdf", payload);
        IntStream.range(0, 2).forEach(__ -> {
            documentHandler.write(createMessage)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();
        });
    }

    @Test
    void writeDocument() {
        var payload = Base64.getEncoder().encodeToString("payload".getBytes());
        var subscriber = documentHandler.write(new DocumentCreateMessage("orgCode", "1234567", "fake.pdf", payload))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem().assertCompleted();
    }

    @Test
    void hasNoAccessToDocument() {
        var subscriber = documentHandler.read(new DocumentFileAccess("", "", ""))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void documentFileDoesNotExist() {
        var subscriber = documentHandler.read(new DocumentFileAccess("userId", "orgCode", "doesNotExist"))
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
            var subscriber = documentHandler.read(new DocumentFileAccess(userId, organizationId, fileName))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());
            subscriber.awaitItem().assertItem(Buffer.buffer("fake".getBytes()));
        } finally {
            Files.delete(tempFile);
        }
    }
}