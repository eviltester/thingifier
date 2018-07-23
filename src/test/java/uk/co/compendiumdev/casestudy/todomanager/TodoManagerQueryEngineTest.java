package uk.co.compendiumdev.casestudy.todomanager;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.query.SimpleQuery;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.List;

public class TodoManagerQueryEngineTest {

    private Thingifier todoManager;
    ThingInstance paperwork;
    ThingInstance filework;
    Thing project;
    ThingInstance officeCategory;

    @Before
    public void createDefinitions(){

        todoManager = TodoManagerModel.definedAsThingifier();

        Thing todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");
        Thing category = todoManager.getThingNamed("category");

        paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);

        //System.out.println(new Gson().toJson(JsonThing.asJsonObject(paperwork)));

        filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        officeCategory = category.createInstance().setValue("title", "Office");
        category.addInstance(officeCategory);

        ThingInstance homeCategory = category.createInstance().setValue("title", "Home");
        category.addInstance(homeCategory);


        paperwork.connects("categories", officeCategory);

    }

   /*


        API Prototype backend query engine


     */


    @Test
    public void canGetListOfEntityInstancesViaName(){
        // todo

        final SimpleQuery query = new SimpleQuery(todoManager, "todo");

        List<ThingInstance> queryResults = query.performQuery().getListThingInstance();

        Assert.assertTrue(query.isResultACollection());

        Assert.assertEquals(2, queryResults.size());
        Assert.assertTrue(queryResults.contains(paperwork));
        Assert.assertTrue(queryResults.contains(filework));

        System.out.println(JsonThing.asJson(queryResults));

    }

    @Test
    public void canGetListOfEntityInstancesViaPluralName(){
        // todos
        final SimpleQuery query = new SimpleQuery(todoManager, "todos");

        List<ThingInstance> queryResults = query.performQuery().getListThingInstance();

        Assert.assertTrue(query.isResultACollection());

        Assert.assertEquals(2, queryResults.size());
        Assert.assertTrue(queryResults.contains(paperwork));
        Assert.assertTrue(queryResults.contains(filework));

        System.out.println(JsonThing.asJson(queryResults));

    }

    @Test
    public void canGetSpecificEntityInstanceUsingGUID(){

        List<ThingInstance> queryResults;

        // todo/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager, "todo/" + paperwork.getGUID());

        queryResults = query.performQuery().getListThingInstance();

        Assert.assertFalse(query.isResultACollection()); // it can still be returned as a collection but is valid to return as a single

        Assert.assertEquals(1, queryResults.size());
        Assert.assertTrue(queryResults.contains(paperwork));
        Assert.assertFalse(queryResults.contains(filework));

        System.out.println(JsonThing.asJson(queryResults));

    }

    @Test
    public void canGetSpecificEntityInstanceUsingGUIDOnPlural(){

        List<ThingInstance> queryResults;

        // todo/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager, "todos/" + paperwork.getGUID());

        queryResults = query.performQuery().getListThingInstance();

        Assert.assertTrue(query.isResultACollection()); // plural should always report itself as a collection even on instance

        Assert.assertEquals(1, queryResults.size());
        Assert.assertTrue(queryResults.contains(paperwork));
        Assert.assertFalse(queryResults.contains(filework));

        System.out.println(JsonThing.asJson(queryResults));

    }


    @Test
    public void cannotGetGuidThatDoesNotExist(){

        List<ThingInstance> queryResults;

        // todo/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager, "todo/" + paperwork.getGUID() + "bob");

        queryResults = query.performQuery().getListThingInstance();

        // even though it doesn not match anything I should know what type of thing this empty collection is
        Assert.assertTrue(query.isResultACollection());
        Assert.assertEquals(todoManager.getThingNamed("todo").definition(), query.resultContainsDefn());

        Assert.assertEquals(0, queryResults.size());
        System.out.println(JsonThing.asJson(queryResults));

    }

    @Test
    public void cannotGetGuidThatDoesNotExistWithPlural(){

        List<ThingInstance> queryResults;

        // todo/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager, "todos/" + paperwork.getGUID() + "bob");

        queryResults = query.performQuery().getListThingInstance();

        // even though it doesn not match anything I should know what type of thing this empty collection is
        Assert.assertTrue(query.isResultACollection());
        Assert.assertEquals(todoManager.getThingNamed("todo").definition(), query.resultContainsDefn());

        Assert.assertEquals(0, queryResults.size());
        System.out.println(JsonThing.asJson(queryResults));

    }



    @Test
    public void connectionTesting() {


        // stuff we could get for free from backend

        List<ThingInstance> queryResults;

        //
        ThingInstance officeWork = project.createInstance().setValue("title", "Office Work");
        project.addInstance(officeWork);

        officeWork.connects("tasks", paperwork);
        officeWork.connects("tasks", filework);


        // match on relationships
        // project/_GUID_/tasks

        queryResults = new SimpleQuery(todoManager, String.format("project/%s/tasks", officeWork.getGUID())).performQuery().getListThingInstance();

        Assert.assertEquals(2, queryResults.size());
        Assert.assertTrue(queryResults.contains(paperwork));
        Assert.assertTrue(queryResults.contains(filework));

        System.out.println(JsonThing.asJson(queryResults));




        // should be able to get projects for a task

        queryResults = new SimpleQuery(todoManager, String.format("todo/%s/task-of", paperwork.getGUID())).performQuery().getListThingInstance();
        Assert.assertEquals(1, queryResults.size());
        Assert.assertTrue(queryResults.contains(officeWork));
        System.out.println(JsonThing.asJson(queryResults));


        // match on entity types
        // project/_GUID_/todo

        queryResults = new SimpleQuery(todoManager, String.format("project/%s/todo", officeWork.getGUID())).performQuery().getListThingInstance();

        Assert.assertEquals(2, queryResults.size());
        Assert.assertTrue(queryResults.contains(paperwork));
        Assert.assertTrue(queryResults.contains(filework));

        System.out.println(JsonThing.asJson(queryResults));

        // project/_GUID_/todo/category

        queryResults = new SimpleQuery(todoManager, String.format("project/%s/todo/category", officeWork.getGUID())).performQuery().getListThingInstance();

        Assert.assertEquals(1, queryResults.size());
        Assert.assertTrue(queryResults.contains(officeCategory));

        System.out.println(JsonThing.asJson(queryResults));

        // invalid query should match nothing there is no entity called task
        // project/_GUID_/task

        queryResults = new SimpleQuery(todoManager, String.format("project/%s/task", officeWork.getGUID())).performQuery().getListThingInstance();

        Assert.assertEquals(0, queryResults.size());



    }


}
