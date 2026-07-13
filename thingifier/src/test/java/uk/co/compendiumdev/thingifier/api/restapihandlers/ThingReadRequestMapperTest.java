package uk.co.compendiumdev.thingifier.api.restapihandlers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.query.ReadCollectionQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadInstanceQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadRelationshipQuery;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public class ThingReadRequestMapperTest {

    @Test
    public void mapsCollectionRoute() {
        Thingifier thingifier = taskProjectThingifier();

        ThingReadRequestMapping mapping = mapperFor(thingifier).map("task", params());

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getQuery() instanceof ReadCollectionQuery);
        Assertions.assertEquals(
                thingifier.getDefinitionNamed("task"), mapping.getQuery().getEntity());
    }

    @Test
    public void mapsInstanceRoute() {
        Thingifier thingifier = taskProjectThingifier();

        ThingReadRequestMapping mapping = mapperFor(thingifier).map("task/abc", params());

        Assertions.assertFalse(mapping.isError());
        ReadInstanceQuery query = (ReadInstanceQuery) mapping.getQuery();
        Assertions.assertEquals("abc", query.getIdentifier());
    }

    @Test
    public void mapsRelationshipRoute() {
        Thingifier thingifier = taskProjectThingifier();

        ThingReadRequestMapping mapping = mapperFor(thingifier).map("project/p1/tasks", params());

        Assertions.assertFalse(mapping.isError());
        ReadRelationshipQuery query = (ReadRelationshipQuery) mapping.getQuery();
        Assertions.assertEquals("p1", query.getIdentifier());
        Assertions.assertEquals("tasks", query.getRelationshipName());
    }

    @Test
    public void mapsPluralEntityNames() {
        Thingifier thingifier = taskProjectThingifier();

        ThingReadRequestMapping mapping = mapperFor(thingifier).map("tasks", params());

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getQuery() instanceof ReadCollectionQuery);
        Assertions.assertEquals(
                thingifier.getDefinitionNamed("task"), mapping.getQuery().getEntity());
    }

    @Test
    public void rejectsInvalidRoot() {
        ThingReadRequestMapping mapping =
                mapperFor(taskProjectThingifier()).map("missing", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getErrorResponse().getStatusCode());
    }

    @Test
    public void rejectsRelationshipNameInIdentifierPosition() {
        ThingReadRequestMapping mapping =
                mapperFor(taskProjectThingifier()).map("project/tasks", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getErrorResponse().getStatusCode());
    }

    @Test
    public void rejectsEntityNameInIdentifierPosition() {
        ThingReadRequestMapping mapping =
                mapperFor(taskProjectThingifier()).map("project/task", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getErrorResponse().getStatusCode());
    }

    @Test
    public void rejectsEmptyRoute() {
        ThingReadRequestMapping mapping = mapperFor(taskProjectThingifier()).map("", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getErrorResponse().getStatusCode());
    }

    @Test
    public void rejectsTooDeepRoute() {
        ThingReadRequestMapping mapping =
                mapperFor(taskProjectThingifier()).map("project/p1/tasks/t1", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getErrorResponse().getStatusCode());
    }

    @Test
    public void normalizesLeadingAndTrailingSlashes() {
        Thingifier thingifier = taskProjectThingifier();

        ThingReadRequestMapping mapping = mapperFor(thingifier).map("/projects/", params());

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getQuery() instanceof ReadCollectionQuery);
        Assertions.assertEquals(
                thingifier.getDefinitionNamed("project"), mapping.getQuery().getEntity());
    }

    @Test
    public void preservesQueryParamsOnMappedQuery() {
        Thingifier thingifier = taskProjectThingifier();
        QueryFilterParams params = new QueryFilterParams();
        params.put("title", "=Task");

        ThingReadRequestMapping mapping = mapperFor(thingifier).map("tasks", params);

        Assertions.assertFalse(mapping.isError());
        Assertions.assertEquals(1, mapping.getQuery().getQueryParams().size());
    }

    private Thingifier taskProjectThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        task.addField(Field.is("title", FieldType.STRING));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_MANY(), "task-of");
        return thingifier;
    }

    private ThingReadRequestMapper mapperFor(final Thingifier thingifier) {
        return new ThingReadRequestMapper(thingifier);
    }

    private QueryFilterParams params() {
        return new QueryFilterParams();
    }
}
