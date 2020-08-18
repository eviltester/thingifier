package uk.co.compendiumdev.thingifier.core.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.List;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

public class TodoManagerQueryEngineTest {

    private EntityRelModel todoManager;
    ThingInstance paperwork;
    ThingInstance filework;
    Thing project;
    ThingInstance officeCategory;

    // todo: simplify setup and move this test into core
    @BeforeEach
    public void createDefinitions(){

        todoManager = new EntityRelModel();
        Thing todo = todoManager.createThing("todo", "todos");

        todo.definition()
                .addFields( Field.is("title", STRING)
                )
        ;


        project = todoManager.createThing("project", "projects");

        project.definition()
                .addFields(
                        Field.is("title", STRING));


        Thing category = todoManager.createThing("category", "categories");

        category.definition()
                .addFields(
                        Field.is("title", STRING));

        todoManager.defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY).
                whenReversed(Cardinality.ONE_TO_MANY,"task-of");

        todoManager.defineRelationship(project, category, "categories", Cardinality.ONE_TO_MANY);
        todoManager.defineRelationship(category, todo, "todos", Cardinality.ONE_TO_MANY);
        todoManager.defineRelationship(category, project, "projects", Cardinality.ONE_TO_MANY);
        todoManager.defineRelationship(todo, category, "categories", Cardinality.ONE_TO_MANY);


        paperwork = todo.createManagedInstance().setValue("title", "scan paperwork");

        //System.out.println(new Gson().toJson(JsonThing.asJsonObject(paperwork)));

        filework = todo.createManagedInstance().setValue("title", "file paperwork");

        officeCategory = category.createManagedInstance().setValue("title", "Office");

        ThingInstance homeCategory = category.createManagedInstance().setValue("title", "Home");


        paperwork.getRelationships().connect("categories", officeCategory);

    }

   /*
        API Prototype backend query engine
     */


    @Test
    public void canGetListOfEntityInstancesViaName(){
        // to do

        final SimpleQuery query = new SimpleQuery(todoManager, "todo");

        List<ThingInstance> queryResults = query.performQuery().getListThingInstance();

        Assertions.assertTrue(query.isResultACollection());

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));

    }

    @Test
    public void canGetListOfEntityInstancesViaPluralName(){
        // todos
        final SimpleQuery query = new SimpleQuery(todoManager, "todos");

        List<ThingInstance> queryResults = query.performQuery().getListThingInstance();

        Assertions.assertTrue(query.isResultACollection());

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));
    }

    @Test
    public void canGetSpecificEntityInstanceUsingGUID(){

        List<ThingInstance> queryResults;

        // to do/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager, "todo/" + paperwork.getGUID());

        queryResults = query.performQuery().getListThingInstance();

        Assertions.assertFalse(query.isResultACollection()); // it can still be returned as a collection but is valid to return as a single

        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertFalse(queryResults.contains(filework));

    }

    @Test
    public void canGetSpecificEntityInstanceUsingGUIDOnPlural(){

        List<ThingInstance> queryResults;

        // to do/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager, "todos/" + paperwork.getGUID());

        queryResults = query.performQuery().getListThingInstance();

        Assertions.assertTrue(query.isResultACollection()); // plural should always report itself as a collection even on instance

        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertFalse(queryResults.contains(filework));

    }


    @Test
    public void cannotGetGuidThatDoesNotExist(){

        List<ThingInstance> queryResults;

        // to do/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager, "todo/" + paperwork.getGUID() + "bob");

        queryResults = query.performQuery().getListThingInstance();

        // even though it doesn not match anything I should know what type of thing this empty collection is
        Assertions.assertTrue(query.isResultACollection());
        Assertions.assertEquals(todoManager.getThingNamed("todo").definition(), query.resultContainsDefn());

        Assertions.assertEquals(0, queryResults.size());
    }

    @Test
    public void cannotGetGuidThatDoesNotExistWithPlural(){

        List<ThingInstance> queryResults;

        // to do/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager, "todos/" + paperwork.getGUID() + "bob");

        queryResults = query.performQuery().getListThingInstance();

        // even though it doesn not match anything I should know what type of thing this empty collection is
        Assertions.assertTrue(query.isResultACollection());
        Assertions.assertEquals(todoManager.getThingNamed("todo").definition(), query.resultContainsDefn());

        Assertions.assertEquals(0, queryResults.size());

    }



    @Test
    public void connectionTesting() {


        // stuff we could get for free from backend

        List<ThingInstance> queryResults;

        //
        ThingInstance officeWork = project.createManagedInstance().setValue("title", "Office Work");

        officeWork.getRelationships().connect("tasks", paperwork);
        officeWork.getRelationships().connect("tasks", filework);


        // match on relationships
        // project/_GUID_/tasks

        queryResults = new SimpleQuery(todoManager, String.format("project/%s/tasks", officeWork.getGUID())).performQuery().getListThingInstance();

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));


        // should be able to get projects for a task

        queryResults = new SimpleQuery(todoManager, String.format("todo/%s/task-of", paperwork.getGUID())).performQuery().getListThingInstance();
        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(officeWork));


        // match on entity types
        // project/_GUID_/to do

        queryResults = new SimpleQuery(todoManager, String.format("project/%s/todo", officeWork.getGUID())).performQuery().getListThingInstance();

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));

        // project/_GUID_/to do/category

        queryResults = new SimpleQuery(todoManager, String.format("project/%s/todo/category", officeWork.getGUID())).performQuery().getListThingInstance();

        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(officeCategory));

        // invalid query should match nothing there is no entity called task
        // project/_GUID_/task

        queryResults = new SimpleQuery(todoManager, String.format("project/%s/task", officeWork.getGUID())).performQuery().getListThingInstance();

        Assertions.assertEquals(0, queryResults.size());

    }


}
