package io.reactivefs.io;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivefs.RFSConfig;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.file.FileSystemException;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class FileSystemHandlerTest {

    @Inject
    FileSystemHandler fileSystemHandler;

    @ConfigProperty(name = RFSConfig.USER_DOCUMENT_ROOT_DIRECTORY)
    String userDirectory;

    @ConfigProperty(name = RFSConfig.ATTACHMENT_DOCUMENT_ROOT_DIRECTORY)
    String attachmentDirectory;

    private final String organizationId = "orgCodeFSTest";

    @Test
    void getFileListWhenFolderDoesNotExist() {
        fileSystemHandler.getFiles(Paths.get("invalid"))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitFailure(Duration.ofMillis(500))
            .assertFailedWith(FileSystemException.class, "Cannot read directory invalid. Does not exist");
    }

    @Test
    void getFileListFromOrganizationDirectory() throws IOException {
        var tempFile = createOrgTempFile(organizationId, "sample.tmp");
        try {
            Files.write(tempFile, "fake".getBytes());
            fileSystemHandler.getFiles(createOrgFolder(organizationId))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertItem(List.of(tempFile.toString()));
        } finally {
            Files.delete(tempFile);
        }
    }

    @Test
    void createUserDirectory() {
        var folder = Paths.get(userDirectory, organizationId, "userdir");
        fileSystemHandler.createDirectories(folder)
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();
    }

    @Test
    void whenDirectoryExistsNotToThrowException() throws IOException {
        var path = createUserFolder("createUserOrgFolderBeforeTryAgain", "userId");
        fileSystemHandler.createDirectories(path)
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();
    }

    @Test
    void deleteFileDoesNotExist() {
        assertDoesNotThrow(() -> fileSystemHandler.deleteFile(Paths.get("fileDoesNotExists")));
    }

    @Test
    void deleteExistingFile() throws IOException {
        var tempFile = createOrgTempFile(organizationId, "sample.tmp");
        try {
            assertTrue(Files.exists(tempFile));
            fileSystemHandler.deleteFile(tempFile)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertCompleted();
            assertFalse(Files.exists(tempFile));
        } finally {
            removeFile(tempFile);
        }
    }

    @Test
    void writeValidContent() throws IOException {
        var userFolder = createUserFolder(organizationId, "userId");
        var filePath = userFolder.resolve("validFile.tmp");
        try {
            fileSystemHandler.writeFile(new FileContent(filePath, "content".getBytes()))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertCompleted();
            assertTrue(Files.exists(filePath));
            assertEquals("content", Files.readString(filePath));
        } finally {
            removeFile(filePath);
        }
    }

    @Test
    void readExistFile() throws IOException {
        var userId = "userId";
        var fileName = "userFile.tmp";
        var userFolder = Paths.get(userDirectory, organizationId, userId);
        fileSystemHandler.createDirectories(userFolder)
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();

        var filePath = userFolder.resolve(fileName);
        try {
            fileSystemHandler.writeFile(new FileContent(filePath, "content".getBytes()))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertCompleted();
            fileSystemHandler.readFile(filePath)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertItem(Buffer.buffer("content".getBytes()));
        } finally {
            Files.delete(filePath);
        }
    }

    private Path createOrgTempFile(String organizationId, String fileName) throws IOException {
        return Files.createFile(createOrgFolder(organizationId).resolve(fileName));
    }

    private Path createOrgFolder(String organizationId) throws IOException {
        return Files.createDirectories(Paths.get(attachmentDirectory, organizationId.toLowerCase()));
    }

    private Path createUserFolder(String organizationId) throws IOException {
        return Files.createDirectories(Paths.get(userDirectory, organizationId.toLowerCase()));
    }

    private Path createUserFolder(String organizationId, String userId) throws IOException {
        return Files.createDirectories(Paths.get(userDirectory, organizationId.toLowerCase(), userId));
    }

    private void removeFile(Path path) {
        FileUtils.deleteQuietly(path.toFile());
    }
}