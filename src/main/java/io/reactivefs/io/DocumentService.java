package io.reactivefs.io;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.reactivefs.RFSConfig;
import io.reactivefs.model.DocumentCreateMessage;
import io.reactivefs.model.DocumentFileAccess;
import io.reactivefs.model.DocumentRemoveMessage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.smallrye.mutiny.unchecked.Unchecked.function;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;

@ApplicationScoped
public class DocumentService implements DocumentReader, DocumentWriter, DocumentRemoval {

    @ConfigProperty(name = RFSConfig.ROOT_DIRECTORY)
    String rootDirectory;

    @Inject
    FileSystemHandler fileSystemHandler;

    @Override
    public Uni<Void> remove(DocumentRemoveMessage message) {
        return Uni.createFrom().item(message)
            .map(function(this::toMessagePath))
            .invoke(fileSystemHandler::delete)
            .replaceWithVoid();
    }

    @Override
    public Uni<Buffer> read(DocumentFileAccess fileAccess) {
        return Uni.createFrom().item(fileAccess)
            .map(function(this::toMessagePath))
            .map(fileSystemHandler.readFile());
    }

    @Override
    public Uni<Void> write(DocumentCreateMessage createMessage) {
        return Uni.createFrom().item(createMessage)
            .map(function(this::toFileMessage))
            .invoke(fileSystemHandler::createDirectories)
            .invoke(fileSystemHandler::writeFile)
            .replaceWithVoid();
    }

    private Path toMessagePath(DocumentFileAccess fileAccess) {
        if (isAnyBlank(fileAccess.organizationId(), fileAccess.userId(), fileAccess.fileName())) {
            throw new IllegalArgumentException("Pdf message file cannot be identified");
        }
        return toMessagePath(fileAccess.organizationId(), fileAccess.userId(), fileAccess.fileName());
    }

    private Path toMessagePath(DocumentRemoveMessage removeMessage) {
        return toMessagePath(removeMessage.organizationId(), removeMessage.userId(), removeMessage.fileName());
    }

    private FileMessage toFileMessage(DocumentCreateMessage fm) {
        if (isAnyBlank(fm.organizationId(), fm.userId(), fm.fileName(), fm.payload())) {
            throw new IllegalArgumentException("Pdf message file cannot be written");
        }
        return new FileMessage(toMessagePath(fm.organizationId(), fm.userId(), fm.fileName()), fm.payload());
    }

    private Path toMessagePath(String organizationId, String userId, String fileName) {
        return Paths.get(rootDirectory, organizationId.toLowerCase(), userId.substring(5), fileName);
    }
}