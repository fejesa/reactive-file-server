package org.rfs.io;

import org.rfs.FSConfig;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class FileHandlerTest {

    @Inject
    FileHandler fileHandler;

    @ConfigProperty(name = FSConfig.ROOT_DIRECTORY)
    String rootDirectory;

    @Test
    void getFileListFromOrganizationDirectory() throws IOException {
        var organizationId = "orgCode";
        var fileName = "sample.pdf";
        var path = Files.createDirectories(Paths.get(rootDirectory, organizationId.toLowerCase()));
        var tempFile = Files.createFile(path.resolve(fileName));
        try {
            Files.write(tempFile, "fake".getBytes());

            assertEquals(List.of(tempFile.toString()), fileHandler.getFiles(path));
        } finally {
            Files.delete(tempFile);
        }
    }


    @Test
    void deleteFileDoesNotExist() {
        assertDoesNotThrow(() -> fileHandler.delete(Paths.get("fileDoesNotExists")));
    }

    @Test
    void deleteFile() throws IOException {
        var organizationId = "orgCode";
        var fileName = "sample.pdf";
        var path = Files.createDirectories(Paths.get(rootDirectory, organizationId.toLowerCase()));
        var tempFile = Files.createFile(path.resolve(fileName));
        Files.write(tempFile, "fake".getBytes());
        assertTrue(Files.exists(tempFile));
        fileHandler.delete(tempFile);
        await().atMost(Duration.ofMillis(500)).untilAsserted(() -> assertFalse(Files.exists(tempFile)));
    }
}