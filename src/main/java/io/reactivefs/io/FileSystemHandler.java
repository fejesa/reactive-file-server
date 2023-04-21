package io.reactivefs.io;

import io.smallrye.mutiny.Uni;
import io.vertx.core.file.FileSystemException;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

/**
 * Reads and writes documents to the storage. All actions are blocking execution except the deletion.
 */
@ApplicationScoped
class FileSystemHandler {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FileSystem fileSystem = Vertx.vertx().fileSystem();

    public Uni<List<String>> getFiles(Path path) {
        logger.info("Folder read request: {}", path);
        return fileSystem.readDir(path.toString());
    }

    public void createDirectories(FileMessage fileMessage) {
        try {
            fileSystem.mkdirsBlocking(fileMessage.path().getParent().toString());
        } catch (FileSystemException fe) {
            if (fe.getCause() != null && !(fe.getCause() instanceof FileAlreadyExistsException)) {
                logger.error("Cannot create folder: {}", fileMessage.path().getParent(), fe);
            }
        }
    }

    /**
     * Deletes the given file asynchronously. If there is an error it is discarded.
     *
     * @param path The file path that should be deleted.
     */
    public void delete(Path path) {
        logger.info("File removal request: {}", path);
        fileSystem.deleteAndForget(path.toString());
    }

    /**
     * Writes a file to the given path and content. If the file already exist it overrides the older one with the new content.
     * The message must be Base64 format, and before the content is written to the disk it is decoded.
     *
     * @param fileMessage Contains the file path and its content that should be written out to the storage.
     * @throws IllegalArgumentException if the message is not Base64 format
     */
    public void writeFile(FileMessage fileMessage) {
        logger.info("File write request: {}", fileMessage.path());
        var buffer = Buffer.buffer(Base64.getDecoder().decode(fileMessage.message()));
        fileSystem.writeFileBlocking(fileMessage.path().toString(), buffer);
        logger.info("Message file is written: {}", fileMessage.path());
    }

    public Function<Path, Buffer> readFile() {
        return path -> {
            logger.info("File read request: {}", path);
            return fileSystem.readFileBlocking(path.toString());
        };
    }
}