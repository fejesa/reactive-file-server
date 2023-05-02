package io.reactivefs.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.reactivefs.ext.DocumentAccessResourceService;
import io.reactivefs.model.DocumentFileAccess;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;

public class FileAccessResourceWireMockExtension implements QuarkusTestResourceLifecycleManager {

    private static final int WIREMOCK_PORT = 7777;

    private static final String BASE_PATH = "/api";

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.start();
        try {
            stubExtensions();
            return Collections.singletonMap(
                    "quarkus.rest-client.\"io.reactivefs.ext.DocumentAccessResourceService\".url",
                    wireMockServer.baseUrl() + BASE_PATH);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private void stubExtensions() throws JsonProcessingException {
        wireMockServer.stubFor(
                get(urlEqualTo(BASE_PATH + "/document-access/document/1"))
                        .withHeader(DocumentAccessResourceService.TOKEN_HEADER, equalTo("test-token"))
                        .willReturn(okJson(createFileAccessRequestBody("FAKE", "1267890", "document.pdf"))));

        wireMockServer.stubFor(
                get(urlEqualTo(BASE_PATH + "/document-access/document/2"))
                        .withHeader(DocumentAccessResourceService.TOKEN_HEADER, equalTo("test-token"))
                        .willReturn(okJson(createFileAccessRequestBody("", "", ""))));

        wireMockServer.stubFor(
                get(urlEqualTo(BASE_PATH + "/document-access/document/3"))
                        .withHeader(DocumentAccessResourceService.TOKEN_HEADER, equalTo("invalid-token"))
                        .willReturn(okJson(createFileAccessRequestBody("", "", ""))));

        wireMockServer.stubFor(
                get(urlEqualTo(BASE_PATH + "/document-access/document/4"))
                        .withHeader(DocumentAccessResourceService.TOKEN_HEADER, equalTo("test-token"))
                        .willReturn(aResponse().withStatus(500).withFixedDelay(1500)));

        wireMockServer.stubFor(
                get(urlEqualTo(BASE_PATH + "/document-access/key"))
                        .withHeader(DocumentAccessResourceService.API_KEY_HEADER, equalTo("apikey"))
                        .willReturn(okJson(new ObjectMapper().writeValueAsString(Boolean.TRUE))));

        wireMockServer.stubFor(
                get(urlEqualTo(BASE_PATH + "/document-access/key"))
                        .withHeader(DocumentAccessResourceService.API_KEY_HEADER, equalTo("invalid-apikey"))
                        .willReturn(okJson(new ObjectMapper().writeValueAsString(Boolean.FALSE))));

        wireMockServer.stubFor(
                get(urlEqualTo(BASE_PATH + "/document-access/key"))
                        .withHeader(DocumentAccessResourceService.API_KEY_HEADER, equalTo("invalid-delayed-apikey"))
                        .willReturn(aResponse().withStatus(500).withFixedDelay(100)));

        wireMockServer.stubFor(
                get(urlEqualTo(BASE_PATH + "/document-access/key"))
                        .withHeader(DocumentAccessResourceService.API_KEY_HEADER, equalTo("delayed-apikey"))
                        .willReturn(okJson(new ObjectMapper().writeValueAsString(Boolean.TRUE)).withFixedDelay(500)));
    }

    private String createFileAccessRequestBody(String organizationId, String userId, String fileName) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        return mapper.writeValueAsString(new DocumentFileAccess(organizationId, userId, fileName));
    }
}