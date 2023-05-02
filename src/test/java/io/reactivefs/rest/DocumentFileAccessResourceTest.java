package io.reactivefs.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.reactivefs.RFSConfig;
import io.reactivefs.ext.DocumentAccessResourceService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(FileAccessResourceWireMockExtension.class)
public class DocumentFileAccessResourceTest {

    @ConfigProperty(name = RFSConfig.USER_DOCUMENT_ROOT_DIRECTORY)
    String userDocumentRootDirectory;

    @Test
    void whenGetUserDocumentWithoutToken() {
        given()
            .when()
            .header("Accept", "application/octet-stream")
            .get("/api/document/1")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getUserDocumentInvalidFileAccess() {
        given()
            .when()
            .header(DocumentAccessResourceService.TOKEN_HEADER, "test-token")
            .header("Accept", "application/octet-stream")
            .get("key")
            .then()
            .statusCode(RestResponse.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void whenGetUserDocumentUsingInvalidToken() {
        given()
            .when()
            .header(DocumentAccessResourceService.TOKEN_HEADER, "invalid-token")
            .header("Accept", "application/octet-stream")
            .get("/api/document/3")
            .then()
            .statusCode(RestResponse.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void whenGetUserDocumentAndFileNotExist() {
        given()
            .when()
            .header(DocumentAccessResourceService.TOKEN_HEADER, "test-token")
            .header("Accept", "application/octet-stream")
            .get("/api/document/1")
            .then()
            .statusCode(RestResponse.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void whenGetUserDocumentAndFileAccessServiceIsNotAvailable() {
        given()
            .when()
            .header(DocumentAccessResourceService.TOKEN_HEADER, "test-token")
            .header("Accept", "application/octet-stream")
            .get("/api/document/4")
            .then()
            .statusCode(RestResponse.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getUserDocument() throws IOException {
        var userId = "1267890";
        var candidateDir = userId.substring(5);
        var organizationId = "FAKE";
        var fileName = "document.pdf";
        var path = Files.createDirectories(Paths.get(userDocumentRootDirectory, organizationId.toLowerCase(), candidateDir));
        var tempFile = Files.createFile(path.resolve(fileName));
        try {
            Files.write(tempFile, "fake".getBytes());

            given()
                .when()
                .header("Accept", "application/octet-stream")
                .header(DocumentAccessResourceService.TOKEN_HEADER, "test-token")
                .get("/api/document/1")
                .then()
                .statusCode(RestResponse.Status.OK.getStatusCode())
                .body(is("fake"));
        } finally {
            Files.delete(tempFile);
        }
    }
}
