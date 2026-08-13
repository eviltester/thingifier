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
                                    "Content-Type",
                                    ThingifierHttpApi.STRUCTURED_TODO_QUERY_CONTENT_TYPE)
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
        return expectedBodies;
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
