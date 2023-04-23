package io.reactivefs.service;

import io.reactivefs.io.*;
import io.reactivefs.model.DocumentCreateRequest;
import io.reactivefs.model.DocumentFileAccess;
import io.reactivefs.model.DocumentRemoveRequest;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

import static io.smallrye.mutiny.unchecked.Unchecked.function;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;

@UserDocument
@ApplicationScoped
public class DocumentService implements DocumentReader, DocumentWriter, DocumentRemoval {

    @Inject
    FileSystemHandler fileSystemHandler;

    @UserDocument
    @Inject
    DocumentPathResolver pathResolver;

    @Override
    public Uni<Void> remove(DocumentRemoveRequest message) {
        return Uni.createFrom().item(message)
            .map(function(this::documentPath))
            .onItem()
            .transformToUni(fileSystemHandler::deleteFile);
    }

    @Override
    public Uni<Buffer> read(DocumentFileAccess fileAccess) {
        return Uni.createFrom().item(fileAccess)
            .map(function(this::documentPath))
            .map(fileSystemHandler.readFile());
    }

    @Override
    public Uni<Void> write(DocumentCreateRequest createMessage) {
        return Uni.createFrom().item(createMessage)
            .map(function(this::toFileContent))
            .onItem()
            .call(fileContent -> fileSystemHandler.createDirectories(fileContent.path().getParent()))
            .onItem()
            .transformToUni(fileSystemHandler::writeFile);
    }

    private Path documentPath(DocumentFileAccess fileAccess) {
        if (isAnyBlank(fileAccess.organizationId(), fileAccess.userId(), fileAccess.fileName())) {
            throw new IllegalArgumentException("Document file cannot be identified");
        }
        return pathResolver.resolve(fileAccess.organizationId(), fileAccess.userId(), fileAccess.fileName());
    }

    private Path documentPath(DocumentRemoveRequest removeMessage) {
        return pathResolver.resolve(removeMessage.organizationId(), removeMessage.userId(), removeMessage.fileName());
    }

    /**
     * Converts the document creation request to the final path and decodes the contents.
     *
     * @param createRequest This specifies the request for creating a file, which includes the payload as well as the owners of the document.
     * @return The file path and content that should be written.
     * @throws IllegalArgumentException if the message is not Base64 format or the file path cannot be determined
     */
    private FileContent toFileContent(DocumentCreateRequest createRequest) {
        if (isAnyBlank(createRequest.organizationId(), createRequest.userId(), createRequest.fileName(), createRequest.payload())) {
            throw new IllegalArgumentException("Document file path cannot be determined");
        }
        var content = Base64.getDecoder().decode(createRequest.payload().getBytes(StandardCharsets.UTF_8));
        return new FileContent(pathResolver.resolve(createRequest.organizationId(), createRequest.userId(), createRequest.fileName()), content);
    }
}