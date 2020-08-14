package uk.co.compendiumdev.thingifier.api.response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.ArrayList;
import java.util.List;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

public class ApiResponseTest {


    @Test
    public void response404HasSingleErrorMessage(){

        ApiResponse response = ApiResponse.error404("oops");

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        Assertions.assertEquals(1, response.getErrorMessages().size());
        Assertions.assertTrue(response.getErrorMessages().contains("oops"));
    }

    @Test
    public void responseError(){

        ApiResponse response = ApiResponse.error(500, "oopsy");

        Assertions.assertEquals(500, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        Assertions.assertEquals(1, response.getErrorMessages().size());
        Assertions.assertTrue(response.getErrorMessages().contains("oopsy"));

    }

    @Test
    public void responseErrors(){

        List<String> errors = new ArrayList();
        errors.add("oopsy");
        errors.add("doopsy");
        errors.add("do");

        ApiResponse response = ApiResponse.error(501, errors);

        Assertions.assertEquals(501, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        Assertions.assertEquals(3, response.getErrorMessages().size());
        Assertions.assertTrue(response.getErrorMessages().contains("oopsy"));
        Assertions.assertTrue(response.getErrorMessages().contains("doopsy"));
        Assertions.assertTrue(response.getErrorMessages().contains("do"));

    }

    @Test
    public void response200(){

        ApiResponse response = ApiResponse.success();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(false, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

    }

    @Test
    public void response200WithInstance(){

        Thingifier thingifier = new Thingifier();
        Thing todo = thingifier.createThing("todo", "todos");
        todo.definition().addFields( Field.is("title", STRING));
        Thing todos = thingifier.getThingNamed("todo");

        ThingInstance aTodo = todos.createManagedInstance().setValue("title", "a todo");

        ApiResponse response = ApiResponse.success().returnSingleInstance(aTodo);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        Assertions.assertEquals(aTodo, response.getReturnedInstance());

    }

    @Test
    public void response200WithInstances(){

        Thingifier thingifier = new Thingifier();
        Thing todo = thingifier.createThing("todo", "todos");
        todo.definition().addFields( Field.is("title", STRING));
        Thing todos = thingifier.getThingNamed("todo");

        ThingInstance aTodo = todos.createManagedInstance().setValue("title", "a todo");
        ThingInstance anotherTodo = todos.createManagedInstance().setValue("title", "another todo");

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList(todos.getInstances()));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(true, response.isCollection());

        Assertions.assertEquals(2, response.getReturnedInstanceCollection().size());
        Assertions.assertTrue(response.getReturnedInstanceCollection().contains(aTodo));
        Assertions.assertTrue(response.getReturnedInstanceCollection().contains(anotherTodo));

    }

    @Test
    public void response200WithEmptyInstances(){

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(true, response.isCollection());

        Assertions.assertEquals(0, response.getReturnedInstanceCollection().size());
    }

}
