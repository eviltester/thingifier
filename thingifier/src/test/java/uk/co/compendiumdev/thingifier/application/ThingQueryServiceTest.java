package uk.co.compendiumdev.thingifier.application;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.application.query.ReadCollectionQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadInstanceQuery;
import uk.co.compendiumdev.thingifier.application.query.ReadRelationshipQuery;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStore;

public class ThingQueryServiceTest {

    @Test
    public void collectionReadReturnsAllInstances() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance first = createTask(storeFor(thingifier), task, "First", "3");
        EntityInstance second = createTask(storeFor(thingifier), task, "Second", "1");

        RepositoryQueryResult result =
                queryServiceFor(thingifier)
                        .execute(
                                new ReadCollectionQuery(task.getName(), new QueryFilterParams()),
                                storeFor(thingifier));

        Assertions.assertTrue(result.isResultACollection());
        Assertions.assertEquals(2, result.getListEntityInstances().size());
        Assertions.assertTrue(result.getListEntityInstances().contains(first));
        Assertions.assertTrue(result.getListEntityInstances().contains(second));
    }

    @Test
    public void instanceReadReturnsMatchingInstance() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance instance = createTask(storeFor(thingifier), task, "Only", "1");

        RepositoryQueryResult result =
                queryServiceFor(thingifier)
                        .execute(
                                new ReadInstanceQuery(
                                        task.getName(),
                                        instance.getPrimaryKeyValue(),
                                        new QueryFilterParams()),
                                storeFor(thingifier));

        Assertions.assertTrue(result.wasQueryIntendedToMatchAnInstance());
        Assertions.assertTrue(result.lastMatchWasInstance());
        Assertions.assertEquals(instance, result.getLastInstance());
    }

    @Test
    public void missingInstanceReadReportsNoMatch() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");

        RepositoryQueryResult result =
                queryServiceFor(thingifier)
                        .execute(
                                new ReadInstanceQuery(
                                        task.getName(), "missing", new QueryFilterParams()),
                                storeFor(thingifier));

        Assertions.assertTrue(result.wasQueryIntendedToMatchAnInstance());
        Assertions.assertTrue(result.lastMatchWasNothing());
        Assertions.assertTrue(result.getListEntityInstances().isEmpty());
    }

    @Test
    public void relationshipReadReturnsRelatedInstances() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance taskInstance = createTask(store, task, "Related", "1");
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));
        store.relationships().connect(projectInstance, "tasks", taskInstance);

        RepositoryQueryResult result =
                queryServiceFor(thingifier)
                        .execute(
                                new ReadRelationshipQuery(
                                        project.getName(),
                                        projectInstance.getPrimaryKeyValue(),
                                        "tasks",
                                        new QueryFilterParams()),
                                store);

        Assertions.assertTrue(result.isResultACollection());
        Assertions.assertEquals(List.of(taskInstance), result.getListEntityInstances());
        Assertions.assertEquals(task, result.resultContainsDefn());
    }

    @Test
    public void missingRelationshipSourceReportsNoMatch() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition project = thingifier.getDefinitionNamed("project");

        RepositoryQueryResult result =
                queryServiceFor(thingifier)
                        .execute(
                                new ReadRelationshipQuery(
                                        project.getName(),
                                        "missing",
                                        "tasks",
                                        new QueryFilterParams()),
                                storeFor(thingifier));

        Assertions.assertTrue(result.lastMatchWasNothing());
        Assertions.assertTrue(result.getListEntityInstances().isEmpty());
    }

    @Test
    public void collectionReadAppliesFilters() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        createTask(storeFor(thingifier), task, "Keep", "1");
        createTask(storeFor(thingifier), task, "Drop", "2");
        QueryFilterParams params = new QueryFilterParams();
        params.put("title", "=Keep");

        RepositoryQueryResult result =
                queryServiceFor(thingifier)
                        .execute(
                                new ReadCollectionQuery(task.getName(), params),
                                storeFor(thingifier));

        Assertions.assertEquals(1, result.getListEntityInstances().size());
        Assertions.assertEquals(
                "Keep", result.getListEntityInstances().get(0).getFieldValue("title").asString());
    }

    @Test
    public void collectionReadAppliesSorting() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        createTask(storeFor(thingifier), task, "Last", "3");
        createTask(storeFor(thingifier), task, "First", "1");
        QueryFilterParams params = new QueryFilterParams();
        params.put("_sortBy", "+priority");

        RepositoryQueryResult result =
                queryServiceFor(thingifier)
                        .execute(
                                new ReadCollectionQuery(task.getName(), params),
                                storeFor(thingifier));

        Assertions.assertEquals(
                "First", result.getListEntityInstances().get(0).getFieldValue("title").asString());
        Assertions.assertEquals(
                "Last", result.getListEntityInstances().get(1).getFieldValue("title").asString());
    }

    @Test
    public void sqliteBackedStoreExecutesReadQuery() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");

        try (ThingStore store = SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            store.administration().initializeFrom(thingifier.getERmodel().getSchema());
            EntityInstance instance = createTask(store, task, "SQLite", "1");

            RepositoryQueryResult result =
                    queryServiceFor(thingifier)
                            .execute(
                                    new ReadCollectionQuery(
                                            task.getName(), new QueryFilterParams()),
                                    store);

            Assertions.assertEquals(List.of(instance), result.getListEntityInstances());
        }
    }

    private Thingifier taskProjectThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        task.addField(Field.is("title", FieldType.STRING));
        task.addField(Field.is("priority", FieldType.INTEGER));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_MANY(), "task-of");
        return thingifier;
    }

    private EntityInstance createTask(
            final ThingStore store,
            final EntityDefinition task,
            final String title,
            final String priority) {
        return store.entities()
                .create(
                        EntityInstanceDraft.forEntity(task)
                                .withField("title", title)
                                .withField("priority", priority));
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private ThingQueryService queryServiceFor(final Thingifier thingifier) {
        return new ThingQueryService(new ThingifierSchemaCatalog(thingifier));
    }
}
