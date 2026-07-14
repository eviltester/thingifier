package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class ThingRouteMapperTest {

    @Test
    void mapsCollectionRoute() {
        ThingRoute route = mapper().map("/tasks/");

        Assertions.assertTrue(route instanceof CollectionRoute);
        Assertions.assertEquals("task", ((CollectionRoute) route).entity().getName());
    }

    @Test
    void mapsInstanceRoute() {
        ThingRoute route = mapper().map("tasks/123");

        Assertions.assertTrue(route instanceof InstanceRoute);
        Assertions.assertEquals("123", ((InstanceRoute) route).identifier());
    }

    @Test
    void mapsRelationshipCollectionRouteWithoutResolvingInstances() {
        ThingRoute route = mapper().map("projects/p1/tasks");

        Assertions.assertTrue(route instanceof RelationshipCollectionRoute);
        RelationshipCollectionRoute relationship = (RelationshipCollectionRoute) route;
        Assertions.assertEquals("project", relationship.parentEntity().getName());
        Assertions.assertEquals("p1", relationship.parentIdentifier());
        Assertions.assertEquals("tasks", relationship.relationshipName());
    }

    @Test
    void mapsRelationshipInstanceRouteWithoutResolvingInstances() {
        ThingRoute route = mapper().map("projects/p1/tasks/t1");

        Assertions.assertTrue(route instanceof RelationshipInstanceRoute);
        RelationshipInstanceRoute relationship = (RelationshipInstanceRoute) route;
        Assertions.assertEquals("p1", relationship.parentIdentifier());
        Assertions.assertEquals("t1", relationship.childIdentifier());
    }

    @Test
    void rejectsUnknownRoot() {
        ThingRoute route = mapper().map("widgets/1");

        Assertions.assertTrue(route instanceof UnmatchedRoute);
        Assertions.assertEquals("widgets", ((UnmatchedRoute) route).firstPart());
    }

    @Test
    void rejectsUnknownRelationshipName() {
        ThingRoute route = mapper().map("projects/p1/widgets");

        Assertions.assertTrue(route instanceof UnmatchedRoute);
    }

    private ThingRouteMapper mapper() {
        return new ThingRouteMapper(new ThingifierSchemaCatalog(thingifier()));
    }

    private Thingifier thingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        thingifier
                .defineRelationship(task, project, "task-of", Cardinality.ONE_TO_ONE())
                .whenReversed(Cardinality.ONE_TO_MANY(), "tasks");
        return thingifier;
    }
}
