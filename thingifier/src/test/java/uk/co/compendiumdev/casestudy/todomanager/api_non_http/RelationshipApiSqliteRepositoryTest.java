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
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStoreProvider;

public class RelationshipApiSqliteRepositoryTest {

    @Test
    public void getRelationshipPathsUseRepositoryWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager =
                new Thingifier(new EntityRelModel(SqliteThingStoreProvider.inMemory()))) {
            EntityDefinition todo = todoManager.defineThing("todo", "todos");
            todo.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
            todo.addField(Field.is("title", FieldType.STRING));

            EntityDefinition project = todoManager.defineThing("project", "projects");
            project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
            project.addField(Field.is("title", FieldType.STRING));

            todoManager
                    .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                    .whenReversed(Cardinality.ONE_TO_MANY(), "task-of");

            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);

            EntityInstance task =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(todo)
                                            .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(project)
                                            .withField("title", "SQLite relationship project"));

            repository.relationships().connect(projectInstance, "tasks", task);

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
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(todo)
                                            .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(project)
                                            .withField("title", "SQLite relationship project"));
            repository.relationships().connect(projectInstance, "tasks", task);

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
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(todo)
                                            .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
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
                    1, repository.relationships().listRelated(projectInstance, "tasks").size());
            Assertions.assertEquals(
                    task.getPrimaryKeyValue(),
                    repository
                            .relationships()
                            .listRelated(projectInstance, "tasks")
                            .get(0)
                            .getPrimaryKeyValue());
        }
    }

    @Test
    public void
            postReverseRelationshipPathConnectsExistingItemWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(todo)
                                            .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
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
            Assertions.assertEquals(
                    1, repository.relationships().listRelated(task, "task-of").size());
            Assertions.assertEquals(
                    projectInstance.getPrimaryKeyValue(),
                    repository
                            .relationships()
                            .listRelated(task, "task-of")
                            .get(0)
                            .getPrimaryKeyValue());
        }
    }

    @Test
    public void postRelationshipPathCreatesAndConnectsNewItemWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
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
                    repository.relationships().listRelated(projectInstance, "tasks");
            Assertions.assertEquals(1, relatedTasks.size());
            Assertions.assertEquals(
                    "created through relationship path",
                    relatedTasks.get(0).getFieldValue("title").asString());
            Assertions.assertEquals(1, repository.entityQueries().list(todo).size());
        }
    }

    @Test
    public void failedRelationshipPathDoesNotDeleteExistingRelatedItem() {
        try (Thingifier todoManager = sqliteTodoManagerWithMandatoryCategory()) {
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(todo)
                                            .withField("title", "Existing invalid task"));
            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
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

            Assertions.assertEquals(422, response.getStatusCode());
            Assertions.assertNotNull(
                    repository
                            .entityQueries()
                            .findByQueryIdentifier(todo, task.getPrimaryKeyValue()));
            Assertions.assertTrue(
                    repository.relationships().listRelated(projectInstance, "tasks").isEmpty());
        }
    }

    @Test
    public void failedRootCreateDoesNotPersistEntityWhenRelationshipInvariantFails() {
        try (Thingifier todoManager = sqliteTodoManagerWithMandatoryCategory()) {
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            int todoCount = repository.entityQueries().count(todo);

            Map<String, String> body = new HashMap<>();
            body.put("title", "No category means rollback");

            ApiResponse response =
                    todoManager
                            .api()
                            .post("todo", parserFor(todoManager, body), new HttpHeadersBlock());

            Assertions.assertEquals(422, response.getStatusCode());
            Assertions.assertEquals(todoCount, repository.entityQueries().count(todo));
        }
    }

    @Test
    public void relationshipPathCreateCanSatisfyMandatoryRelationshipInSameCommand() {
        try (Thingifier todoManager = sqliteTaskProjectModelWithMandatoryTaskProject()) {
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition task = todoManager.getDefinitionNamed("task");
            EntityDefinition project = todoManager.getDefinitionNamed("project");
            int taskCount = repository.entityQueries().count(task);
            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(project)
                                            .withField("title", "Project for new task"));

            Map<String, String> body = new HashMap<>();
            body.put("title", "Task created through relationship path");

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
            Assertions.assertEquals(taskCount + 1, repository.entityQueries().count(task));
            Assertions.assertEquals(
                    1, repository.relationships().listRelated(projectInstance, "tasks").size());
            Assertions.assertTrue(
                    repository.relationships().validate(response.getReturnedInstance()).isValid());
        }
    }

    @Test
    public void failedRelationshipAmendRestoresFieldsAndRelationships() {
        try (Thingifier todoManager = sqliteTaskProjectModel()) {
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition taskDefinition = todoManager.getDefinitionNamed("task");
            EntityDefinition projectDefinition = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(taskDefinition)
                                            .withField("title", "Original title"));
            EntityInstance originalProject =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(projectDefinition)
                                            .withField("title", "Original project"));
            EntityInstance rejectedProject =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(projectDefinition)
                                            .withField("title", "Rejected project"));
            repository.relationships().connect(task, "task-of", originalProject);

            Map<String, String> body = new HashMap<>();
            body.put("title", "Should not persist");
            body.put("task-of.guid", rejectedProject.getPrimaryKeyValue());

            ApiResponse response =
                    todoManager
                            .api()
                            .post(
                                    String.format("task/%s", task.getPrimaryKeyValue()),
                                    parserFor(todoManager, body),
                                    new HttpHeadersBlock());

            EntityInstance restoredTask =
                    repository
                            .entityQueries()
                            .findByQueryIdentifier(taskDefinition, task.getPrimaryKeyValue());
            List<EntityInstance> relatedProjects =
                    repository.relationships().listRelated(restoredTask, "task-of");

            Assertions.assertEquals(422, response.getStatusCode());
            Assertions.assertEquals(
                    "Original title", restoredTask.getFieldValue("title").asString());
            Assertions.assertEquals(1, relatedProjects.size());
            Assertions.assertEquals(
                    originalProject.getPrimaryKeyValue(),
                    relatedProjects.get(0).getPrimaryKeyValue());
        }
    }

    @Test
    public void deleteRelationshipPathRemovesOnlyRelationshipWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(todo)
                                            .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(project)
                                            .withField("title", "SQLite relationship project"));
            repository.relationships().connect(projectInstance, "tasks", task);

            ApiResponse response =
                    todoManager
                            .api()
                            .delete(
                                    String.format(
                                            "project/%s/tasks/%s",
                                            projectInstance.getPrimaryKeyValue(),
                                            task.getPrimaryKeyValue()),
                                    new HttpHeadersBlock());

            Assertions.assertEquals(204, response.getStatusCode());
            Assertions.assertTrue(
                    repository.relationships().listRelated(projectInstance, "tasks").isEmpty());
            Assertions.assertNotNull(
                    repository
                            .entityQueries()
                            .findByQueryIdentifier(todo, task.getPrimaryKeyValue()));
        }
    }

    @Test
    public void deleteMissingRelationshipPathReturns404WithoutLoadingCompatibilitySnapshot() {
        try (Thingifier todoManager = sqliteTodoManager()) {
            ThingStore repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = todoManager.getDefinitionNamed("todo");
            EntityDefinition project = todoManager.getDefinitionNamed("project");

            EntityInstance task =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(todo)
                                            .withField("title", "SQLite relationship task"));
            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
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
                    repository.relationships().listRelated(projectInstance, "tasks").isEmpty());
        }
    }

    private Thingifier sqliteTodoManagerWithMandatoryCategory() {
        Thingifier todoManager = sqliteTodoManager();
        EntityDefinition todo = todoManager.getDefinitionNamed("todo");
        EntityDefinition category = todoManager.defineThing("category", "categories");
        category.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        category.addField(Field.is("title", FieldType.STRING));
        todoManager
                .defineRelationship(todo, category, "category", Cardinality.ONE_TO_ONE())
                .getFromRelationship()
                .setOptionality(Optionality.MANDATORY_RELATIONSHIP);
        return todoManager;
    }

    private Thingifier sqliteTaskProjectModel() {
        Thingifier todoManager =
                new Thingifier(new EntityRelModel(SqliteThingStoreProvider.inMemory()));
        EntityDefinition task = todoManager.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        task.addField(Field.is("title", FieldType.STRING));

        EntityDefinition project = todoManager.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        todoManager
                .defineRelationship(task, project, "task-of", Cardinality.ONE_TO_ONE())
                .whenReversed(Cardinality.ONE_TO_MANY(), "tasks");
        return todoManager;
    }

    private Thingifier sqliteTaskProjectModelWithMandatoryTaskProject() {
        Thingifier todoManager =
                new Thingifier(new EntityRelModel(SqliteThingStoreProvider.inMemory()));
        EntityDefinition task = todoManager.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        task.addField(Field.is("title", FieldType.STRING));

        EntityDefinition project = todoManager.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        RelationshipDefinition taskProject =
                todoManager.defineRelationship(task, project, "task-of", Cardinality.ONE_TO_ONE());
        taskProject.getFromRelationship().setOptionality(Optionality.MANDATORY_RELATIONSHIP);
        taskProject.whenReversed(Cardinality.ONE_TO_MANY(), "tasks");
        return todoManager;
    }

    private Thingifier sqliteTodoManager() {
        Thingifier todoManager =
                new Thingifier(new EntityRelModel(SqliteThingStoreProvider.inMemory()));
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
