package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
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

        ThingReadRequestMapping mapping = map(thingifier, "task", params());

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getQuery() instanceof ReadCollectionQuery);
        Assertions.assertEquals("task", mapping.getQuery().getEntityName());
    }

    @Test
    public void mapsInstanceRoute() {
        Thingifier thingifier = taskProjectThingifier();

        ThingReadRequestMapping mapping = map(thingifier, "task/abc", params());

        Assertions.assertFalse(mapping.isError());
        ReadInstanceQuery query = (ReadInstanceQuery) mapping.getQuery();
        Assertions.assertEquals("abc", query.getIdentifier());
    }

    @Test
    public void mapsRelationshipRoute() {
        Thingifier thingifier = taskProjectThingifier();

        ThingReadRequestMapping mapping = map(thingifier, "project/p1/tasks", params());

        Assertions.assertFalse(mapping.isError());
        ReadRelationshipQuery query = (ReadRelationshipQuery) mapping.getQuery();
        Assertions.assertEquals("p1", query.getIdentifier());
        Assertions.assertEquals("tasks", query.getRelationshipName());
    }

    @Test
    public void mapsPluralEntityNames() {
        Thingifier thingifier = taskProjectThingifier();

        ThingReadRequestMapping mapping = map(thingifier, "tasks", params());

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getQuery() instanceof ReadCollectionQuery);
        Assertions.assertEquals("task", mapping.getQuery().getEntityName());
    }

    @Test
    public void rejectsInvalidRoot() {
        ThingReadRequestMapping mapping = map(taskProjectThingifier(), "missing", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getError().statusCode());
        Assertions.assertEquals(
                List.of("Could not find an instance with missing"), mapping.getError().messages());
    }

    @Test
    public void rejectsRelationshipNameInIdentifierPosition() {
        ThingReadRequestMapping mapping = map(taskProjectThingifier(), "project/tasks", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getError().statusCode());
    }

    @Test
    public void rejectsEntityNameInIdentifierPosition() {
        ThingReadRequestMapping mapping = map(taskProjectThingifier(), "project/task", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getError().statusCode());
    }

    @Test
    public void rejectsEmptyRoute() {
        ThingReadRequestMapping mapping = map(taskProjectThingifier(), "", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getError().statusCode());
    }

    @Test
    public void rejectsTooDeepRoute() {
        ThingReadRequestMapping mapping =
                map(taskProjectThingifier(), "project/p1/tasks/t1", params());

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getError().statusCode());
    }

    @Test
    public void normalizesLeadingAndTrailingSlashes() {
        Thingifier thingifier = taskProjectThingifier();

        ThingReadRequestMapping mapping = map(thingifier, "/projects/", params());

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getQuery() instanceof ReadCollectionQuery);
        Assertions.assertEquals("project", mapping.getQuery().getEntityName());
    }

    @Test
    public void preservesQueryParamsOnMappedQuery() {
        Thingifier thingifier = taskProjectThingifier();
        QueryFilterParams params = new QueryFilterParams();
        params.put("title", "=Task");

        ThingReadRequestMapping mapping = map(thingifier, "tasks", params);

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
        return new ThingReadRequestMapper(new ThingifierSchemaCatalog(thingifier));
    }

    private ThingReadRequestMapping map(
            final Thingifier thingifier, final String url, final QueryFilterParams queryParams) {
        ThingifierSchemaCatalog schema = new ThingifierSchemaCatalog(thingifier);
        return mapperFor(thingifier).map(new ThingRouteMapper(schema).map(url), queryParams);
    }

    private QueryFilterParams params() {
        return new QueryFilterParams();
    }
}
