package uk.co.compendiumdev.casestudy.todomanager.http_api;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

public class DeleteRequestTest {

    private Thingifier todoManager;

    Thing todo;
    Thing project;


    // TODO: need the http_api tests to achieve 100% of ThingifierRestApiHandler

    @Before
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");


    }

    @Test
    public void canDeleteItem(){


        final ThingInstance instance = todo.createInstance().setValue("title", "my title");
        todo.addInstance(instance);

        Assert.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("/todos/" + instance.getGUID());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertEquals(0, todo.countInstances());

    }

    @Test
    public void cannotDeleteItemThatDoesNotExist(){


        final ThingInstance instance = todo.createInstance().setValue("title", "my title");
        todo.addInstance(instance);

        Assert.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("/todos/" + instance.getGUID()+"bob");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assert.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertEquals(1, todo.countInstances());

    }

    @Test
    public void cannotDeleteRootItem(){


        final ThingInstance instance = todo.createInstance().setValue("title", "my title");
        todo.addInstance(instance);

        Assert.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("/todos");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assert.assertEquals(405, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertEquals(1, todo.countInstances());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assert.assertEquals(1, errors.errorMessages.length);
        Assert.assertEquals("Cannot delete root level entity",errors.errorMessages[0]);
    }

    private class ErrorMessages{

        String[] errorMessages;
    }

}
