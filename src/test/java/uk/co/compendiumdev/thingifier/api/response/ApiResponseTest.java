package uk.co.compendiumdev.thingifier.api.response;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.co.compendiumdev.thingifier.generic.FieldType.STRING;

public class ApiResponseTest {


    @Test
    public void response404HasSingleErrorMessage(){

        ApiResponse response = ApiResponse.error404("oops");

        Assert.assertEquals(404, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(true, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        Assert.assertEquals(1, response.getErrorMessages().size());
        Assert.assertTrue(response.getErrorMessages().contains("oops"));
    }

    @Test
    public void responseError(){

        ApiResponse response = ApiResponse.error(500, "oopsy");

        Assert.assertEquals(500, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(true, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        Assert.assertEquals(1, response.getErrorMessages().size());
        Assert.assertTrue(response.getErrorMessages().contains("oopsy"));

    }

    @Test
    public void responseErrors(){

        List<String> errors = new ArrayList();
        errors.add("oopsy");
        errors.add("doopsy");
        errors.add("do");

        ApiResponse response = ApiResponse.error(501, errors);

        Assert.assertEquals(501, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(true, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        Assert.assertEquals(3, response.getErrorMessages().size());
        Assert.assertTrue(response.getErrorMessages().contains("oopsy"));
        Assert.assertTrue(response.getErrorMessages().contains("doopsy"));
        Assert.assertTrue(response.getErrorMessages().contains("do"));

    }

    @Test
    public void response200(){

        ApiResponse response = ApiResponse.success();

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(false, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

    }

    @Test
    public void response200WithInstance(){

        Thingifier thingifier = new Thingifier();
        Thing todo = thingifier.createThing("todo", "todos");
        todo.definition().addFields( Field.is("title", STRING));
        Thing todos = thingifier.getThingNamed("todo");

        ThingInstance aTodo = todos.createInstance().setValue("title", "a todo");
        todos.addInstance(aTodo);

        ApiResponse response = ApiResponse.success().returnSingleInstance(aTodo);

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        Assert.assertEquals(aTodo, response.getReturnedInstance());

    }

    @Test
    public void response200WithInstances(){

        Thingifier thingifier = new Thingifier();
        Thing todo = thingifier.createThing("todo", "todos");
        todo.definition().addFields( Field.is("title", STRING));
        Thing todos = thingifier.getThingNamed("todo");

        ThingInstance aTodo = todos.createInstance().setValue("title", "a todo");
        todos.addInstance(aTodo);
        ThingInstance anotherTodo = todos.createInstance().setValue("title", "another todo");
        todos.addInstance(anotherTodo);

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList(todos.getInstances()));

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(true, response.isCollection());

        Assert.assertEquals(2, response.getReturnedInstanceCollection().size());
        Assert.assertTrue(response.getReturnedInstanceCollection().contains(aTodo));
        Assert.assertTrue(response.getReturnedInstanceCollection().contains(anotherTodo));

    }

    @Test
    public void response200WithEmptyInstances(){

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList());

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(true, response.isCollection());

        Assert.assertEquals(0, response.getReturnedInstanceCollection().size());
    }

}
