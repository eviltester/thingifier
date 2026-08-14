package uk.co.compendiumdev.thingifier.api.http.requests;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.AUTO_INCREMENT;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

class ThingifierHttpApiAdditionalRepresentationsTest {

    @ParameterizedTest
    @MethodSource("additionalRepresentationBodies")
    void getSelectsAdditionalResponseRepresentationFromAcceptHeader(
            final String acceptHeader, final String expectedBody) {
        ThingifierHttpApi api = taskApiWithOneTask();

        HttpApiResponse response =
                api.get(new HttpApiRequest("tasks").addHeader("Accept", acceptHeader));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(acceptHeader, response.getType());
        Assertions.assertEquals(expectedBody, response.getBody());
    }

    @ParameterizedTest
    @MethodSource("defaultAndWildcardAcceptHeaders")
    void getDefaultsToJsonForMissingAndWildcardAcceptHeaders(final String acceptHeader) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, "application/json", "\"tasks\"");
    }

    @ParameterizedTest
    @MethodSource("exactRepresentationAcceptHeaders")
    void getSelectsExactRepresentationFromAcceptHeader(
            final String acceptHeader,
            final String expectedContentType,
            final String bodyFragment) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, expectedContentType, bodyFragment);
    }

    @ParameterizedTest
    @MethodSource("structuredXmlWildcardAcceptHeaders")
    void getSelectsModelDerivedXmlFromStructuredXmlWildcardAcceptHeader(
            final String acceptHeader,
            final String expectedContentType,
            final String bodyFragment) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, expectedContentType, bodyFragment);
    }

    @ParameterizedTest
    @MethodSource("typeWildcardAcceptHeaders")
    void getSelectsRepresentationFromTypeWildcardAcceptHeader(
            final String acceptHeader,
            final String expectedContentType,
            final String bodyFragment) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, expectedContentType, bodyFragment);
    }

    @ParameterizedTest
    @MethodSource("structuredJsonFallbackHeaders")
    void getIgnoresUnsupportedStructuredJsonWhenJsonAlternativeExists(final String acceptHeader) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, "application/json", "\"tasks\"");
    }

    @ParameterizedTest
    @MethodSource("unsupportedStructuredJsonHeaders")
    void getRejectsUnsupportedStructuredJsonAcceptHeaders(final String acceptHeader) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 406, "application/json", "errorMessages");
    }

    @ParameterizedTest
    @MethodSource("issue103HttpQualityValueExamples")
    void issue103ExamplesNegotiateHttpResponsesByQualityValues(
            final String acceptHeader,
            final int expectedStatusCode,
            final String expectedContentType,
            final String bodyFragment) {
        assertNegotiatedGet(
                taskApiWithOneTask(),
                acceptHeader,
                expectedStatusCode,
                expectedContentType,
                bodyFragment);
    }

    @ParameterizedTest
    @MethodSource("xmlCompatibleQualityValueExamples")
    void xmlCompatibleMediaTypesUseHighestSupportedQualityValue(
            final String acceptHeader, final String expectedContentType) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, expectedContentType, "<tasks>");
    }

    @ParameterizedTest
    @MethodSource("unsupportedHigherQualityXmlFallbackExamples")
    void unsupportedHigherQualityXmlMediaRangesFallThroughToSupportedAlternatives(
            final String acceptHeader, final String expectedContentType) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, expectedContentType, "<tasks>");
    }

    @ParameterizedTest
    @MethodSource("qZeroWildcardFallbackExamples")
    void qZeroExactMediaTypeCanFallBackToAllowedWildcardRepresentation(
            final String acceptHeader,
            final String expectedContentType,
            final String bodyFragment) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, expectedContentType, bodyFragment);
    }

    @ParameterizedTest
    @MethodSource("structuredXmlRejectedByQZeroExamples")
    void qZeroExactStructuredXmlRejectsStructuredXmlWildcardFallback(final String acceptHeader) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 406, "application/json", "errorMessages");
    }

    @ParameterizedTest
    @MethodSource("specificityTieBreakerExamples")
    void specificityBeatsHeaderOrderWhenQualityValuesTie(
            final String acceptHeader, final String expectedContentType) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, expectedContentType, "<tasks>");
    }

    @ParameterizedTest
    @MethodSource("clientOrderTieBreakerExamples")
    void clientOrderBreaksTiesWhenQualityAndSpecificityAreTheSame(
            final String acceptHeader, final String expectedContentType) {
        assertNegotiatedGet(
                taskApiWithOneTask(), acceptHeader, 200, expectedContentType, "<tasks>");
    }

    @ParameterizedTest
    @MethodSource("unsupportedXmlMediaTypes")
    void unsupportedXmlBasedAcceptHeadersAreNotNormalResourceXml(final String mediaType) {
        assertNegotiatedGet(
                taskApiWithOneTask(), mediaType, 406, "application/json", "errorMessages");
    }

    @ParameterizedTest
    @MethodSource("additionalRepresentationBodies")
    void querySelectsAdditionalResponseRepresentationFromAcceptHeader(
            final String acceptHeader, final String expectedBody) {
        HttpApiRequest request =
                new HttpApiRequest("tasks")
                        .addHeader("Content-Type", ThingifierHttpApi.QUERY_CONTENT_TYPE)
                        .addHeader("Accept", acceptHeader)
                        .setBody("title=Task");

        HttpApiResponse response = taskApiWithOneTask().queryRequest(request);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(acceptHeader, response.getType());
        Assertions.assertEquals(expectedBody, response.getBody());
    }

    @ParameterizedTest
    @MethodSource("additionalRepresentationBodies")
    void jsonPathQuerySelectsAdditionalResponseRepresentationFromAcceptHeader(
            final String acceptHeader, final String expectedBody) {
        HttpApiRequest request =
                new HttpApiRequest("tasks")
                        .addHeader("Content-Type", ThingifierHttpApi.JSONPATH_QUERY_CONTENT_TYPE)
                        .addHeader("Accept", acceptHeader)
                        .setBody("$.tasks[?(@.title == 'Task')]");

        HttpApiResponse response = taskApiWithOneTask().queryRequest(request);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(acceptHeader, response.getType());
        Assertions.assertEquals(expectedBody, response.getBody());
    }

    @ParameterizedTest
    @MethodSource("additionalRepresentationBodies")
    void structuredJsonQuerySelectsAdditionalResponseRepresentationFromAcceptHeader(
            final String acceptHeader, final String expectedBody) {
        HttpApiRequest request =
                new HttpApiRequest("tasks")
                        .addHeader("Content-Type", ThingifierHttpApi.STRUCTURED_QUERY_CONTENT_TYPE)
                        .addHeader("Accept", acceptHeader)
                        .setBody("{\"filter\":{\"title\":\"Task\"}}");

        HttpApiResponse response = taskApiWithOneTask().queryRequest(request);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(acceptHeader, response.getType());
        Assertions.assertEquals(expectedBody, response.getBody());
    }

    private static Stream<Arguments> additionalRepresentationBodies() {
        return Stream.of(
                Arguments.of("text/csv", "id,title\n1,Task"),
                Arguments.of("text/plain", "id=1, title=Task"),
                Arguments.of(
                        "text/html",
                        "<table><thead><tr><th>id</th><th>title</th></tr></thead>"
                                + "<tbody><tr><td>1</td><td>Task</td></tr></tbody></table>"),
                Arguments.of("application/x-ndjson", "{\"id\":1,\"title\":\"Task\"}\n"),
                Arguments.of("application/jsonl", "{\"id\":1,\"title\":\"Task\"}\n"),
                Arguments.of("application/json-seq", "\u001E{\"id\":1,\"title\":\"Task\"}\n"),
                Arguments.of("text/tab-separated-values", "id\ttitle\n1\tTask"),
                Arguments.of(
                        "text/xml", "<tasks><task><id>1</id><title>Task</title></task></tasks>"),
                Arguments.of(
                        "application/vnd.example.task+xml",
                        "<tasks><task><id>1</id><title>Task</title></task></tasks>"));
    }

    private static Stream<Arguments> defaultAndWildcardAcceptHeaders() {
        return Stream.of(
                Arguments.of((String) null), Arguments.of("*/*"), Arguments.of("application/*"));
    }

    private static Stream<Arguments> exactRepresentationAcceptHeaders() {
        return Stream.of(
                Arguments.of("application/json", "application/json", "\"tasks\""),
                Arguments.of("application/xml", "application/xml", "<tasks>"),
                Arguments.of("text/xml", "text/xml", "<tasks>"),
                Arguments.of(
                        "application/vnd.example.task+xml",
                        "application/vnd.example.task+xml",
                        "<tasks>"));
    }

    private static Stream<Arguments> structuredXmlWildcardAcceptHeaders() {
        return Stream.of(Arguments.of("application/*+xml", "application/task+xml", "<tasks>"));
    }

    private static Stream<Arguments> typeWildcardAcceptHeaders() {
        return Stream.of(Arguments.of("text/*", "text/csv", "id,title"));
    }

    private static Stream<Arguments> structuredJsonFallbackHeaders() {
        return Stream.of(
                Arguments.of("application/json, application/problem+json"),
                Arguments.of("application/problem+json, application/json;q=0.5"));
    }

    private static Stream<Arguments> unsupportedStructuredJsonHeaders() {
        return Stream.of(
                Arguments.of("application/problem+json"), Arguments.of("application/*+json"));
    }

    private static Stream<Arguments> issue103HttpQualityValueExamples() {
        return Stream.of(
                Arguments.of(
                        "application/xml;q=1, application/json;q=0.5",
                        200,
                        "application/xml",
                        "<tasks>"),
                Arguments.of(
                        "application/json;q=1, application/xml;q=0.5",
                        200,
                        "application/json",
                        "\"tasks\""),
                Arguments.of(
                        "application/xml;q=0.2, application/json;q=0.9",
                        200,
                        "application/json",
                        "\"tasks\""),
                Arguments.of(
                        "application/json;q=0, application/xml;q=1",
                        200,
                        "application/xml",
                        "<tasks>"),
                Arguments.of(
                        "application/json;q=0, application/xml;q=0",
                        406,
                        "application/json",
                        "errorMessages"),
                Arguments.of(
                        "application/xml, application/json;q=0.5",
                        200,
                        "application/xml",
                        "<tasks>"),
                Arguments.of("*/*;q=0.8, application/xml;q=0.9", 200, "application/xml", "<tasks>"),
                Arguments.of(
                        "application/gzip;q=1, application/json;q=0.5",
                        200,
                        "application/json",
                        "\"tasks\""));
    }

    private static Stream<Arguments> xmlCompatibleQualityValueExamples() {
        return Stream.of(
                Arguments.of(
                        "application/json;q=0.5, "
                                + "application/xml;q=0.6, "
                                + "text/xml;q=0.8, "
                                + "application/vnd.example.task+xml;q=0.9",
                        "application/vnd.example.task+xml"),
                Arguments.of(
                        "application/json;q=0.5, "
                                + "application/xml;q=0.6, "
                                + "text/xml;q=0.8, "
                                + "application/*+xml;q=0.9",
                        "application/task+xml"));
    }

    private static Stream<Arguments> unsupportedHigherQualityXmlFallbackExamples() {
        return Stream.of(
                Arguments.of("application/problem+xml;q=1, text/xml;q=0.4", "text/xml"),
                Arguments.of(
                        "application/soap+xml;q=1, application/*+xml;q=0.4",
                        "application/task+xml"));
    }

    private static Stream<Arguments> qZeroWildcardFallbackExamples() {
        return Stream.of(
                Arguments.of(
                        "application/xml;q=0, application/*;q=0.8",
                        "application/json",
                        "\"tasks\""),
                Arguments.of("text/xml;q=0, text/*;q=0.8", "text/csv", "id,title"));
    }

    private static Stream<Arguments> structuredXmlRejectedByQZeroExamples() {
        return Stream.of(Arguments.of("application/task+xml;q=0, application/*+xml;q=0.8"));
    }

    private static Stream<Arguments> specificityTieBreakerExamples() {
        return Stream.of(
                Arguments.of("application/*;q=0.8, application/xml;q=0.8", "application/xml"),
                Arguments.of("text/*;q=0.8, text/xml;q=0.8", "text/xml"),
                Arguments.of(
                        "application/*+xml;q=0.8, application/vnd.example.task+xml;q=0.8",
                        "application/vnd.example.task+xml"));
    }

    private static Stream<Arguments> clientOrderTieBreakerExamples() {
        return Stream.of(
                Arguments.of("text/xml;q=0.8, application/xml;q=0.8", "text/xml"),
                Arguments.of("application/xml;q=0.8, text/xml;q=0.8", "application/xml"),
                Arguments.of(
                        "application/vnd.example.task+xml;q=0.8, text/xml;q=0.8",
                        "application/vnd.example.task+xml"),
                Arguments.of("text/xml;q=0.8, application/vnd.example.task+xml;q=0.8", "text/xml"));
    }

    private static Stream<Arguments> unsupportedXmlMediaTypes() {
        return Stream.of(
                Arguments.of("application/problem+xml"),
                Arguments.of("application/soap+xml"),
                Arguments.of("application/xhtml+xml"),
                Arguments.of("image/svg+xml"),
                Arguments.of("application/atom+xml"),
                Arguments.of("application/rss+xml"));
    }

    private void assertNegotiatedGet(
            final ThingifierHttpApi api,
            final String acceptHeader,
            final int statusCode,
            final String contentType,
            final String bodyFragment) {
        HttpApiRequest request = new HttpApiRequest("tasks");
        if (acceptHeader != null) {
            request.addHeader("Accept", acceptHeader);
        }

        HttpApiResponse response = api.get(request);

        Assertions.assertEquals(statusCode, response.getStatusCode(), acceptHeader);
        Assertions.assertEquals(contentType, response.getType(), acceptHeader);
        Assertions.assertTrue(response.getBody().contains(bodyFragment), response.getBody());
    }

    private ThingifierHttpApi taskApiWithOneTask() {
        Thingifier thingifier = taskThingifier();
        createTask(thingifier, "Task");
        return new ThingifierHttpApi(thingifier);
    }

    private Thingifier taskThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", AUTO_INCREMENT));
        task.addField(Field.is("title", STRING));
        return thingifier;
    }

    private void createTask(final Thingifier thingifier, final String title) {
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(task).withField("title", title));
    }
}
