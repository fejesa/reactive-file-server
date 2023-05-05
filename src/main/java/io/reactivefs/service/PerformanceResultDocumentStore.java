package io.reactivefs.service;

import io.reactivefs.io.FileSystemHandler;
import io.reactivefs.model.DocumentCreateRequest;
import io.reactivefs.model.DocumentFileAccess;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Path;

import static io.smallrye.mutiny.unchecked.Unchecked.function;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;

@PerformanceResult
@ApplicationScoped
public class PerformanceResultDocumentStore implements DocumentStore {

    @Inject
    FileSystemHandler fileSystemHandler;

    @PerformanceResult
    @Inject
    DocumentPathResolver pathResolver;

    @Override
    public Uni<Buffer> read(DocumentFileAccess fileAccess) {
        return Uni.createFrom().item(fileAccess)
            .map(function(this::performanceReportPath))
            .onItem()
            .transformToUni(fileSystemHandler::readFile);
    }

    /**
     * In this scenario we assume that the admins put - for example copying manually - the user performance report into the given folder.
     * @throws UnsupportedOperationException User performance report write is not supported
     */
    @Override
    public Uni<Void> write(DocumentCreateRequest createRequest) {
        throw new UnsupportedOperationException("User performance report write is not supported");
    }

    private Path performanceReportPath(DocumentFileAccess fileAccess) {
        if (isAnyBlank(fileAccess.organizationId(), fileAccess.userId())) {
            throw new IllegalArgumentException("User performance report file cannot be identified");
        }
        return pathResolver.resolve(fileAccess.organizationId(), fileAccess.userId(), null);
    }
}
