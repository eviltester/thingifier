package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.SqliteThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class RelationshipApiSqliteRepositoryTest {

    @Test
    public void getRelationshipPathsUseRepositoryWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = new Thingifier(
                new EntityRelModel(SqliteThingRepositoryProvider.inMemory()))) {
            EntityDefinition todo = todoManager.defineThing("todo", "todos");
            todo.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
            todo.addField(Field.is("title", FieldType.STRING));

            EntityDefinition project = todoManager.defineThing("project", "projects");
            project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
            project.addField(Field.is("title", FieldType.STRING));

            todoManager.defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY()).
                    whenReversed(Cardinality.ONE_TO_MANY(), "task-of");

            ThingRepository repository =
                    todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

            EntityInstance task = repository.addInstance(new EntityInstance(todo).
                    setValue("title", "SQLite relationship task"));
            EntityInstance projectInstance = repository.addInstance(new EntityInstance(project).
                    setValue("title", "SQLite relationship project"));

            repository.connectRelationship(projectInstance, "tasks", task);

            ApiResponse tasksResponse = todoManager.api().get(
                    String.format("project/%s/tasks", projectInstance.getPrimaryKeyValue()),
                    new QueryFilterParams(),
                    new HttpHeadersBlock());

            Assertions.assertEquals(200, tasksResponse.getStatusCode());
            Assertions.assertTrue(tasksResponse.isCollection());
            Assertions.assertEquals(1, tasksResponse.getReturnedInstanceCollection().size());
            Assertions.assertEquals(task.getPrimaryKeyValue(),
                    tasksResponse.getReturnedInstanceCollection().get(0).getPrimaryKeyValue());
            Assertions.assertFalse(repository.hasLoadedCompatibilitySnapshot());

            ApiResponse projectResponse = todoManager.api().get(
                    String.format("todo/%s/task-of", task.getPrimaryKeyValue()),
                    new QueryFilterParams(),
                    new HttpHeadersBlock());

            Assertions.assertEquals(200, projectResponse.getStatusCode());
            Assertions.assertTrue(projectResponse.isCollection());
            Assertions.assertEquals(1, projectResponse.getReturnedInstanceCollection().size());
            Assertions.assertEquals(projectInstance.getPrimaryKeyValue(),
                    projectResponse.getReturnedInstanceCollection().get(0).getPrimaryKeyValue());
            Assertions.assertFalse(repository.hasLoadedCompatibilitySnapshot());
        }
    }
}
