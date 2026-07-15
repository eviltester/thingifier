package uk.co.compendiumdev.thingifier.yaml;

import com.google.gson.Gson;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.SchemaDefinitionValidationReport;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelAssembler;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

class ThingifierYamlLoaderTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "minimal-todo.yaml",
                "todo-manager.yaml",
                "relationships-one-way.yaml",
                "relationships-two-way.yaml",
                "field-types.yaml",
                "validations.yaml",
                "object-fields.yaml",
                "max-instances.yaml"
            })
    void validYamlResourcesLoadIntoDefinitions(final String resourceName) {
        ThingifierModelDefinition definition =
                new ThingifierYamlLoader().loadDefinition(resource(resourceName));

        SchemaDefinitionValidationReport report =
                new ThingifierModelAssembler().validate(definition);

        Assertions.assertTrue(report.isValid(), report.combinedMessages());
        Assertions.assertFalse(definition.entities().isEmpty());
    }

    @Test
    void loaderBuildsThingifierFromPath() throws Exception {
        Thingifier thingifier =
                new ThingifierYamlLoader().loadThingifier(resourcePath("minimal-todo.yaml"));

        Assertions.assertEquals("Minimal Todo", thingifier.getTitle());
        Assertions.assertNotNull(thingifier.getDefinitionNamed("todo"));
        Assertions.assertEquals(
                "id", thingifier.getDefinitionNamed("todo").getPrimaryKeyField().getName());
    }

    @Test
    void invalidSemanticYamlReportsValidationErrors() {
        ThingifierYamlException error =
                Assertions.assertThrows(
                        ThingifierYamlException.class,
                        () ->
                                new ThingifierYamlLoader()
                                        .loadThingifier(resource("invalid-primary-key.yaml")));

        Assertions.assertTrue(
                error.getMessage().contains("Primary key field missing is not defined"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "invalid-missing-entity.yaml",
                "invalid-primary-key.yaml",
                "invalid-relationship-target.yaml",
                "invalid-field-type.yaml"
            })
    void invalidYamlResourcesLoadAsDefinitionsButDoNotAssemble(final String resourceName) {
        ThingifierModelDefinition definition =
                new ThingifierYamlLoader().loadDefinition(resource(resourceName));

        SchemaDefinitionValidationReport report =
                new ThingifierModelAssembler().validate(definition);

        Assertions.assertFalse(report.isValid());
    }

    @Test
    void malformedYamlReportsParseErrorSeparatelyFromSemanticErrors() {
        ThingifierYamlException error =
                Assertions.assertThrows(
                        ThingifierYamlException.class,
                        () -> new ThingifierYamlLoader().loadDefinition("formatVersion: ["));

        Assertions.assertTrue(error.getMessage().contains("Could not parse YAML schema"));
    }

    @Test
    void yamlLoadedTodoManagerMatchesJavaDefinedSchemaShape() {
        Thingifier yamlThingifier =
                new ThingifierYamlLoader().loadThingifier(resource("todo-manager.yaml"));
        Thingifier javaThingifier = new TodoManagerThingifier().get();

        Stream.of("todo", "project", "category")
                .forEach(
                        entityName ->
                                assertEntityMatches(javaThingifier, yamlThingifier, entityName));
        Stream.of("tasks", "tasksof", "categories", "todos", "projects")
                .forEach(
                        relationship ->
                                Assertions.assertTrue(
                                        yamlThingifier.hasRelationshipNamed(relationship)));
    }

    @Test
    void yamlLoadedModelCanBackExistingNonHttpApiPath() {
        Thingifier thingifier =
                new ThingifierYamlLoader().loadThingifier(resource("minimal-todo.yaml"));
        Map<String, String> body = new HashMap<>();
        body.put("title", "Yaml API task");

        ApiResponse postResponse =
                thingifier.api().post("todo", bodyParser(thingifier, body), new HttpHeadersBlock());

        Assertions.assertEquals(201, postResponse.getStatusCode());
        Assertions.assertEquals(
                "Yaml API task",
                postResponse.getReturnedInstance().getFieldValue("title").asString());

        ApiResponse getResponse =
                thingifier
                        .api()
                        .get(
                                "/todo/" + postResponse.getReturnedInstance().getPrimaryKeyValue(),
                                new QueryFilterParams(),
                                new HttpHeadersBlock());

        Assertions.assertEquals(200, getResponse.getStatusCode());
        Assertions.assertEquals(
                postResponse.getReturnedInstance(), getResponse.getReturnedInstance());
    }

    private void assertEntityMatches(
            final Thingifier expected, final Thingifier actual, final String entityName) {
        EntityDefinition expectedEntity = expected.getDefinitionNamed(entityName);
        EntityDefinition actualEntity = actual.getDefinitionNamed(entityName);

        Assertions.assertEquals(expectedEntity.getPlural(), actualEntity.getPlural());
        Assertions.assertEquals(
                expectedEntity.getPrimaryKeyField().getName(),
                actualEntity.getPrimaryKeyField().getName());
        Assertions.assertEquals(expectedEntity.getFieldNames(), actualEntity.getFieldNames());
    }

    private BodyParser bodyParser(final Thingifier thingifier, final Map<String, String> body) {
        return new BodyParser(
                new HttpApiRequest("/path").setBody(new Gson().toJson(body)),
                thingifier.getThingNames());
    }

    private InputStream resource(final String resourceName) {
        InputStream stream = getClass().getResourceAsStream("/models/" + resourceName);
        Assertions.assertNotNull(stream, resourceName);
        return stream;
    }

    private Path resourcePath(final String resourceName) throws URISyntaxException {
        return Path.of(getClass().getResource("/models/" + resourceName).toURI());
    }
}
