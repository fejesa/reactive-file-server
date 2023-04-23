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
import java.util.List;

/**
 * Reads and writes documents to the storage.
 */
@ApplicationScoped
public class FileSystemHandler {

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
                    logger.info("Folder read request: {}", p);
                    return fileSystem.readDir(p.toString());
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
    public Uni<Void> deleteFile(Path path) {
        return Uni.createFrom().item(path)
                .onItem()
                .transformToUni(p -> {
                    logger.info("File removal request: {}", p);
                    return fileSystem.delete(p.toString());
                });
    }

    /**
     * This function creates the given file at the specified path and writes the provided content to it.
     * If the file already exists at that path, it will be replaced with the new content.
     *
     * @param fileContent Contains the file path and its content that should be written out to the storage.
     * @return The asynchronous result of the operation when completed, or a failure if the operation failed.
     * @see FileSystem#writeFile
     */
    public Uni<Void> writeFile(FileContent fileContent) {
        return Uni.createFrom().item(fileContent)
                .onItem()
                .transformToUni(fc -> {
                    logger.info("File write request to path: {}", fc.path());
                    return fileSystem.writeFile(fc.path().toString(), Buffer.buffer(fc.content()));
                });
    }

    public Uni<Buffer> readFile(Path path) {
        return Uni.createFrom().item(path)
                .onItem()
                .transformToUni(p -> {
                    logger.info("File read request: {}", p);
                    return fileSystem.readFile(p.toString());
                });
    }
}