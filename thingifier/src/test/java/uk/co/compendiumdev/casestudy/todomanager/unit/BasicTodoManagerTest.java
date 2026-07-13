package uk.co.compendiumdev.casestudy.todomanager.unit;

import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class BasicTodoManagerTest {

    private Thingifier todoManager;
    private ThingStore repository;

    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();
        repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    @Test
    public void todoModelDefinitionCheck() {

        EntityDefinition todo =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        Assertions.assertTrue(todo.hasFieldNameDefined("title"));
        Assertions.assertTrue(todo.hasFieldNameDefined("description"));
        Assertions.assertTrue(todo.hasFieldNameDefined("doneStatus"));

        Assertions.assertEquals("false", todo.getField("doneStatus").getDefaultValue().asString());
    }

    @Test
    public void relationshipDefinitionCheck() {

        EntityDefinition todo =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");
        EntityDefinition project =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("project");

        EntityInstance paperwork =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "scan paperwork"));
        EntityInstance filework =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "file paperwork"));

        EntityInstance officeWork =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Office Work"));

        repository.relationships().connect(officeWork, "tasks", paperwork);
        repository.relationships().connect(officeWork, "tasks", filework);

        Collection<EntityInstance> relatedItems =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(officeWork, "tasks");

        Assertions.assertTrue(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));

        relatedItems =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(officeWork, "tasks");
        Assertions.assertTrue(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));

        todoManager.deleteThing(paperwork, EntityRelModel.DEFAULT_DATABASE_NAME);

        relatedItems =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(officeWork, "tasks");
        Assertions.assertFalse(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));
    }

    @Test
    public void createAndAmendSomeTodos() {

        EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        EntityInstance tidy =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todos)
                                        .withField("title", "Tidy up my room")
                                        .withField(
                                                "description",
                                                "I need to tidy up my room because it is a mess"));

        EntityInstance paperwork =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todos)
                                        .withField("title", "Do Paperwork")
                                        .withField(
                                                "description",
                                                "Scan everything in, upload to document management system and file paperwork"));

        Assertions.assertEquals("false", paperwork.getFieldValue("doneStatus").asString());

        System.out.println(todoManager.toString());

        tidy =
                repository
                        .entities()
                        .patch(
                                tidy,
                                EntityInstanceDraft.forEntity(todos)
                                        .withField("doneStatus", "true"));
        Assertions.assertEquals("true", tidy.getFieldValue("doneStatus").asString());
        System.out.println(todoManager.toString());
    }

    @Test
    public void createAndDeleteTodos() {

        EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        int originalTodosCount = repository.entityQueries().count(todos);

        EntityInstance tidy =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todos)
                                        .withField("title", "Delete this todo")
                                        .withField("description", "I need to be deleted"));

        EntityInstance foundit =
                repository.entityQueries().findByPrimaryKey(todos, tidy.getPrimaryKeyValue());

        Assertions.assertEquals("Delete this todo", foundit.getFieldValue("title").asString());

        todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);
        Assertions.assertEquals(originalTodosCount, repository.entityQueries().count(todos));

        foundit = repository.entityQueries().findByPrimaryKey(todos, tidy.getPrimaryKeyValue());

        Assertions.assertNull(foundit);

        try {
            todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);
            Assertions.fail("Item already deleted, exception should have been thrown");
        } catch (Exception e) {

        }
    }

    @Test
    public void createAmendAndDeleteATodoWithAGivenGUID() {

        EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        int originalTodosCount = repository.entityQueries().count(todos);

        String guid = "6fd86e2d-7c52-4dea-85bb-34760ef66d9d";

        EntityInstance tidy =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todos)
                                        .withProtectedField("guid", guid)
                                        .withField("title", "Delete this todo")
                                        .withField("description", "I need to be deleted"));
        Assertions.assertEquals(guid, tidy.getPrimaryKeyValue());

        EntityInstance foundit = repository.entityQueries().findByField(todos, "guid", guid);

        Assertions.assertEquals("Delete this todo", foundit.getFieldValue("title").asString());

        todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);

        Assertions.assertEquals(originalTodosCount, repository.entityQueries().count(todos));

        foundit = repository.entityQueries().findByField(todos, "guid", guid);

        Assertions.assertNull(foundit);

        try {
            todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);

            Assertions.fail("Item already deleted, exception should have been thrown");

        } catch (Exception e) {

        }
    }
}
