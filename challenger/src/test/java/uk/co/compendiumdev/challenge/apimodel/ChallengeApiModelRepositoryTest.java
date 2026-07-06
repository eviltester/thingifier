package uk.co.compendiumdev.challenge.apimodel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.SqliteThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.SqliteThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class ChallengeApiModelRepositoryTest {

    @Test
    public void challengeApiCanUseSqliteInMemoryRepository() {
        try (SqliteThingRepositoryProvider provider = SqliteThingRepositoryProvider.inMemory()) {
            Thingifier thingifier = new ChallengeApiModel().get(provider);

            Assertions.assertTrue(
                    thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                            instanceof SqliteThingRepository);
            Assertions.assertEquals(
                    10,
                    thingifier.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME).
                            countInstances());
        }
    }

    @Test
    public void challengeApiGetUsesSqliteRepositoryReadsForSimpleTodoQueries() {
        try (SqliteThingRepositoryProvider provider = SqliteThingRepositoryProvider.inMemory()) {
            Thingifier thingifier = new ChallengeApiModel().get(provider);
            ThingRepository repository = thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = thingifier.getDefinitionNamed("todo");

            EntityInstance firstTodo = repository.findInstanceByQueryIdentifier(todo, "1");
            firstTodo.setValue("doneStatus", "true");
            repository.updateInstance(firstTodo);

            ApiResponse allTodos = thingifier.api().get(
                    "/todos", new QueryFilterParams(), new HttpHeadersBlock());
            Assertions.assertEquals(200, allTodos.getStatusCode());
            Assertions.assertEquals(10, allTodos.getReturnedInstanceCollection().size());

            ApiResponse todoById = thingifier.api().get(
                    "/todos/1", new QueryFilterParams(), new HttpHeadersBlock());
            Assertions.assertEquals(200, todoById.getStatusCode());
            Assertions.assertEquals(1, todoById.getReturnedInstanceCollection().size());
            Assertions.assertEquals(
                    "1",
                    todoById.getReturnedInstanceCollection().get(0).getPrimaryKeyValue());

            QueryFilterParams doneStatusFilter = new QueryFilterParams();
            doneStatusFilter.put("doneStatus", "false");
            ApiResponse notDoneTodos = thingifier.api().get(
                    "/todos", doneStatusFilter, new HttpHeadersBlock());
            Assertions.assertEquals(200, notDoneTodos.getStatusCode());
            Assertions.assertEquals(9, notDoneTodos.getReturnedInstanceCollection().size());

            QueryFilterParams idRangeFilter = new QueryFilterParams();
            idRangeFilter.put("id", ">=2");
            ApiResponse idRangeTodos = thingifier.api().get(
                    "/todos", idRangeFilter, new HttpHeadersBlock());
            Assertions.assertEquals(200, idRangeTodos.getStatusCode());
            Assertions.assertEquals(9, idRangeTodos.getReturnedInstanceCollection().size());

            QueryFilterParams sortByDescendingId = new QueryFilterParams();
            sortByDescendingId.put("sortBy", "-id");
            ApiResponse sortedTodos = thingifier.api().get(
                    "/todos", sortByDescendingId, new HttpHeadersBlock());
            Assertions.assertEquals(200, sortedTodos.getStatusCode());
            Assertions.assertEquals(
                    "10",
                    sortedTodos.getReturnedInstanceCollection().get(0).getPrimaryKeyValue());
        }
    }

    @Test
    public void relationshipGetRoutesStillFallBackToLegacyQueryTraversal() {
        try (SqliteThingRepositoryProvider provider = SqliteThingRepositoryProvider.inMemory()) {
            Thingifier thingifier = new Thingifier(new EntityRelModel(provider));

            EntityDefinition project = thingifier.defineThing("project", "projects");
            project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            project.addField(Field.is("title", FieldType.STRING));

            EntityDefinition task = thingifier.defineThing("task", "tasks");
            task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            task.addField(Field.is("title", FieldType.STRING));

            thingifier.defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY()).
                    whenReversed(Cardinality.ONE_TO_ONE(), "task-of");

            ThingRepository repository = thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityInstance projectInstance = new EntityInstance(project).setValue("title", "Office");
            EntityInstance taskInstance = new EntityInstance(task).setValue("title", "File paperwork");
            repository.addInstance(projectInstance);
            repository.addInstance(taskInstance);
            repository.connectRelationship(projectInstance, "tasks", taskInstance);

            ApiResponse response = thingifier.api().get(
                    "/project/1/tasks", new QueryFilterParams(), new HttpHeadersBlock());

            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertEquals(1, response.getReturnedInstanceCollection().size());
            Assertions.assertEquals(
                    "File paperwork",
                    response.getReturnedInstanceCollection().get(0).getFieldValue("title").asString());
        }
    }
}
