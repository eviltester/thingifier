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
    public void entityInstanceWritesDocumentValidationAndConflictStatuses() {
        ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(model()).generate("");

        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.POST, "todos/:id"))
                        .containsAll(Set.of(200, 404, 422, 409)));
        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.PUT, "todos/:id"))
                        .containsAll(Set.of(200, 404, 422, 409)));
    }

    @Test
    public void relationshipWritesDocumentValidationAndConflictStatuses() {
        ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(model()).generate("");

        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.POST, "projects/:id/tasks"))
                        .containsAll(Set.of(201, 400, 404, 422, 409)));
        Assertions.assertTrue(
                statuses(route(definition, RoutingVerb.DELETE, "projects/:id/tasks/:id"))
                        .containsAll(Set.of(200, 400, 404, 422, 409)));
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
                .whenReversed(Cardinality.ONE_TO_MANY(), "tasksof");

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
}
