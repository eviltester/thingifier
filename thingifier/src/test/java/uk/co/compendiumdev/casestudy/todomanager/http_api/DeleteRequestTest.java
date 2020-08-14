package uk.co.compendiumdev.casestudy.todomanager.http_api;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

public class DeleteRequestTest {

    private Thingifier todoManager;

    Thing todo;
    Thing project;


    // TODO: need the http_api tests to achieve 100% of ThingifierRestApiHandler

    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");


    }

    @Test
    public void canDeleteItem(){


        final ThingInstance instance = todo.createManagedInstance().setValue("title", "my title");

        Assertions.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("/todos/" + instance.getGUID());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(0, todo.countInstances());

    }

    @Test
    public void cannotDeleteItemThatDoesNotExist(){


        final ThingInstance instance = todo.createManagedInstance().setValue("title", "my title");

        Assertions.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("/todos/" + instance.getGUID()+"bob");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(1, todo.countInstances());

    }

    @Test
    public void cannotDeleteRootItem(){


        final ThingInstance instance = todo.createManagedInstance().setValue("title", "my title");

        Assertions.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("/todos");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(405, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(1, todo.countInstances());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assertions.assertEquals(1, errors.errorMessages.length);
        Assertions.assertEquals("Cannot delete root level entity",errors.errorMessages[0]);
    }

    private class ErrorMessages{

        String[] errorMessages;
    }

}
