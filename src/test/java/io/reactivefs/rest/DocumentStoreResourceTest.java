package io.reactivefs.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.reactivefs.ext.DocumentAccessResourceService;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(FileAccessResourceWireMockExtension.class)
public class DocumentStoreResourceTest {

    @Test
    void whenUserDocumentRemovalWithValidApiKeyProvided() {
        given()
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .delete("/api/document/orgId/7654321/file1")
            .then()
            .statusCode(RestResponse.Status.ACCEPTED.getStatusCode())
            .body(is("true"));
    }

    @Test
    void whenUserDocumentRemovalWithMissingApiKeyProvided() {
        given()
            .when()
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .delete("/api/document/orgId/7654321/file1")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode())
            .body(is("{\"title\":\"Constraint Violation\",\"status\":400,\"violations\":[{\"field\":\"removeUserDocument.apiKey\",\"message\":\"must not be null\"}]}"));
    }

    @Test
    void whenUserDocumentRemovalWithInvalidApiKeyProvided() {
        given()
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "invalid-apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .delete("/api/document/orgId/7654321/file1")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void whenUserDocumentCreateWithValidApiKeyProvided() {
        given()
            .body("{\"organizationId\":\"ORGID\",\"userId\":\"1234567\",\"fileName\":\"sample.tmp\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/document")
            .then()
            .statusCode(RestResponse.Status.CREATED.getStatusCode())
            .body(is("true"));
    }

    @Test
    void whenUserDocumentCreateWithMissingApiKeyProvided() {
        given()
            .body("{\"organizationId\":\"orgId\",\"userId\":\"1234567\",\"fileName\":\"sample.tmp\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/document")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode())
            .body(is("{\"title\":\"Constraint Violation\",\"status\":400,\"violations\":[{\"field\":\"createUserDocument.apiKey\",\"message\":\"must not be null\"}]}"));
    }

    @Test
    void whenUserDocumentCreateWithMissingUserIdProvided() {
        given()
            .body("{\"organizationId\":\"orgid\",\"userId\":\"\",\"fileName\":\"sample.tmp\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/document")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode())
            .body(is("false"));
    }

    @Test
    void whenUserDocumentCreateWithInvalidFileContentEncodingProvided() {
        given()
            .body("{\"organizationId\":\"orgid\",\"userId\":\"1234567\",\"fileName\":\"sample.tmp\",\"content\":\"cHJvc3RkZXY_YmxvZw==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/document")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode())
            .body(is("false"));
    }

    @Test
    void whenUserDocumentCreateWithInvalidApiKeyProvided() {
        given()
            .body("{\"organizationId\":\"orgid\",\"userId\":\"1234567\",\"fileName\":\"sample.tmp\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "invalid-apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/document")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void whenUserDocumentCreateWithApiKeyCheckFailureDueToRemoteIsNotAvailable() {
        given()
            .body("{\"organizationId\":\"orgid\",\"userId\":\"1234567\",\"fileName\":\"sample.pdf\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "invalid-delayed-apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/document")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void whenAttachmentCreateWithMissingApiKeyProvided() {
        given()
            .body("{\"organizationId\":\"orgid\",\"userId\":\"\",\"fileName\":\"sample-attachment.tmp\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/attachment")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode())
            .body(is("{\"title\":\"Constraint Violation\",\"status\":400,\"violations\":[{\"field\":\"createAttachment.apiKey\",\"message\":\"must not be null\"}]}"));
    }

    @Test
    void whenAttachmentCreateWithValidApiKeyProvided() {
        given()
            .body("{\"organizationId\":\"orgid\",\"userId\":\"\",\"fileName\":\"sample-attachment.tmp\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/attachment")
            .then()
            .statusCode(RestResponse.Status.CREATED.getStatusCode())
            .body(is("true"));
    }

    @Test
    void whenAttachmentCreateWithMissingOrganizationProvided() {
        given()
            .body("{\"organizationId\":\"\",\"userId\":\"\",\"fileName\":\"sample.tmp\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/attachment")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode())
            .body(is("false"));
    }

    @Test
    void whenAttachmentCreateWithInvalidFileContentEncodingProvided() {
        given()
            .body("{\"organizationId\":\"orgid\",\"userId\":\"\",\"fileName\":\"sample.tmp\",\"content\":\"cHJvc3RkZXY_YmxvZw==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/attachment")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode())
            .body(is("false"));
    }

    @Test
    void whenAttachmentCreateWithInvalidApiKeyProvided() {
        given()
            .body("{\"organizationId\":\"orgid\",\"userId\":\"\",\"fileName\":\"sample.tmp\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "invalid-apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/attachment")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void whenAttachmentCreateWithApiKeyCheckFailureDueToRemoteIsNotAvailable() {
        given()
            .body("{\"organizationId\":\"orgid\",\"userId\":\"1234567\",\"fileName\":\"sample.tmp\",\"content\":\"cGF5bG9hZA==\"}")
            .when()
            .header(DocumentAccessResourceService.API_KEY_HEADER, "invalid-delayed-apikey")
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .post("api/attachment")
            .then()
            .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void multipleAttachmentRequest() {
        IntStream.range(0, 100).parallel().forEach(i -> {
            given()
                .body("{\"organizationId\":\"orgid\",\"userId\":\"\",\"fileName\":\"sample-attachment" + i + ".tmp\",\"content\":\"cGF5bG9hZA==\"}")
                .when()
                .header(DocumentAccessResourceService.API_KEY_HEADER, "delayed-apikey")
                .header("Accept", MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post("api/attachment")
                .then()
                .statusCode(RestResponse.Status.CREATED.getStatusCode())
                .body(is("true"));

            given()
                .body("{\"organizationId\":\"orgid\",\"userId\":\"\",\"fileName\":\"sample-attachment.tmp\",\"content\":\"cGF5bG9hZA==\"}")
                .when()
                .header(DocumentAccessResourceService.API_KEY_HEADER, "invalid-delayed-apikey")
                .header("Accept", MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post("api/attachment")
                .then()
                .statusCode(RestResponse.Status.BAD_REQUEST.getStatusCode());
        });
    }
}
