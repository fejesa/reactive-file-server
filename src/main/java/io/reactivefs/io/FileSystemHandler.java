package io.reactivefs.io;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

/**
 * Reads and writes documents to the storage.
 */
@ApplicationScoped
class FileSystemHandler {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FileSystem fileSystem = Vertx.vertx().fileSystem();

    /**
     * Reads the files from the given folder.
     * @param path The folder that content should be read.
     * @see FileSystem#readDir
     */
    public Uni<List<String>> getFiles(Path path) {
        return Uni.createFrom().item(path)
                .onItem()
                .transformToUni(p -> {
                    logger.info("Folder read request: {}", path);
                    return fileSystem.readDir(path.toString());
        });
    }

    /**
     * Create the directory represented by the path and any non-existent parents, asynchronously.
     *
     * @param path The absolute path of the folder that should be created.
     * @see FileSystem#mkdirs
     */
    public Uni<Void> createDirectories(Path path) {
        return fileSystem.mkdirs(path.toString());
    }

    /**
     * Deletes the given file asynchronously. If there is an error it is discarded.
     *
     * @param path The file path that should be deleted.
     */
    public Uni<Void> delete(Path path) {
        return Uni.createFrom().item(path).call(() -> {
            logger.info("File removal request: {}", path);
            return fileSystem.delete(path.toString());
        }).replaceWithVoid();
    }

    /**
     * This function creates the given file at the specified path and writes the provided content to it.
     * If the file already exists at that path, it will be replaced with the new content.
     * The content should be in Base64 format and will be decoded and written to the file.
     *
     * @param fileContent Contains the file path and its content that should be written out to the storage.
     * @return The asynchronous result of the operation when completed, or a failure if the operation failed.
     * @throws IllegalArgumentException if the message is not Base64 format
     * @see FileSystem#writeFile
     */
    public Uni<Void> writeFile(FileContent fileContent) {
        return Uni.createFrom().item(fileContent).call(() -> {
            logger.info("File write request to path: {}", fileContent.path());
            var buffer = Buffer.buffer(Base64.getDecoder().decode(fileContent.content()));
            return fileSystem.writeFile(fileContent.path().toString(), buffer);
        }).replaceWithVoid();
    }

    public Function<Path, Buffer> readFile() {
        return path -> {
            logger.info("File read request: {}", path);
            return fileSystem.readFileBlocking(path.toString());
        };
    }
}