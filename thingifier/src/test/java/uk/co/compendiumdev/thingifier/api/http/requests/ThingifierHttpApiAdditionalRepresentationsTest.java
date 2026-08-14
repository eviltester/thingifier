package uk.co.compendiumdev.thingifier.api.http.requests;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.AUTO_INCREMENT;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

class ThingifierHttpApiAdditionalRepresentationsTest {

    @Test
    void getSelectsAdditionalResponseRepresentationsFromAcceptHeader() {
        Thingifier thingifier = taskThingifier();
        createTask(thingifier, "Task");
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier);

        for (Map.Entry<String, String> expected : expectedBodies().entrySet()) {
            HttpApiResponse response =
                    api.get(new HttpApiRequest("tasks").addHeader("Accept", expected.getKey()));

            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertEquals(expected.getKey(), response.getType());
            Assertions.assertEquals(expected.getValue(), response.getBody());
        }
    }

    @Test
    void getNegotiatesAcceptHeaderQualityValuesWildcardsAndStructuredJsonTypes() {
        Thingifier thingifier = taskThingifier();
        createTask(thingifier, "Task");
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier);

        assertNegotiatedGet(api, null, 200, "application/json", "\"tasks\"");
        assertNegotiatedGet(api, "*/*", 200, "application/json", "\"tasks\"");
        assertNegotiatedGet(api, "application/*", 200, "application/json", "\"tasks\"");
        assertNegotiatedGet(api, "text/*", 200, "text/csv", "id,title");
        assertNegotiatedGet(api, "application/json", 200, "application/json", "\"tasks\"");
        assertNegotiatedGet(api, "application/xml", 200, "application/xml", "<tasks>");
        assertNegotiatedGet(api, "text/xml", 200, "text/xml", "<tasks>");
        assertNegotiatedGet(
                api,
                "application/vnd.example.task+xml",
                200,
                "application/vnd.example.task+xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/*+xml",
                200,
                "application/task+xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/json, application/problem+json",
                200,
                "application/json",
                "\"tasks\"");
        assertNegotiatedGet(
                api,
                "application/problem+json, application/json;q=0.5",
                200,
                "application/json",
                "\"tasks\"");
        assertNegotiatedGet(
                api, "application/json;q=0, application/xml", 200, "application/xml", "<tasks>");
        assertNegotiatedGet(
                api, "application/problem+json", 406, "application/json", "errorMessages");
        assertNegotiatedGet(api, "application/*+json", 406, "application/json", "errorMessages");
        assertNegotiatedGet(api, "application/json;q=0", 406, "application/json", "errorMessages");
        assertIssue103AcceptHeaderQualityValueExamples(api);
        assertXmlCompatibleAcceptHeaderQualityValueCombinations(api);
        assertUnsupportedXmlBasedMediaTypesAreNotNormalResourceXml(api);
    }

    @Test
    void querySelectsAdditionalResponseRepresentationsFromAcceptHeader() {
        Thingifier thingifier = taskThingifier();
        createTask(thingifier, "Task");
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier);

        for (Map.Entry<String, String> expected : expectedBodies().entrySet()) {
            HttpApiRequest request =
                    new HttpApiRequest("tasks")
                            .addHeader("Content-Type", ThingifierHttpApi.QUERY_CONTENT_TYPE)
                            .addHeader("Accept", expected.getKey())
                            .setBody("title=Task");
            HttpApiResponse response = api.queryRequest(request);

            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertEquals(expected.getKey(), response.getType());
            Assertions.assertEquals(expected.getValue(), response.getBody());
        }
    }

    @Test
    void jsonPathQuerySelectsAdditionalResponseRepresentationsFromAcceptHeader() {
        Thingifier thingifier = taskThingifier();
        createTask(thingifier, "Task");
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier);

        for (Map.Entry<String, String> expected : expectedBodies().entrySet()) {
            HttpApiRequest request =
                    new HttpApiRequest("tasks")
                            .addHeader(
                                    "Content-Type", ThingifierHttpApi.JSONPATH_QUERY_CONTENT_TYPE)
                            .addHeader("Accept", expected.getKey())
                            .setBody("$.tasks[?(@.title == 'Task')]");
            HttpApiResponse response = api.queryRequest(request);

            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertEquals(expected.getKey(), response.getType());
            Assertions.assertEquals(expected.getValue(), response.getBody());
        }
    }

    @Test
    void structuredJsonQuerySelectsAdditionalResponseRepresentationsFromAcceptHeader() {
        Thingifier thingifier = taskThingifier();
        createTask(thingifier, "Task");
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier);

        for (Map.Entry<String, String> expected : expectedBodies().entrySet()) {
            HttpApiRequest request =
                    new HttpApiRequest("tasks")
                            .addHeader(
                                    "Content-Type", ThingifierHttpApi.STRUCTURED_QUERY_CONTENT_TYPE)
                            .addHeader("Accept", expected.getKey())
                            .setBody("{\"filter\":{\"title\":\"Task\"}}");
            HttpApiResponse response = api.queryRequest(request);

            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertEquals(expected.getKey(), response.getType());
            Assertions.assertEquals(expected.getValue(), response.getBody());
        }
    }

    private Map<String, String> expectedBodies() {
        Map<String, String> expectedBodies = new LinkedHashMap<>();
        expectedBodies.put("text/csv", "id,title\n1,Task");
        expectedBodies.put("text/plain", "id=1, title=Task");
        expectedBodies.put(
                "text/html",
                "<table><thead><tr><th>id</th><th>title</th></tr></thead>"
                        + "<tbody><tr><td>1</td><td>Task</td></tr></tbody></table>");
        expectedBodies.put("application/x-ndjson", "{\"id\":1,\"title\":\"Task\"}\n");
        expectedBodies.put("application/jsonl", "{\"id\":1,\"title\":\"Task\"}\n");
        expectedBodies.put("application/json-seq", "\u001E{\"id\":1,\"title\":\"Task\"}\n");
        expectedBodies.put("text/tab-separated-values", "id\ttitle\n1\tTask");
        expectedBodies.put("text/xml", "<tasks><task><id>1</id><title>Task</title></task></tasks>");
        expectedBodies.put(
                "application/vnd.example.task+xml",
                "<tasks><task><id>1</id><title>Task</title></task></tasks>");
        return expectedBodies;
    }

    private void assertIssue103AcceptHeaderQualityValueExamples(final ThingifierHttpApi api) {
        assertNegotiatedGet(
                api,
                "application/xml;q=1, application/json;q=0.5",
                200,
                "application/xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/json;q=1, application/xml;q=0.5",
                200,
                "application/json",
                "\"tasks\"");
        assertNegotiatedGet(
                api,
                "application/xml;q=0.2, application/json;q=0.9",
                200,
                "application/json",
                "\"tasks\"");
        assertNegotiatedGet(
                api,
                "application/json;q=0, application/xml;q=1",
                200,
                "application/xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/json;q=0, application/xml;q=0",
                406,
                "application/json",
                "errorMessages");
        assertNegotiatedGet(
                api,
                "application/xml, application/json;q=0.5",
                200,
                "application/xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "*/*;q=0.8, application/xml;q=0.9",
                200,
                "application/xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/gzip;q=1, application/json;q=0.5",
                200,
                "application/json",
                "\"tasks\"");
    }

    private void assertXmlCompatibleAcceptHeaderQualityValueCombinations(
            final ThingifierHttpApi api) {
        assertNegotiatedGet(
                api,
                "application/json;q=0.5, "
                        + "application/xml;q=0.6, "
                        + "text/xml;q=0.8, "
                        + "application/vnd.example.task+xml;q=0.9",
                200,
                "application/vnd.example.task+xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/json;q=0.5, "
                        + "application/xml;q=0.6, "
                        + "text/xml;q=0.8, "
                        + "application/*+xml;q=0.9",
                200,
                "application/task+xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/problem+xml;q=1, text/xml;q=0.4",
                200,
                "text/xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/soap+xml;q=1, application/*+xml;q=0.4",
                200,
                "application/task+xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/xml;q=0, application/*;q=0.8",
                200,
                "application/json",
                "\"tasks\"");
        assertNegotiatedGet(
                api,
                "text/xml;q=0, text/*;q=0.8",
                200,
                "text/csv",
                "id,title");
        assertNegotiatedGet(
                api,
                "application/task+xml;q=0, application/*+xml;q=0.8",
                406,
                "application/json",
                "errorMessages");
        assertNegotiatedGet(
                api,
                "application/*;q=0.8, application/xml;q=0.8",
                200,
                "application/xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "text/*;q=0.8, text/xml;q=0.8",
                200,
                "text/xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/*+xml;q=0.8, application/vnd.example.task+xml;q=0.8",
                200,
                "application/vnd.example.task+xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "text/xml;q=0.8, application/xml;q=0.8",
                200,
                "text/xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/xml;q=0.8, text/xml;q=0.8",
                200,
                "application/xml",
                "<tasks>");
        assertNegotiatedGet(
                api,
                "application/vnd.example.task+xml;q=0.8, text/xml;q=0.8",
                200,
                "application/vnd.example.task+xml",
                "<tasks>");
    }

    private void assertUnsupportedXmlBasedMediaTypesAreNotNormalResourceXml(
            final ThingifierHttpApi api) {
        for (String mediaType :
                java.util.List.of(
                        "application/problem+xml",
                        "application/soap+xml",
                        "application/xhtml+xml",
                        "image/svg+xml",
                        "application/atom+xml",
                        "application/rss+xml")) {
            assertNegotiatedGet(api, mediaType, 406, "application/json", "errorMessages");
        }
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
