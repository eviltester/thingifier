package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingRepositoryProvider;

public class RelationshipApiSqliteRepositoryTest {

    @Test
    public void getRelationshipPathsUseRepositoryWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager =
                new Thingifier(new EntityRelModel(SqliteThingRepositoryProvider.inMemory()))) {
            EntityDefinition todo = todoManager.defineThing("todo", "todos");
            todo.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
            todo.addField(Field.is("title", FieldType.STRING));

            EntityDefinition project = todoManager.defineThing("project", "projects");
            project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
            project.addField(Field.is("title", FieldType.STRING));

            todoManager
                    .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                    .whenReversed(Cardinality.ONE_TO_MANY(), "task-of");

            ThingRepository repository =
                    todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

            EntityInstance task =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(todo)
                                    .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(project)
                                    .withField("title", "SQLite relationship project"));

            repository.connectRelationship(projectInstance, "tasks", task);

            ApiResponse tasksResponse =
                    todoManager
                            .api()
                            .get(
                                    String.format(
                                            "project/%s/tasks",
                                            projectInstance.getPrimaryKeyValue()),
                                    new QueryFilterParams(),
                                    new HttpHeadersBlock());

            Assertions.assertEquals(200, tasksResponse.getStatusCode());
            Assertions.assertTrue(tasksResponse.isCollection());
            Assertions.assertEquals(1, tasksResponse.getReturnedInstanceCollection().size());
            Assertions.assertEquals(
                    task.getPrimaryKeyValue(),
                    tasksResponse.getReturnedInstanceCollection().get(0).getPrimaryKeyValue());

            ApiResponse projectResponse =
                    todoManager
                            .api()
                            .get(
                                    String.format("todo/%s/task-of", task.getPrimaryKeyValue()),
                                    new QueryFilterParams(),
                                    new HttpHeadersBlock());

            Assertions.assertEquals(200, projectResponse.getStatusCode());
            Assertions.assertTrue(projectResponse.isCollection());
            Assertions.assertEquals(1, projectResponse.getReturnedInstanceCollection().size());
            Assertions.assertEquals(
                    projectInstance.getPrimaryKeyValue(),
                    projectResponse.getReturnedInstanceCollection().get(0).getPrimaryKeyValue());
        }
    }

    @Test
    public void getUnsupportedRelationshipTraversalDoesNotUseCompatibilityFallback() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingRepository repository =
                    todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(todo)
                                    .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(project)
                                    .withField("title", "SQLite relationship project"));
            repository.connectRelationship(projectInstance, "tasks", task);

            ApiResponse response =
                    todoManager
                            .api()
                            .get(
                                    String.format(
                                            "project/%s/todo",
                                            projectInstance.getPrimaryKeyValue()),
                                    new QueryFilterParams(),
                                    new HttpHeadersBlock());

            Assertions.assertEquals(404, response.getStatusCode());
        }
    }

    @Test
    public void postRelationshipPathConnectsExistingItemWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingRepository repository =
                    todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(todo)
                                    .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(project)
                                    .withField("title", "SQLite relationship project"));

            Map<String, String> body = new HashMap<>();
            body.put("guid", task.getPrimaryKeyValue());

            ApiResponse response =
                    todoManager
                            .api()
                            .post(
                                    String.format(
                                            "project/%s/tasks",
                                            projectInstance.getPrimaryKeyValue()),
                                    parserFor(todoManager, body),
                                    new HttpHeadersBlock());

            Assertions.assertEquals(201, response.getStatusCode());
            Assertions.assertEquals(
                    1, repository.listRelatedInstances(projectInstance, "tasks").size());
            Assertions.assertEquals(
                    task.getPrimaryKeyValue(),
                    repository
                            .listRelatedInstances(projectInstance, "tasks")
                            .get(0)
                            .getPrimaryKeyValue());
        }
    }

    @Test
    public void
            postReverseRelationshipPathConnectsExistingItemWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingRepository repository =
                    todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(todo)
                                    .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(project)
                                    .withField("title", "SQLite relationship project"));

            Map<String, String> body = new HashMap<>();
            body.put("guid", projectInstance.getPrimaryKeyValue());

            ApiResponse response =
                    todoManager
                            .api()
                            .post(
                                    String.format("todo/%s/task-of", task.getPrimaryKeyValue()),
                                    parserFor(todoManager, body),
                                    new HttpHeadersBlock());

            Assertions.assertEquals(201, response.getStatusCode());
            Assertions.assertEquals(1, repository.listRelatedInstances(task, "task-of").size());
            Assertions.assertEquals(
                    projectInstance.getPrimaryKeyValue(),
                    repository.listRelatedInstances(task, "task-of").get(0).getPrimaryKeyValue());
        }
    }

    @Test
    public void postRelationshipPathCreatesAndConnectsNewItemWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingRepository repository =
                    todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance projectInstance =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(project)
                                    .withField("title", "SQLite relationship project"));

            Map<String, String> body = new HashMap<>();
            body.put("title", "created through relationship path");

            ApiResponse response =
                    todoManager
                            .api()
                            .post(
                                    String.format(
                                            "project/%s/tasks",
                                            projectInstance.getPrimaryKeyValue()),
                                    parserFor(todoManager, body),
                                    new HttpHeadersBlock());

            Assertions.assertEquals(201, response.getStatusCode());
            List<EntityInstance> relatedTasks =
                    repository.listRelatedInstances(projectInstance, "tasks");
            Assertions.assertEquals(1, relatedTasks.size());
            Assertions.assertEquals(
                    "created through relationship path",
                    relatedTasks.get(0).getFieldValue("title").asString());
            Assertions.assertEquals(1, repository.listInstances(todo).size());
        }
    }

    @Test
    public void deleteRelationshipPathRemovesOnlyRelationshipWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingRepository repository =
                    todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(todo)
                                    .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(project)
                                    .withField("title", "SQLite relationship project"));
            repository.connectRelationship(projectInstance, "tasks", task);

            ApiResponse response =
                    todoManager
                            .api()
                            .delete(
                                    String.format(
                                            "project/%s/tasks/%s",
                                            projectInstance.getPrimaryKeyValue(),
                                            task.getPrimaryKeyValue()),
                                    new HttpHeadersBlock());

            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertTrue(
                    repository.listRelatedInstances(projectInstance, "tasks").isEmpty());
            Assertions.assertNotNull(
                    repository.findInstanceByQueryIdentifier(todo, task.getPrimaryKeyValue()));
        }
    }

    @Test
    public void deleteMissingRelationshipPathReturns404WithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingRepository repository =
                    todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(todo)
                                    .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(project)
                                    .withField("title", "SQLite relationship project"));

            ApiResponse response =
                    todoManager
                            .api()
                            .delete(
                                    String.format(
                                            "project/%s/tasks/%s",
                                            projectInstance.getPrimaryKeyValue(),
                                            task.getPrimaryKeyValue()),
                                    new HttpHeadersBlock());

            Assertions.assertEquals(404, response.getStatusCode());
            Assertions.assertTrue(
                    repository.listRelatedInstances(projectInstance, "tasks").isEmpty());
        }
    }

    private Thingifier sqliteTodoManager() {
        Thingifier todoManager =
                new Thingifier(new EntityRelModel(SqliteThingRepositoryProvider.inMemory()));
        EntityDefinition todo = todoManager.defineThing("todo", "todos");
        todo.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        todo.addField(Field.is("title", FieldType.STRING));

        EntityDefinition project = todoManager.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        todoManager
                .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_MANY(), "task-of");
        return todoManager;
    }

    private BodyParser parserFor(final Thingifier todoManager, final Map<String, String> body) {
        HttpApiRequest request = new HttpApiRequest("/path").setBody(new Gson().toJson(body));
        return new BodyParser(request, todoManager.getThingNames());
    }
}
