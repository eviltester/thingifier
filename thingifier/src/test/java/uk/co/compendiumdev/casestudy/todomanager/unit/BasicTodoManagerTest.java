package uk.co.compendiumdev.casestudy.todomanager.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.testsupport.ThingifierRepositoryTestSupport;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import java.util.Collection;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
public class BasicTodoManagerTest {

    private Thingifier todoManager;

    @BeforeEach
    public void createDefinitions(){

        todoManager = TodoManagerModel.definedAsThingifier();

    }

    @Test
    public void todoModelDefinitionCheck(){


        EntityDefinition todo = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        Assertions.assertTrue(todo.hasFieldNameDefined("title"));
        Assertions.assertTrue(todo.hasFieldNameDefined("description"));
        Assertions.assertTrue(todo.hasFieldNameDefined("doneStatus"));

        Assertions.assertEquals("false", todo.
                                                    getField("doneStatus").
                                                    getDefaultValue().asString());

    }


    @Test
    public void relationshipDefinitionCheck(){


        EntityDefinition todo = ThingifierRepositoryTestSupport.entity(todoManager, "todo");
        EntityDefinition project = ThingifierRepositoryTestSupport.entity(todoManager, "project");

        EntityInstance paperwork = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todo)).setValue("title", "scan paperwork");
        EntityInstance filework = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todo)).setValue("title", "file paperwork");

        EntityInstance officeWork = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(project)).setValue("title", "Office Work");

        officeWork.getRelationships().connect("tasks", paperwork);
        officeWork.getRelationships().connect("tasks", filework);

        Collection<EntityInstance> relatedItems = officeWork.getRelationships().getConnectedItems("tasks");

        Assertions.assertTrue(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));


        relatedItems = officeWork.getRelationships().getConnectedItems("tasks");
        Assertions.assertTrue(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));

        todoManager.deleteThing(paperwork, EntityRelModel.DEFAULT_DATABASE_NAME);


        relatedItems = officeWork.getRelationships().getConnectedItems("tasks");
        Assertions.assertFalse(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));
    }


    @Test
    public void createAndAmendSomeTodos(){

        EntityDefinition todos = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        EntityInstance tidy = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todos)).
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        EntityInstance paperwork = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todos)).
                setValue("title","Do Paperwork").
                setValue("description", "Scan everything in, upload to document management system and file paperwork");

        Assertions.assertEquals("false", paperwork.getFieldValue("doneStatus").asString());

        System.out.println(todoManager.toString());

        tidy.setValue("doneStatus", "true");
        Assertions.assertEquals("true", tidy.getFieldValue("doneStatus").asString());
        System.out.println(todoManager.toString());

    }

    @Test
    public void createAndDeleteTodos(){

        EntityDefinition todos = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        int originalTodosCount = ThingifierRepositoryTestSupport.repository(todoManager).countInstances(todos);

        EntityInstance tidy = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todos)).
                setValue("title","Delete this todo").
                setValue("description", "I need to be deleted");

        EntityInstance foundit = ThingifierRepositoryTestSupport.repository(todoManager).findInstanceByPrimaryKey(todos, tidy.getPrimaryKeyValue());

        Assertions.assertEquals("Delete this todo", foundit.getFieldValue("title").asString());

        todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);
        Assertions.assertEquals(originalTodosCount, ThingifierRepositoryTestSupport.repository(todoManager).countInstances(todos));


        foundit = ThingifierRepositoryTestSupport.repository(todoManager).findInstanceByPrimaryKey(todos, tidy.getPrimaryKeyValue());

        Assertions.assertNull(foundit);


        try{
            todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);
            Assertions.fail("Item already deleted, exception should have been thrown");
        }catch(Exception e){

        }

    }

    @Test
    public void createAmendAndDeleteATodoWithAGivenGUID(){

        EntityDefinition todos = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        int originalTodosCount = ThingifierRepositoryTestSupport.repository(todoManager).countInstances(todos);

        String guid="6fd86e2d-7c52-4dea-85bb-34760ef66d9d";

        EntityInstance tidy = new EntityInstance(todos);
        tidy.overrideValue("guid", guid);

        tidy.setValue("title", "Delete this todo").
        setValue("description", "I need to be deleted");

        ThingifierRepositoryTestSupport.repository(todoManager).addInstance(tidy);

        EntityInstance foundit = ThingifierRepositoryTestSupport.repository(todoManager).findInstanceByFieldNameAndValue(todos, "guid", guid);

        Assertions.assertEquals("Delete this todo", foundit.getFieldValue("title").asString());

        todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);

        Assertions.assertEquals(originalTodosCount, ThingifierRepositoryTestSupport.repository(todoManager).countInstances(todos));


        foundit = ThingifierRepositoryTestSupport.repository(todoManager).findInstanceByFieldNameAndValue(todos, "guid", guid);

        Assertions.assertNull(foundit);


        try{
            todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);

            Assertions.fail("Item already deleted, exception should have been thrown");

        }catch(Exception e){

        }

    }
}
