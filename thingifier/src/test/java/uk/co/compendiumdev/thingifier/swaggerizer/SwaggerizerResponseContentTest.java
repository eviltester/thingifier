package uk.co.compendiumdev.thingifier.swaggerizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class SwaggerizerResponseContentTest {

    @Test
    void openApi30OmitsContentForSuppressedErrorResponseBodies() {
        final Thingifier thingifier = secretModel();
        secretNoteRoute(thingifier).onError(401).suppressBody();

        final JsonObject document =
                JsonParser.parseString(
                                new Swaggerizer(apiDefn(thingifier))
                                        .asJson(OpenApiSpecificationVersion.OPENAPI_3_0))
                        .getAsJsonObject();
        final JsonObject response =
                document.getAsJsonObject("paths")
                        .getAsJsonObject("/secret/note")
                        .getAsJsonObject("get")
                        .getAsJsonObject("responses")
                        .getAsJsonObject("401");

        Assertions.assertFalse(response.has("content"));
    }

    @Test
    void headOperationsDoNotDocumentResponseBodies() {
        final Thingifier thingifier = secretModel();
        thingifier.apiSpec().security().apiKey("authToken", "X-AUTH-TOKEN");
        secretNoteRoute(thingifier).secureWithApiKey("authToken").onError(401).suppressBody();

        final OpenAPI openApi = new Swaggerizer(apiDefn(thingifier)).swagger();
        final Operation head = openApi.getPaths().get("/secret/note").getHead();

        Assertions.assertTrue(head.getResponses().containsKey("200"));
        Assertions.assertTrue(head.getResponses().containsKey("401"));
        for (ApiResponse response : head.getResponses().values()) {
            Assertions.assertNull(response.getContent());
        }
    }

    @Test
    void customRouteQueryParamsAreDocumentedAsQueryParameters() {
        final OpenAPI openApi = new Swaggerizer(apiDefnWithExportRoute()).swagger();
        final Operation get = openApi.getPaths().get("/todos/export").getGet();
        final Parameter format = get.getParameters().get(0);

        Assertions.assertEquals("format", format.getName());
        Assertions.assertEquals("query", format.getIn());
        Assertions.assertFalse(Boolean.TRUE.equals(format.getRequired()));
        Assertions.assertEquals("string", format.getSchema().getType());
        Assertions.assertEquals("csv", format.getExample());
    }

    @Test
    void customRouteResponseContentIsDocumentedByStatusAndMediaType() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        final ObjectSchema statusSchema = new ObjectSchema();
        statusSchema.addProperty("status", new StringSchema());
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET, "/status", RoutingStatus.returnedFromCall(), null)
                        .addDocumentation("status")
                        .addPossibleStatuses(200, 400)
                        .responseSchema(200, "application/json", statusSchema)
                        .responseExample(200, "application/json", Map.of("status", "ok"))
                        .responseStringSchema(200, "text/plain")
                        .responseSchemaRef(
                                400, "application/json", "#/components/schemas/Problem"));

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();
        final Operation get = openApi.getPaths().get("/status").getGet();

        Assertions.assertTrue(
                get.getResponses()
                        .get("200")
                        .getContent()
                        .get("application/json")
                        .getSchema()
                        .getProperties()
                        .containsKey("status"));
        Assertions.assertEquals(
                Map.of("status", "ok"),
                get.getResponses().get("200").getContent().get("application/json").getExample());
        Assertions.assertEquals(
                "string",
                get.getResponses().get("200").getContent().get("text/plain").getSchema().getType());
        Assertions.assertEquals(
                "#/components/schemas/Problem",
                get.getResponses()
                        .get("400")
                        .getContent()
                        .get("application/json")
                        .getSchema()
                        .get$ref());
    }

    @Test
    void openApi30StrongSchemaSerializesCustomRouteQueryParamTypes() {
        final JsonObject document =
                JsonParser.parseString(
                                new Swaggerizer(apiDefnWithExportRoute())
                                        .asJson(
                                                strongConfig(
                                                        OpenApiSpecificationVersion.OPENAPI_3_0)))
                        .getAsJsonObject();
        final JsonObject parameter =
                document.getAsJsonObject("paths")
                        .getAsJsonObject("/todos/export")
                        .getAsJsonObject("get")
                        .getAsJsonArray("parameters")
                        .get(0)
                        .getAsJsonObject();

        Assertions.assertEquals("query", parameter.get("in").getAsString());
        Assertions.assertEquals("format", parameter.get("name").getAsString());
        Assertions.assertEquals(
                "string", parameter.getAsJsonObject("schema").get("type").getAsString());
    }

    private ThingifierApiDocumentationDefn apiDefnWithExportRoute() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET,
                                "/todos/export",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation("export todos")
                        .addPossibleStatuses(200)
                        .addRequestQueryParam(
                                Field.is("format", FieldType.STRING)
                                        .withExample("csv")
                                        .withDescription("Export format")));
        return apiDefn;
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

    private ThingifierApiRouteRule secretNoteRoute(final Thingifier thingifier) {
        return thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note");
    }

    private Thingifier secretModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition note = thingifier.defineThing("secretnote", "secretnotes");
        note.addAsPrimaryKeyField(Field.is("id", FieldType.STRING).withExample("note"));
        note.addField(Field.is("text", FieldType.STRING).withExample("visible text"));
        return thingifier;
    }
}
