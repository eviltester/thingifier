package uk.co.compendiumdev.casestudy.todomanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import java.util.Collection;

public class BasicTodoManagerTest {

    private Thingifier todoManager;

    @BeforeEach
    public void createDefinitions(){

        todoManager = TodoManagerModel.definedAsThingifier();

    }

    @Test
    public void todoModelDefinitionCheck(){


        Thing todo = todoManager.getThingNamed("todo");

        Assertions.assertTrue(todo.definition().hasFieldNameDefined("title"));
        Assertions.assertTrue(todo.definition().hasFieldNameDefined("description"));
        Assertions.assertTrue(todo.definition().hasFieldNameDefined("doneStatus"));

        Assertions.assertEquals("false", todo.definition().getField("doneStatus").getDefaultValue());

    }


    @Test
    public void relationshipDefinitionCheck(){


        Thing todo = todoManager.getThingNamed("todo");
        Thing project = todoManager.getThingNamed("project");

        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);
        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        ThingInstance officeWork = project.createInstance().setValue("title", "Office Work");
        project.addInstance(officeWork);

        officeWork.connects("tasks", paperwork);
        officeWork.connects("tasks", filework);

        Collection<ThingInstance> relatedItems = officeWork.connectedItems("tasks");

        Assertions.assertTrue(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));


        relatedItems = officeWork.connectedItems("tasks");
        Assertions.assertTrue(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));

        todoManager.deleteThing(paperwork);


        relatedItems = officeWork.connectedItems("tasks");
        Assertions.assertFalse(relatedItems.contains(paperwork));
        Assertions.assertTrue(relatedItems.contains(filework));
    }


    @Test
    public void createAndAmendSomeTodos(){

        Thing todos = todoManager.getThingNamed("todo");

        ThingInstance tidy = todos.createInstance().
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        todos.addInstance(tidy);

        ThingInstance paperwork = todos.createInstance().
                setValue("title","Do Paperwork").
                setValue("description", "Scan everything in, upload to document management system and file paperwork");

        todos.addInstance(paperwork);

        Assertions.assertEquals("false", paperwork.getValue("doneStatus"));

        System.out.println(todoManager.toString());

        tidy.setValue("doneStatus", "true");
        Assertions.assertEquals("true", tidy.getValue("doneStatus"));
        System.out.println(todoManager.toString());

    }

    @Test
    public void createAndDeleteTodos(){

        Thing todos = todoManager.getThingNamed("todo");

        int originalTodosCount = todos.countInstances();

        ThingInstance tidy = todos.createInstance().
                setValue("title","Delete this todo").
                setValue("description", "I need to be deleted");

        todos.addInstance(tidy);

        ThingInstance foundit = todos.findInstanceByGUID(tidy.getGUID());

        Assertions.assertEquals("Delete this todo", foundit.getValue("title"));

        todoManager.deleteThing(foundit);
        Assertions.assertEquals(originalTodosCount, todos.countInstances());


        foundit = todos.findInstanceByGUID(tidy.getGUID());

        Assertions.assertNull(foundit);


        try{
            todoManager.deleteThing(foundit);
            Assertions.fail("Item already deleted, exception should have been thrown");
        }catch(Exception e){

        }

    }

    @Test
    public void createAmendAndDeleteATodoWithAGivenGUID(){

        Thing todos = todoManager.getThingNamed("todo");

        int originalTodosCount = todos.countInstances();

        String guid="1234-12334-1234-1234";

        ThingInstance tidy = todos.createInstance(guid).setValue("title", "Delete this todo").
                setValue("description", "I need to be deleted");

        todos.addInstance(tidy);

        ThingInstance foundit = todos.findInstanceByGUID(guid);

        Assertions.assertEquals("Delete this todo", foundit.getValue("title"));

        todoManager.deleteThing(foundit);

        Assertions.assertEquals(originalTodosCount, todos.countInstances());


        foundit = todos.findInstanceByGUID(guid);

        Assertions.assertNull(foundit);


        try{
            todoManager.deleteThing(foundit);

            Assertions.fail("Item already deleted, exception should have been thrown");

        }catch(Exception e){

        }

    }
}
