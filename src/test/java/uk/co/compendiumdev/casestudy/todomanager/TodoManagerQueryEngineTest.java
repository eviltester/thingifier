package uk.co.compendiumdev.casestudy.todomanager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.List;

public class TodoManagerQueryEngineTest {

    private Thingifier todoManager;

    @Before
    public void createDefinitions(){

        todoManager = TodoManagerModel.definedAsThingifier();

    }

   /*


        API Prototype backend query engine


     */

    @Test
    public void ApiPrototypeFreeBackend() {


        // stuff we could get for free from backend
        Thing todo = todoManager.getThingNamed("todo");
        Thing project = todoManager.getThingNamed("project");
        Thing category = todoManager.getThingNamed("category");

        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);
        System.out.println(JsonThing.asJson(paperwork));

        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        ThingInstance officeCategory = category.createInstance().setValue("title", "Office");
        category.addInstance(officeCategory);

        ThingInstance homeCategory = category.createInstance().setValue("title", "Home");
        category.addInstance(homeCategory);


        paperwork.connects("categories", officeCategory);

        // todo
        List<ThingInstance> query = todoManager.simplequery("todo");

        Assert.assertEquals(2, query.size());
        Assert.assertTrue(query.contains(paperwork));
        Assert.assertTrue(query.contains(filework));

        System.out.println(JsonThing.asJson(query));

        // todo/_GUID_
        query = todoManager.simplequery("todo/" + paperwork.getGUID());

        Assert.assertEquals(1, query.size());
        Assert.assertTrue(query.contains(paperwork));
        Assert.assertFalse(query.contains(filework));

        ApiResponse apiresponse = todoManager.api().get("todo/" + paperwork.getGUID());
        Assert.assertEquals(200, apiresponse.getStatusCode());

        // get a todo that does not exist
        apiresponse = todoManager.api().get("todo/" + paperwork.getGUID() + "bob");
        Assert.assertEquals(404, apiresponse.getStatusCode());


        //
        ThingInstance officeWork = project.createInstance().setValue("title", "Office Work");
        project.addInstance(officeWork);

        officeWork.connects("tasks", paperwork);
        officeWork.connects("tasks", filework);


        // match on relationships
        // project/_GUID_/tasks
        query = todoManager.simplequery(String.format("project/%s/tasks", officeWork.getGUID()));

        Assert.assertEquals(2, query.size());
        Assert.assertTrue(query.contains(paperwork));
        Assert.assertTrue(query.contains(filework));

        System.out.println(JsonThing.asJson(query));

        // match on entity types
        // project/_GUID_/todo
        query = todoManager.simplequery(String.format("project/%s/todo", officeWork.getGUID()));

        Assert.assertEquals(2, query.size());
        Assert.assertTrue(query.contains(paperwork));
        Assert.assertTrue(query.contains(filework));

        System.out.println(JsonThing.asJson(query));

        // project/_GUID_/todo/category
        query = todoManager.simplequery(String.format("project/%s/todo/category", officeWork.getGUID()));

        Assert.assertEquals(1, query.size());
        Assert.assertTrue(query.contains(officeCategory));

        System.out.println(JsonThing.asJson(query));

        // invalid query should match nothing there is no entity called task
        // project/_GUID_/task
        query = todoManager.simplequery(String.format("project/%s/task", officeWork.getGUID()));

        Assert.assertEquals(0, query.size());

    }


}
