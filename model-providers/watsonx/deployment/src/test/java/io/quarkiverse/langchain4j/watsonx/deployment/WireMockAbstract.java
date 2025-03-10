package io.quarkiverse.langchain4j.watsonx.deployment;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.options;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.trace;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.API_KEY;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.BEARER_TOKEN;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.GRANT_TYPE;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.IAM_200_RESPONSE;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.PORT_IAM_SERVER;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.PORT_WATSONX_SERVER;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.PORT_WX_SERVER;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_IAM_GENERATE_TOKEN;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.VERSION;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import io.quarkiverse.langchain4j.watsonx.client.WatsonxRestApi;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;

public abstract class WireMockAbstract {

    static WireMockServer watsonxServer;
    static WireMockServer wxServer;
    static WireMockServer iamServer;
    static ObjectMapper mapper;

    @Inject
    LangChain4jWatsonxConfig langchain4jWatsonConfig;

    @BeforeAll
    static void beforeAll() {
        mapper = WatsonxRestApi.objectMapper(new ObjectMapper());

        watsonxServer = new WireMockServer(options().port(PORT_WATSONX_SERVER));
        watsonxServer.start();

        wxServer = new WireMockServer(options().port(PORT_WX_SERVER));
        wxServer.start();

        iamServer = new WireMockServer(options().port(PORT_IAM_SERVER));
        iamServer.start();
    }

    @AfterAll
    static void afterAll() {
        watsonxServer.stop();
        wxServer.stop();
        iamServer.stop();
    }

    @BeforeEach
    void beforeEach() throws Exception {
        watsonxServer.resetAll();
        wxServer.resetAll();
        iamServer.resetAll();
        handlerBeforeEach();
    }

    void handlerBeforeEach() throws Exception {
    };

    /**
     * Builder to mock the IAM server.
     */
    public IAMBuilder mockIAMBuilder(int status) {
        return new IAMBuilder(status);
    }

    /**
     *
     * Builder to mock the wx server.
     */
    public WxBuilder mockWxBuilder(String apiURL, int status) {
        return new WxBuilder(apiURL, status);
    }

    /**
     *
     * Builder to mock the wx server.
     */
    public WxBuilder mockWxBuilder(String apiURL, int status, String version) {
        return new WxBuilder(apiURL, status, version);
    }

    /**
     *
     * Builder to mock the wx server.
     */
    public WxBuilder mockWxBuilder(RequestMethod method, String apiURL, int status, String version) {
        return new WxBuilder(method, apiURL, status, version);
    }

    /**
     *
     * Builder to mock the Watsonx.ai server.
     */
    public WatsonxBuilder mockWatsonxBuilder(String apiURL, int status) {
        return new WatsonxBuilder(apiURL, status);
    }

    /**
     *
     * Builder to mock the Watsonx.ai server.
     */
    public WatsonxBuilder mockWatsonxBuilder(String apiURL, int status, String version) {
        return new WatsonxBuilder(apiURL, status, version);
    }

    /**
     *
     * Builder to mock the Watsonx.ai server.
     */
    public WatsonxBuilder mockWatsonxBuilder(RequestMethod method, String apiURL, int status, String version) {
        return new WatsonxBuilder(method, apiURL, status, version);
    }

    public static class IAMBuilder {

        private MappingBuilder builder;
        private String apikey = API_KEY;
        private String grantType = GRANT_TYPE;
        private String responseMediaType = MediaType.APPLICATION_JSON;
        private String response = "";
        private int status;

        protected IAMBuilder(int status) {
            this.status = status;
            this.builder = post(urlEqualTo(URL_IAM_GENERATE_TOKEN));
        }

        public IAMBuilder scenario(String currentState, String nextState) {
            builder = builder.inScenario("")
                    .whenScenarioStateIs(currentState)
                    .willSetStateTo(nextState);
            return this;
        }

        public IAMBuilder apikey(String apikey) {
            this.apikey = apikey;
            return this;
        }

        public IAMBuilder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public IAMBuilder responseMediaType(String mediaType) {
            this.responseMediaType = mediaType;
            return this;
        }

        public IAMBuilder response(String response) {
            this.response = response;
            return this;
        }

        public IAMBuilder response(String token, Date expiration) {
            this.response = IAM_200_RESPONSE.formatted(token, TimeUnit.MILLISECONDS.toSeconds(expiration.getTime()));
            return this;
        }

