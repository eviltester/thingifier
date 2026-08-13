package uk.co.compendiumdev.thingifier.swaggerizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class SwaggerizerEntityDescriptionTest {

    @Test
    void openApiSchemaUsesEntityDescriptionWhenConfigured() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.withDescription("A task item that explains generated OpenAPI schemas.");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));

        final ThingifierApiDocumentationDefn apiDefn =
                new ThingifierApiDocumentationDefn().setThingifier(thingifier);

        final String json = new Swaggerizer(apiDefn).asJson();

        Assertions.assertTrue(
                json.contains(
                        "\"description\" : \"A task item that explains generated OpenAPI schemas.\""));
    }

    @Test
    void relationshipRoutesExposeEditablePathParametersAndPostBodies() {
        final OpenAPI openApi = new Swaggerizer(apiDefn(relationshipModel())).swagger();

        final PathItem relationshipCollection = openApi.getPaths().get("/projects/{id}/tasks");
        Assertions.assertNotNull(relationshipCollection);
        Assertions.assertEquals(Set.of("id"), pathParameterNames(relationshipCollection));
        Assertions.assertTrue(relationshipCollection.getParameters().get(0).getRequired());
        Assertions.assertNotNull(
                relationshipCollection.getGet().getResponses().get("200").getContent());
        Assertions.assertEquals(
                "#/components/schemas/todos",
                relationshipCollection
                        .getGet()
                        .getResponses()
                        .get("200")
                        .getContent()
                        .get("application/json")
                        .getSchema()
                        .get$ref());
        Assertions.assertEquals(
                "array",
                relationshipCollection
                        .getGet()
                        .getResponses()
                        .get("200")
                        .getContent()
                        .get("application/xml")
                        .getSchema()
                        .getType());
        Assertions.assertEquals(
                "todos",
                relationshipCollection
                        .getGet()
                        .getResponses()
                        .get("200")
                        .getContent()
                        .get("application/xml")
                        .getSchema()
                        .getXml()
                        .getName());
        Assertions.assertNotNull(relationshipCollection.getPost().getRequestBody());
        Assertions.assertEquals(
                "#/components/schemas/todo",
                relationshipCollection
                        .getPost()
                        .getRequestBody()
                        .getContent()
                        .get("application/json")
                        .getSchema()
                        .get$ref());

        final PathItem singleTargetRelationship = openApi.getPaths().get("/todos/{id}/project");
        Assertions.assertNotNull(singleTargetRelationship);
        Assertions.assertEquals(
                "#/components/schemas/project",
                singleTargetRelationship
                        .getGet()
                        .getResponses()
                        .get("200")
                        .getContent()
                        .get("application/json")
                        .getSchema()
                        .get$ref());

        final PathItem relationshipInstance =
                openApi.getPaths().get("/projects/{id}/tasks/{relatedId}");
        Assertions.assertNotNull(relationshipInstance);
        Assertions.assertEquals(
                Set.of("id", "relatedId"), pathParameterNames(relationshipInstance));
        Assertions.assertTrue(
                relationshipInstance.getParameters().stream()
                        .allMatch(parameter -> Boolean.TRUE.equals(parameter.getRequired())));
    }

    @Test
    void responseContentAdvertisesAdditionalRepresentations() {
        final OpenAPI openApi = new Swaggerizer(apiDefn(relationshipModel())).swagger();

        final Content content =
                openApi.getPaths().get("/projects").getGet().getResponses().get("200").getContent();

        Assertions.assertEquals(
                "#/components/schemas/projects",
                content.get("application/json").getSchema().get$ref());
        final Schema<?> xmlCollectionSchema = content.get("application/xml").getSchema();
        Assertions.assertNull(xmlCollectionSchema.get$ref());
        Assertions.assertEquals("array", xmlCollectionSchema.getType());
        Assertions.assertEquals("projects", xmlCollectionSchema.getXml().getName());
        Assertions.assertTrue(xmlCollectionSchema.getXml().getWrapped());
        for (String mediaType :
                List.of(
                        "text/csv",
                        "text/plain",
                        "text/html",
                        "application/x-ndjson",
                        "application/jsonl",
                        "application/json-seq",
                        "text/tab-separated-values")) {
            Assertions.assertEquals("string", content.get(mediaType).getSchema().getType());
        }
    }

    @Test
    void filterableCollectionOperationsExposeSortByParameter() {
        final OpenAPI openApi = new Swaggerizer(apiDefn(relationshipModel())).swagger();

        assertSortByParameter(openApi.getPaths().get("/projects").getGet());
        assertPagingParameters(openApi.getPaths().get("/projects").getGet());
        assertSortByParameter(queryOperation(openApi.getPaths().get("/projects")));
        assertPagingParameters(queryOperation(openApi.getPaths().get("/projects")));
        assertQueryContentTypes(queryOperation(openApi.getPaths().get("/projects")));
        assertSortByParameter(openApi.getPaths().get("/projects/{id}/tasks").getGet());
        assertPagingParameters(openApi.getPaths().get("/projects/{id}/tasks").getGet());
        assertSortByParameter(queryOperation(openApi.getPaths().get("/projects/{id}/tasks")));
        assertPagingParameters(queryOperation(openApi.getPaths().get("/projects/{id}/tasks")));
        assertQueryContentTypes(queryOperation(openApi.getPaths().get("/projects/{id}/tasks")));

        Operation singleTargetRelationshipGet =
                openApi.getPaths().get("/todos/{id}/project").getGet();
        Assertions.assertTrue(
                singleTargetRelationshipGet.getParameters() == null
                        || singleTargetRelationshipGet.getParameters().stream()
                                .noneMatch(parameter -> "_sortBy".equals(parameter.getName())));
    }

    @Test
    void pagingParametersAreOmittedWhenPagingIsDisabled() {
        Thingifier thingifier = relationshipModel();
        thingifier.apiConfig().forParams().setAllowPagingThroughUrlParams(false);

        final OpenAPI openApi = new Swaggerizer(apiDefn(thingifier)).swagger();

        Operation collectionGet = openApi.getPaths().get("/projects").getGet();
        Assertions.assertTrue(
                collectionGet.getParameters().stream()
                        .noneMatch(parameter -> "_limit".equals(parameter.getName())));
        Assertions.assertTrue(
                collectionGet.getParameters().stream()
                        .noneMatch(parameter -> "_offset".equals(parameter.getName())));
    }

    private ThingifierApiDocumentationDefn apiDefn(final Thingifier thingifier) {
        return new ThingifierApiDocumentationDefn().setThingifier(thingifier);
    }

    private Thingifier relationshipModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));

        final EntityDefinition todo = thingifier.defineThing("todo", "todos");
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "project");
        return thingifier;
    }

    private Set<String> pathParameterNames(final PathItem pathItem) {
        return pathItem.getParameters().stream()
                .map(parameter -> parameter.getName())
                .collect(Collectors.toSet());
    }

    private Operation queryOperation(final PathItem pathItem) {
        return (Operation) pathItem.getExtensions().get("x-query-operation");
    }

    private void assertSortByParameter(final Operation operation) {
        Parameter sortBy =
                operation.getParameters().stream()
                        .filter(parameter -> "_sortBy".equals(parameter.getName()))
                        .findFirst()
                        .orElseThrow();

        Assertions.assertEquals("query", sortBy.getIn());
        Assertions.assertFalse(sortBy.getRequired());
        Assertions.assertEquals("+id", sortBy.getExample());
        Assertions.assertTrue(sortBy.getDescription().contains("ascending"));
        Assertions.assertTrue(sortBy.getDescription().contains("descending"));
        Assertions.assertTrue(sortBy.getDescription().contains("+field,-other"));
    }

    private void assertPagingParameters(final Operation operation) {
        Parameter limit =
                operation.getParameters().stream()
                        .filter(parameter -> "_limit".equals(parameter.getName()))
                        .findFirst()
                        .orElseThrow();
        Parameter offset =
                operation.getParameters().stream()
                        .filter(parameter -> "_offset".equals(parameter.getName()))
                        .findFirst()
                        .orElseThrow();

        Assertions.assertEquals("query", limit.getIn());
        Assertions.assertEquals("integer", limit.getSchema().getType());
        Assertions.assertFalse(limit.getRequired());
        Assertions.assertEquals(10, limit.getExample());
        Assertions.assertTrue(limit.getDescription().contains("capped at 20"));

        Assertions.assertEquals("query", offset.getIn());
        Assertions.assertEquals("integer", offset.getSchema().getType());
        Assertions.assertFalse(offset.getRequired());
        Assertions.assertEquals(0, offset.getExample());
        Assertions.assertTrue(offset.getDescription().contains("Zero-based"));
    }

    private void assertQueryContentTypes(final Operation operation) {
        Object contentTypes = operation.getExtensions().get("x-query-content-types");
        Assertions.assertTrue(contentTypes instanceof List<?>);
        Assertions.assertEquals(
                List.of(
                        "application/x-www-form-urlencoded",
                        "application/jsonpath",
                        "application/vnd.apichallenges.todo-query+json"),
                contentTypes);
    }
}
