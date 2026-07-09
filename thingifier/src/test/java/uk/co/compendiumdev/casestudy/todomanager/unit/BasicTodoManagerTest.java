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
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class BasicTodoManagerTest {

    private Thingifier todoManager;
    private ThingRepository repository;

    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();
        repository = todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
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
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todo).withField("title", "scan paperwork"));
        EntityInstance filework =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todo).withField("title", "file paperwork"));

        EntityInstance officeWork =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(project).withField("title", "Office Work"));

        repository.connectRelationship(officeWork, "tasks", paperwork);
        repository.connectRelationship(officeWork, "tasks", filework);

        Collection<EntityInstance> relatedItems = officeWork.getRelatedItems("tasks");

        Assertions.assertTrue(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));

        relatedItems = officeWork.getRelatedItems("tasks");
        Assertions.assertTrue(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));

        todoManager.deleteThing(paperwork, EntityRelModel.DEFAULT_DATABASE_NAME);

        relatedItems = officeWork.getRelatedItems("tasks");
        Assertions.assertFalse(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));
    }

    @Test
    public void createAndAmendSomeTodos() {

        EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        EntityInstance tidy =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todos)
                                .withField("title", "Tidy up my room")
                                .withField(
                                        "description",
                                        "I need to tidy up my room because it is a mess"));

        EntityInstance paperwork =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todos)
                                .withField("title", "Do Paperwork")
                                .withField(
                                        "description",
                                        "Scan everything in, upload to document management system and file paperwork"));

        Assertions.assertEquals("false", paperwork.getFieldValue("doneStatus").asString());

        System.out.println(todoManager.toString());

        tidy =
                repository.patchInstance(
                        tidy, EntityInstanceDraft.forEntity(todos).withField("doneStatus", "true"));
        Assertions.assertEquals("true", tidy.getFieldValue("doneStatus").asString());
        System.out.println(todoManager.toString());
    }

    @Test
    public void createAndDeleteTodos() {

        EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        int originalTodosCount = repository.countInstances(todos);

        EntityInstance tidy =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todos)
                                .withField("title", "Delete this todo")
                                .withField("description", "I need to be deleted"));

        EntityInstance foundit =
                repository.findInstanceByPrimaryKey(todos, tidy.getPrimaryKeyValue());

        Assertions.assertEquals("Delete this todo", foundit.getFieldValue("title").asString());

        todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);
        Assertions.assertEquals(originalTodosCount, repository.countInstances(todos));

        foundit = repository.findInstanceByPrimaryKey(todos, tidy.getPrimaryKeyValue());

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

        int originalTodosCount = repository.countInstances(todos);

        String guid = "6fd86e2d-7c52-4dea-85bb-34760ef66d9d";

        EntityInstance tidy =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todos)
                                .withProtectedField("guid", guid)
                                .withField("title", "Delete this todo")
                                .withField("description", "I need to be deleted"));

        EntityInstance foundit = repository.findInstanceByFieldNameAndValue(todos, "guid", guid);

        Assertions.assertEquals("Delete this todo", foundit.getFieldValue("title").asString());

        todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);

        Assertions.assertEquals(originalTodosCount, repository.countInstances(todos));

        foundit = repository.findInstanceByFieldNameAndValue(todos, "guid", guid);

        Assertions.assertNull(foundit);

        try {
            todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);

            Assertions.fail("Item already deleted, exception should have been thrown");

        } catch (Exception e) {

        }
    }
}
