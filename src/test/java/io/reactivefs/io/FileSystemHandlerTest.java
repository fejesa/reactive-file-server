package io.reactivefs.io;

import io.reactivefs.RFSConfig;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.file.FileSystemException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
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
        var fileName = "sample.pdf";
        var tempFile = createTempFile(organizationId, fileName);
        try {
            Files.write(tempFile, "fake".getBytes());
            fileSystemHandler.getFiles(getOrganizationFolder(organizationId))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofMillis(500))
                .assertItem(List.of(tempFile.toString()));
        } finally {
            Files.delete(tempFile);
        }
    }

    @Test
    void deleteFileDoesNotExist() {
        assertDoesNotThrow(() -> fileSystemHandler.delete(Paths.get("fileDoesNotExists")));
    }

    @Test
    void deleteExistingFile() throws IOException {
        var organizationId = "orgCode";
        var fileName = "sample.pdf";
        var tempFile = createTempFile(organizationId, fileName);
        try {
            assertTrue(Files.exists(tempFile));
            fileSystemHandler.delete(tempFile);
            await()
                .atMost(Duration.ofMillis(500))
                .untilAsserted(() -> assertFalse(Files.exists(tempFile)));
        } finally {
            try {
                Files.delete(tempFile);
            } catch (IOException e) {
                // NOP
            }
        }
    }

    private Path createTempFile(String organizationId, String fileName) throws IOException {
        return Files.createFile(getOrganizationFolder(organizationId).resolve(fileName));
    }

    private Path getOrganizationFolder(String organizationId) throws IOException {
        return Files.createDirectories(Paths.get(rootDirectory, organizationId.toLowerCase()));
    }
}