package uk.co.compendiumdev.thingifier.swaggerizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.NotEmptyValidationRule;

class SwaggerizerStrongSchemaTest {

    @Test
    void openApi30StrongSchemaSerializesKnownFieldTypes() {
        final JsonObject document =
                JsonParser.parseString(
                                new Swaggerizer(apiDefn(todoModel()))
                                        .asJson(
                                                strongConfig(
                                                        OpenApiSpecificationVersion.OPENAPI_3_0)))
                        .getAsJsonObject();
        final JsonObject schemas =
                document.getAsJsonObject("components").getAsJsonObject("schemas");
        final JsonObject createTodoProperties =
                schemas.getAsJsonObject("create_todo").getAsJsonObject("properties");
        final JsonObject todoProperties =
                schemas.getAsJsonObject("todo").getAsJsonObject("properties");

        Assertions.assertEquals(
                "string", createTodoProperties.getAsJsonObject("title").get("type").getAsString());
        Assertions.assertEquals(
                "boolean",
                createTodoProperties.getAsJsonObject("doneStatus").get("type").getAsString());
        Assertions.assertEquals(
                "integer", todoProperties.getAsJsonObject("id").get("type").getAsString());
    }

    @Test
    void strongSchemaDocumentsStringValidationConstraints() {
        final JsonObject document =
                JsonParser.parseString(
                                new Swaggerizer(apiDefn(todoModel()))
                                        .asJson(
                                                strongConfig(
                                                        OpenApiSpecificationVersion.OPENAPI_3_1)))
                        .getAsJsonObject();
        final JsonObject createTodo =
                document.getAsJsonObject("components")
                        .getAsJsonObject("schemas")
                        .getAsJsonObject("create_todo");
        final JsonObject title = createTodo.getAsJsonObject("properties").getAsJsonObject("title");

        Assertions.assertEquals(
                "title", createTodo.getAsJsonArray("required").get(0).getAsString());
        Assertions.assertEquals(1, title.get("minLength").getAsInt());
        Assertions.assertEquals(50, title.get("maxLength").getAsInt());
    }

    @Test
    void guidCustomHeaderUsesStringUuidSchema() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET,
                                "/challenger",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation("read challenger")
                        .addPossibleStatuses(200)
                        .addCustomHeader("X-CHALLENGER", "guid"));

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();
        final Schema<?> headerSchema =
                openApi.getPaths().get("/challenger").getGet().getParameters().get(0).getSchema();

        Assertions.assertEquals("string", headerSchema.getType());
        Assertions.assertEquals("uuid", headerSchema.getFormat());
    }

    @Test
    void operationPathParameterModeMovesPathParametersOntoOperations() {
        final SwaggerGenerationConfig config = new SwaggerGenerationConfig();
        config.pathParameterPlacement = SwaggerGenerationConfig.PathParameterPlacement.OPERATION;
        final OpenAPI openApi = new Swaggerizer(apiDefn(todoModel())).swagger(config);
        final PathItem path = openApi.getPaths().get("/todos/{id}");
        final Operation get = path.getGet();

        Assertions.assertTrue(path.getParameters() == null || path.getParameters().isEmpty());
        Assertions.assertEquals(Set.of("id"), parameterNames(get));
        Assertions.assertEquals("integer", get.getParameters().get(0).getSchema().getType());
    }

    @Test
    void generatedErrorResponsesUseThingifierErrorSchema() {
        final OpenAPI openApi = new Swaggerizer(apiDefn(todoModel())).swagger();
        final Schema<?> errorSchema =
                openApi.getPaths()
                        .get("/todos/{id}")
                        .getGet()
                        .getResponses()
                        .get("404")
                        .getContent()
                        .get("application/json")
                        .getSchema();

        Assertions.assertEquals(
                "#/components/schemas/" + Swaggerizer.THINGIFIER_ERROR_SCHEMA_NAME,
                errorSchema.get$ref());
        Assertions.assertTrue(
                openApi.getComponents()
                        .getSchemas()
                        .get(Swaggerizer.THINGIFIER_ERROR_SCHEMA_NAME)
                        .getProperties()
                        .containsKey("errorMessages"));
    }

    private Set<String> parameterNames(final Operation operation) {
        return operation.getParameters().stream()
                .map(Parameter::getName)
                .collect(Collectors.toSet());
    }

    private SwaggerGenerationConfig strongConfig(final OpenApiSpecificationVersion version) {
        final SwaggerGenerationConfig config = new SwaggerGenerationConfig();
        config.openApiSpecificationVersion = version;
        config.strongSchemas = true;
        return config;
    }

    private ThingifierApiDocumentationDefn apiDefn(final Thingifier thingifier) {
        return new ThingifierApiDocumentationDefn().setThingifier(thingifier);
    }

    private Thingifier todoModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition todo = thingifier.defineThing("todo", "todos");
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT).withExample("1"));
        todo.addField(
                Field.is("title", FieldType.STRING)
                        .makeMandatory()
                        .withValidation(
                                new NotEmptyValidationRule(), new MaximumLengthValidationRule(50)));
        todo.addField(Field.is("doneStatus", FieldType.BOOLEAN).withExample("false"));
        return thingifier;
    }
}
