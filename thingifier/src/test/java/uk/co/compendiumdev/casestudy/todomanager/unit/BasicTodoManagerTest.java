package uk.co.compendiumdev.casestudy.todomanager.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import java.util.Collection;

public class BasicTodoManagerTest {

    private Thingifier todoManager;

    @BeforeEach
    public void createDefinitions(){

        todoManager = TodoManagerModel.definedAsThingifier();

    }

    @Test
    public void todoModelDefinitionCheck(){


        EntityInstanceCollection todo = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);

        Assertions.assertTrue(todo.definition().hasFieldNameDefined("title"));
        Assertions.assertTrue(todo.definition().hasFieldNameDefined("description"));
        Assertions.assertTrue(todo.definition().hasFieldNameDefined("doneStatus"));

        Assertions.assertEquals("false", todo.definition().
                                                    getField("doneStatus").
                                                    getDefaultValue().asString());

    }


    @Test
    public void relationshipDefinitionCheck(){


        EntityInstanceCollection todo = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstanceCollection project = todoManager.getThingInstancesNamed("project", EntityRelModel.DEFAULT_DATABASE_NAME);

        EntityInstance paperwork = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "scan paperwork");
        EntityInstance filework = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "file paperwork");

        EntityInstance officeWork = project.addInstance(new EntityInstance(project.definition())).setValue("title", "Office Work");

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

        EntityInstanceCollection todos = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);

        EntityInstance tidy = todos.addInstance(new EntityInstance(todos.definition())).
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        EntityInstance paperwork = todos.addInstance(new EntityInstance(todos.definition())).
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

        EntityInstanceCollection todos = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);

        int originalTodosCount = todos.countInstances();

        EntityInstance tidy = todos.addInstance(new EntityInstance(todos.definition())).
                setValue("title","Delete this todo").
                setValue("description", "I need to be deleted");

        EntityInstance foundit = todos.findInstanceByPrimaryKey(tidy.getPrimaryKeyValue());

        Assertions.assertEquals("Delete this todo", foundit.getFieldValue("title").asString());

        todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);
        Assertions.assertEquals(originalTodosCount, todos.countInstances());


        foundit = todos.findInstanceByPrimaryKey(tidy.getPrimaryKeyValue());

        Assertions.assertNull(foundit);


        try{
            todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);
            Assertions.fail("Item already deleted, exception should have been thrown");
        }catch(Exception e){

        }

    }

    @Test
    public void createAmendAndDeleteATodoWithAGivenGUID(){

        EntityInstanceCollection todos = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);

        int originalTodosCount = todos.countInstances();

        String guid="6fd86e2d-7c52-4dea-85bb-34760ef66d9d";

        EntityInstance tidy = new EntityInstance(todos.definition());
        tidy.overrideValue("guid", guid);

        tidy.setValue("title", "Delete this todo").
        setValue("description", "I need to be deleted");

        todos.addInstance(tidy);

        EntityInstance foundit = todos.findInstanceByFieldNameAndValue("guid", guid);

        Assertions.assertEquals("Delete this todo", foundit.getFieldValue("title").asString());

        todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);

        Assertions.assertEquals(originalTodosCount, todos.countInstances());


        foundit = todos.findInstanceByFieldNameAndValue("guid", guid);

        Assertions.assertNull(foundit);


        try{
            todoManager.deleteThing(foundit, EntityRelModel.DEFAULT_DATABASE_NAME);

            Assertions.fail("Item already deleted, exception should have been thrown");

        }catch(Exception e){

        }

    }
}
