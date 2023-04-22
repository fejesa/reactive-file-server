package io.reactivefs.io;

import io.quarkus.test.junit.QuarkusTest;
import io.reactivefs.RFSConfig;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.file.FileSystemException;
import io.vertx.mutiny.core.buffer.Buffer;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class FileSystemHandlerTest {

    @Inject
    FileSystemHandler fileSystemHandler;

    @ConfigProperty(name = RFSConfig.ROOT_DIRECTORY)
    String rootDirectory;

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
        var organizationId = "orgCode";
        var tempFile = createTempFile(organizationId, "sample.tmp");
        try {
            Files.write(tempFile, "fake".getBytes());
            fileSystemHandler.getFiles(createOrganizationFolder(organizationId))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertItem(List.of(tempFile.toString()));
        } finally {
            Files.delete(tempFile);
        }
    }

    @Test
    void createOrganizationAndUserDirectory() {
        var folder = Paths.get(rootDirectory, "orgdir", "userdir");
        fileSystemHandler.createDirectories(folder)
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();
    }

    @Test
    void whenDirectoryExistsNotToThrowException() throws IOException {
        var path = createUserFolder("createOrgFolderBeforeTryAgain", "userId");
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
        var tempFile = createTempFile("orgCode", "sample.tmp");
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
    void invalidContentIsNotWritten() throws IOException {
        var path = createUserFolder("orgId", "userId");
        fileSystemHandler.writeFile(new FileContent(path, "cHJvc3RkZXY_YmxvZw=="))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitFailure(Duration.ofMillis(500))
            .assertFailedWith(IllegalArgumentException.class);
    }

    @Test
    void writeValidContent() throws IOException {
        var userFolder = createUserFolder("orgId", "userId");
        var filePath = userFolder.resolve("validFile.tmp");
        try {
            var content = Base64.getEncoder().encodeToString("payload".getBytes());
            fileSystemHandler.writeFile(new FileContent(filePath, content))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertCompleted();
            assertTrue(Files.exists(filePath));
            assertEquals("payload", Files.readString(filePath));
        } finally {
            removeFile(filePath);
        }
    }

    @Test
    void readExistFile() throws IOException {
        var userId = "userId";
        var organizationId = "organizationId";
        var fileName = "userFile.tmp";
        var userFolder = Paths.get(rootDirectory, organizationId, userId);
        fileSystemHandler.createDirectories(userFolder)
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem(Duration.ofMillis(500))
            .assertCompleted();

        var filePath = userFolder.resolve(fileName);
        try {
            var content = Base64.getEncoder().encodeToString("content".getBytes());
            fileSystemHandler.writeFile(new FileContent(filePath, content))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertCompleted();
            assertEquals(Buffer.buffer("content".getBytes()), fileSystemHandler.readFile().apply(filePath));
        } finally {
            Files.delete(filePath);
        }
    }

    private Path createTempFile(String organizationId, String fileName) throws IOException {
        return Files.createFile(createOrganizationFolder(organizationId).resolve(fileName));
    }

    private Path createOrganizationFolder(String organizationId) throws IOException {
        return Files.createDirectories(Paths.get(rootDirectory, organizationId.toLowerCase()));
    }

    private Path createUserFolder(String organizationId, String userId) throws IOException {
        return Files.createDirectories(Paths.get(rootDirectory, organizationId.toLowerCase(), userId));
    }

    private void removeFile(Path path) {
        FileUtils.deleteQuietly(path.toFile());
    }
}