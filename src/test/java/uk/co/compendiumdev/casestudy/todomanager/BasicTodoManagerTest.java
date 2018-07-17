package uk.co.compendiumdev.casestudy.todomanager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.instances.RelationshipInstance;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BasicTodoManagerTest {

    private Thingifier todoManager;

    @Before
    public void createDefinitions(){

        todoManager = TodoManagerModel.definedAsThingifier();

    }

    @Test
    public void todoModelDefinitionCheck(){


        Thing todo = todoManager.getThingNamed("todo");

        Assert.assertTrue(todo.definition().hasFieldNameDefined("title"));
        Assert.assertTrue(todo.definition().hasFieldNameDefined("description"));
        Assert.assertTrue(todo.definition().hasFieldNameDefined("doneStatus"));

        Assert.assertEquals("FALSE", todo.definition().getField("doneStatus").getDefaultValue());

    }


    @Test
    public void RelationshipDefinitionCheck(){


        Thing todo = todoManager.getThingNamed("todo");
        Thing project = todoManager.getThingNamed("project");
        Thing category = todoManager.getThingNamed("category");

        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);
        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        ThingInstance officeWork = project.createInstance().setValue("title", "Office Work");
        project.addInstance(officeWork);

        officeWork.connects("tasks", paperwork);
        officeWork.connects("tasks", filework);

        Collection<RelationshipInstance> relationships = officeWork.connections("tasks");
        Collection<ThingInstance> relatedItems = new ArrayList<ThingInstance>();
        for(RelationshipInstance relationship : relationships){
            relatedItems.add(relationship.getTo());
        }

        Assert.assertTrue(relatedItems.contains(paperwork));
        Assert.assertTrue(relatedItems.contains(filework));


        relatedItems = officeWork.connectedItems("tasks");
        Assert.assertTrue(relatedItems.contains(paperwork));
        Assert.assertTrue(relatedItems.contains(filework));

        todo.deleteInstance(paperwork.getGUID());

        relatedItems = officeWork.connectedItems("tasks");
        Assert.assertFalse(relatedItems.contains(paperwork));
        Assert.assertTrue(relatedItems.contains(filework));
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

        Assert.assertEquals("FALSE", paperwork.getValue("doneStatus"));

        System.out.println(todoManager.toString());

        tidy.setValue("doneStatus", "TRUE");
        Assert.assertEquals("TRUE", tidy.getValue("doneStatus"));
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

        Assert.assertEquals("Delete this todo", foundit.getValue("title"));

        todos.deleteInstance(foundit.getGUID());
        Assert.assertEquals(originalTodosCount, todos.countInstances());


        foundit = todos.findInstanceByGUID(tidy.getGUID());

        Assert.assertNull(foundit);


        try{
            todos.deleteInstance(foundit.getGUID());
            Assert.fail("Item already deleted, exception should have been thrown");
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

        Assert.assertEquals("Delete this todo", foundit.getValue("title"));

        todos.deleteInstance(guid);
        Assert.assertEquals(originalTodosCount, todos.countInstances());


        foundit = todos.findInstanceByGUID(guid);

        Assert.assertNull(foundit);


        try{
            todos.deleteInstance(foundit.getGUID());
            Assert.fail("Item already deleted, exception should have been thrown");
        }catch(Exception e){

        }

    }
}
