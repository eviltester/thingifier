package uk.co.compendiumdev.thingifier.api.docgen;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public class ApiRoutingDefinitionDocGeneratorTest {

    @Test
    public void entityCollectionPostDocumentsValidationAndConflictStatuses() {
        ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(model()).generate("");

        RoutingDefinition route = route(definition, RoutingVerb.POST, "todos");

        Assertions.assertTrue(statuses(route).containsAll(Set.of(201, 400, 422, 409)));
    }

    @Test
    public void entityCollectionQueryDocumentsQueryStatusesAndDiscovery() {
        ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(model()).generate("");

        RoutingDefinition query = route(definition, RoutingVerb.QUERY, "todos");
        RoutingDefinition options = route(definition, RoutingVerb.OPTIONS, "todos");

        Assertions.assertTrue(statuses(query).containsAll(Set.of(200, 400, 413, 415)));
        Assertions.assertEquals("OPTIONS, GET, HEAD, POST, QUERY", options.headerValue());
    }

    @Test
    public void entityInstanceWritesDocumentValidationAndConflictStatuses() {
        ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(model()).generate("");

        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.POST, "todos/:id"))
                        .containsAll(Set.of(200, 404, 422, 409)));
        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.PUT, "todos/:id"))
                        .containsAll(Set.of(200, 404, 422, 409)));
        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.DELETE, "todos/:id"))
                        .containsAll(Set.of(204, 404)));
        Assertions.assertEquals(
                405, route(definition, RoutingVerb.QUERY, "todos/:id").status().value());
    }

    @Test
    public void routeDefinitionWithoutHeaderReturnsEmptyHeaderDetails() {
        RoutingDefinition route =
                new RoutingDefinition(
                        RoutingVerb.DELETE, "todos", RoutingStatus.returnValue(405), null);

        Assertions.assertEquals("", route.header());
        Assertions.assertEquals("", route.headerValue());
    }

    @Test
    public void relationshipWritesDocumentValidationAndConflictStatuses() {
        ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(model()).generate("");

        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.POST, "projects/:id/tasks"))
                        .containsAll(Set.of(201, 400, 404, 422, 409)));
        Assertions.assertEquals(
                Set.of("id"),
                parameterNames(route(definition, RoutingVerb.GET, "projects/:id/tasks")));
        Assertions.assertTrue(
                route(definition, RoutingVerb.GET, "projects/:id/tasks").isFilterable());
        Assertions.assertTrue(
                route(definition, RoutingVerb.GET, "projects/:id/tasks").hasReturnPayloadFor(200));
        Assertions.assertEquals(
                "todos",
                route(definition, RoutingVerb.GET, "projects/:id/tasks").getReturnPayloadFor(200));
        Assertions.assertEquals(
                "todo",
                route(definition, RoutingVerb.POST, "projects/:id/tasks").getRequestPayload());
        Assertions.assertEquals(
                "project",
                route(definition, RoutingVerb.GET, "todos/:id/tasksof").getReturnPayloadFor(200));
        Assertions.assertFalse(
                route(definition, RoutingVerb.GET, "todos/:id/tasksof").isFilterable());
        Assertions.assertEquals(
                "projects",
                route(definition, RoutingVerb.QUERY, "todos/:id/tasksof").getReturnPayloadFor(200));
        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.QUERY, "projects/:id/tasks"))
                        .containsAll(Set.of(200, 400, 413, 415)));
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY",
                route(definition, RoutingVerb.OPTIONS, "projects/:id/tasks").headerValue());
        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.DELETE, "projects/:id/tasks/:relatedId"))
                        .containsAll(Set.of(204, 400, 404, 422, 409)));
        Assertions.assertEquals(
                Set.of("id", "relatedId"),
                parameterNames(
                        route(definition, RoutingVerb.DELETE, "projects/:id/tasks/:relatedId")));
        Assertions.assertEquals(
                405,
                route(definition, RoutingVerb.QUERY, "projects/:id/tasks/:relatedId")
                        .status()
                        .value());
    }

    @Test
    public void relationshipRoutesUseConfiguredPathPrefix() {
        ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(model()).generate("/api");

        Assertions.assertNotNull(route(definition, RoutingVerb.GET, "api/projects/:id/tasks"));
        Assertions.assertNotNull(
                route(definition, RoutingVerb.DELETE, "api/projects/:id/tasks/:relatedId"));
    }

    private Thingifier model() {
        Thingifier thingifier = new Thingifier();

        EntityDefinition project = thingifier.defineThing("project", "projects", 1);
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));

        EntityDefinition todo = thingifier.defineThing("todo", "todos", 1);
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addField(Field.is("title", FieldType.STRING).makeMandatory());

        thingifier
                .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "tasksof");

        return thingifier;
    }

    private RoutingDefinition route(
            final ApiRoutingDefinition definition, final RoutingVerb verb, final String url) {
        return definition.definitions().stream()
                .filter(route -> route.verb() == verb)
                .filter(route -> route.url().equals(url))
                .findFirst()
                .orElseThrow();
    }

    private Set<Integer> statuses(final RoutingDefinition route) {
        return route.getPossibleStatusReponses().stream()
                .map(RoutingStatus::value)
                .collect(Collectors.toSet());
    }

    private Set<String> parameterNames(final RoutingDefinition route) {
        return route.getRequestUrlParameters().stream()
                .map(RoutingDefinition.RequestUrlParameter::name)
                .collect(Collectors.toSet());
    }
}
