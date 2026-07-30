package uk.co.compendiumdev.thingifier.swaggerizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
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
    void filterableCollectionOperationsExposeSortByParameter() {
        final OpenAPI openApi = new Swaggerizer(apiDefn(relationshipModel())).swagger();

        assertSortByParameter(openApi.getPaths().get("/projects").getGet());
        assertSortByParameter(queryOperation(openApi.getPaths().get("/projects")));
        assertSortByParameter(openApi.getPaths().get("/projects/{id}/tasks").getGet());
        assertSortByParameter(queryOperation(openApi.getPaths().get("/projects/{id}/tasks")));

        Operation singleTargetRelationshipGet =
                openApi.getPaths().get("/todos/{id}/project").getGet();
        Assertions.assertTrue(
                singleTargetRelationshipGet.getParameters() == null
                        || singleTargetRelationshipGet.getParameters().stream()
                                .noneMatch(parameter -> "_sortBy".equals(parameter.getName())));
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
}
