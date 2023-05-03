package io.reactivefs.service;

import io.reactivefs.io.FileContent;
import io.reactivefs.io.FileSystemHandler;
import io.reactivefs.model.DocumentCreateRequest;
import io.reactivefs.model.DocumentFileAccess;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

import static io.smallrye.mutiny.unchecked.Unchecked.function;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;

@Attachment
@ApplicationScoped
public class AttachmentDocumentStore implements DocumentStore {

    @Inject
    FileSystemHandler fileSystemHandler;

    @Attachment
    @Inject
    DocumentPathResolver pathResolver;

    @Override
    public Uni<Buffer> read(DocumentFileAccess fileAccess) {
        return Uni.createFrom().item(fileAccess)
            .map(function(this::attachmentPath))
            .onItem()
            .transformToUni(fileSystemHandler::readFile);
    }

    @Override
    public Uni<Void> write(DocumentCreateRequest createRequest) {
        return Uni.createFrom().item(createRequest)
            .map(function(this::toFileContent))
            .onItem()
            .call(fileContent -> fileSystemHandler.createDirectories(fileContent.path().getParent()))
            .onItem()
            .transformToUni(fileSystemHandler::writeFile);
    }

    private Path attachmentPath(DocumentFileAccess fileAccess) {
        if (isAnyBlank(fileAccess.organizationId(), fileAccess.userId(), fileAccess.fileName())) {
            throw new IllegalArgumentException("Attachment document file cannot be identified");
        }
        return pathResolver.resolve(fileAccess.organizationId(), null, fileAccess.fileName());
    }

    /**
     * Converts the document creation request to the final path and decodes the contents.
     *
     * @param createRequest This specifies the request for creating a file, which includes the content as well as the owners of the document.
     * @return The file path and content that should be written.
     * @throws IllegalArgumentException if the content is not Base64 format or the file path cannot be determined
     */
    private FileContent toFileContent(DocumentCreateRequest createRequest) {
        if (isAnyBlank(createRequest.organizationId(), createRequest.fileName(), createRequest.content())) {
            throw new IllegalArgumentException("Attachment document file path cannot be determined");
        }
        var content = Base64.getDecoder().decode(createRequest.content().getBytes(StandardCharsets.UTF_8));
        return new FileContent(pathResolver.resolve(createRequest.organizationId(), null, createRequest.fileName()), content);
    }
}