        public void build() {
            iamServer.stubFor(
                    builder.withHeader("Content-Type", equalTo(MediaType.APPLICATION_FORM_URLENCODED))
                            .withFormParam("apikey", equalTo(apikey))
                            .withFormParam("grant_type", equalTo(grantType))
                            .willReturn(aResponse()
                                    .withStatus(status)
                                    .withHeader("Content-Type", responseMediaType)
                                    .withBody(response)));
        }
    }

    public static abstract class ServerBuilder {

        private MappingBuilder builder;
        private String token = BEARER_TOKEN;
        private String responseMediaType = MediaType.APPLICATION_JSON;
        private String response;
        private int status;

        protected ServerBuilder(RequestMethod method, String apiURL, int status, String version) {
            this.builder = switch (method.getName()) {
                case "GET" -> get(urlEqualTo(apiURL.formatted(version)));
                case "POST" -> post(urlEqualTo(apiURL.formatted(version)));
                case "PUT" -> put(urlEqualTo(apiURL.formatted(version)));
                case "DELETE" -> delete(urlEqualTo(apiURL.formatted(version)));
                case "PATCH" -> patch(urlEqualTo(apiURL.formatted(version)));
                case "OPTIONS" -> options(urlEqualTo(apiURL.formatted(version)));
                case "HEAD" -> head(urlEqualTo(apiURL.formatted(version)));
                case "TRACE" -> trace(urlEqualTo(apiURL.formatted(version)));
                default -> throw new IllegalArgumentException("Unknown request method: " + method);
            };
            this.status = status;
        }

        protected ServerBuilder(String apiURL, int status, String version) {
            this(RequestMethod.POST, apiURL, status, version);
        }

        protected ServerBuilder(String apiURL, int status) {
            this(RequestMethod.POST, apiURL, status, VERSION);
        }

        public ServerBuilder scenario(String currentState, String nextState) {
            builder = builder.inScenario("")
                    .whenScenarioStateIs(currentState)
                    .willSetStateTo(nextState);
            return this;
        }

        public ServerBuilder bodyIgnoreOrder(String body) {
            builder.withRequestBody(equalToJson(body, true, false));
            return this;
        }

        public ServerBuilder body(String body) {
            builder.withRequestBody(equalToJson(body));
            return this;
        }

        public ServerBuilder body(StringValuePattern stringValuePattern) {
            builder.withRequestBody(stringValuePattern);
            return this;
        }

        public ServerBuilder token(String token) {
            this.token = token;
            return this;
        }

        public ServerBuilder responseMediaType(String mediaType) {
            this.responseMediaType = mediaType;
            return this;
        }

        public ServerBuilder response(String response) {
            this.response = response;
            return this;
        }

        public abstract StubMapping build();
    }

    public static class WatsonxBuilder extends ServerBuilder {

        protected WatsonxBuilder(RequestMethod method, String apiURL, int status, String version) {
            super(method, apiURL, status, version);
        }

        protected WatsonxBuilder(String apiURL, int status, String version) {
            super(RequestMethod.POST, apiURL, status, version);
        }

        protected WatsonxBuilder(String apiURL, int status) {
            super(RequestMethod.POST, apiURL, status, VERSION);
        }

        @Override
        public StubMapping build() {
            return watsonxServer.stubFor(
                    super.builder
                            .withHeader("Authorization", equalTo("Bearer %s".formatted(super.token)))
                            .willReturn(aResponse()
                                    .withStatus(super.status)
                                    .withHeader("Content-Type", super.responseMediaType)
                                    .withBody(super.response)));
        }
    }

    public static class WxBuilder extends ServerBuilder {

        protected WxBuilder(RequestMethod method, String apiURL, int status, String version) {
            super(method, apiURL, status, version);
        }

        protected WxBuilder(String apiURL, int status, String version) {
            super(RequestMethod.POST, apiURL, status, version);
        }

        protected WxBuilder(String apiURL, int status) {
            super(RequestMethod.POST, apiURL, status, VERSION);
        }

        @Override
        public StubMapping build() {
            return wxServer.stubFor(
                    super.builder
                            .withHeader("Authorization", equalTo("Bearer %s".formatted(super.token)))
                            .willReturn(aResponse()
                                    .withStatus(super.status)
                                    .withHeader("Content-Type", super.responseMediaType)
                                    .withBody(super.response)));
        }
    }
}
