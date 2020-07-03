package uk.co.compendiumdev.thingifier.api.response;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
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

public class ApiResponseAsXmlTest {


    @Test
    public void response404HasSingleErrorMessage(){

        ApiResponse response = ApiResponse.error404("oops");

        Assert.assertEquals(404, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(true, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        String xml = new ApiResponseAsXml(response).getXml();

        System.out.println(xml);

        XStream xstream = getErrorMessagesXmlParser();
        ErrorMessagesResponse messages = (ErrorMessagesResponse) xstream.fromXML(xml);

        Assert.assertEquals(1, messages.errorMessages.length);
        Assert.assertEquals("oops", messages.errorMessages[0]);

    }



    @Test
    public void responseError(){

        ApiResponse response = ApiResponse.error(500, "oopsy");

        Assert.assertEquals(500, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(true, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        String xml = new ApiResponseAsXml(response).getXml();

        System.out.println(xml);

        XStream xstream = getErrorMessagesXmlParser();
        ErrorMessagesResponse messages = (ErrorMessagesResponse) xstream.fromXML(xml);

        Assert.assertEquals(1, messages.errorMessages.length);
        Assert.assertEquals("oopsy", messages.errorMessages[0]);

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

        String xml = new ApiResponseAsXml(response).getXml();

        System.out.println(xml);

        XStream xstream = getErrorMessagesXmlParser();
        ErrorMessagesResponse messages = (ErrorMessagesResponse) xstream.fromXML(xml);

        Assert.assertEquals(3, messages.errorMessages.length);

        List<String> checkErrors = Arrays.asList(messages.errorMessages);

        Assert.assertTrue(checkErrors.contains("oopsy"));
        Assert.assertTrue(checkErrors.contains("doopsy"));
        Assert.assertTrue(checkErrors.contains("do"));

    }

    @Test
    public void response200(){

        ApiResponse response = ApiResponse.success();

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(false, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        String xml = new ApiResponseAsXml(response).getXml();

        Assert.assertEquals("", xml);

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

        String xml = new ApiResponseAsXml(response).getXml();

        XStream xstream = getTodoXmlParser();
        Todo myTodo = (Todo) xstream.fromXML(xml);

        Assert.assertEquals(aTodo.getGUID(), myTodo.guid);
        Assert.assertEquals("a todo", myTodo.title);

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


        String xml = new ApiResponseAsXml(response).getXml();
        System.out.println(xml);
        XStream xstream = getTodosXmlParser();
        TodoCollectionResponse myTodo = (TodoCollectionResponse) xstream.fromXML(xml);


        int foundCount=0;
        for(int todoid = 0; todoid < 2; todoid++){

            if (myTodo.todos[todoid].guid.equals(aTodo.getGUID())) {
                Assert.assertEquals(aTodo.getGUID(), myTodo.todos[todoid].guid);
                Assert.assertEquals("a todo", myTodo.todos[todoid].title);
                foundCount++;
            }else {
                Assert.assertEquals(anotherTodo.getGUID(), myTodo.todos[todoid].guid);
                Assert.assertEquals("another todo", myTodo.todos[todoid].title);
                foundCount++;
            }


        }
        Assert.assertEquals(2, foundCount);

    }

    @Test
    public void response200WithEmptyInstancesAndNoTypeForXML(){

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList());
        // type has not been set in response

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(true, response.isCollection());

        String xml = new ApiResponseAsXml(response).getXml();
        System.out.println(xml);

        Assert.assertEquals("", xml);

    }

    @Test
    public void response200WithEmptyInstances(){

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList());
        Thingifier thingifier = new Thingifier();
        Thing todo = thingifier.createThing("todo", "todos");
        response.resultContainsType(todo.definition());

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(true, response.isCollection());

        String xml = new ApiResponseAsXml(response).getXml();
        System.out.println(xml);
        XStream xstream = getTodosXmlParser();
        TodoCollectionResponse myTodo = (TodoCollectionResponse) xstream.fromXML(xml);

        Assert.assertEquals("<todos></todos>", xml);

        Assert.assertNull(myTodo.todos);
    }


    private XStream getErrorMessagesXmlParser() {
        XStream xstream = new XStream(new StaxDriver());

        XStream.setupDefaultSecurity(xstream);
        Class<?>[] classes = new Class[] { ErrorMessagesResponse.class, String.class };
        xstream.allowTypes(classes);

        xstream.alias("errorMessages", ErrorMessagesResponse.class);
        xstream.addImplicitCollection(ErrorMessagesResponse.class, "errorMessages");
        xstream.alias("errorMessage", String.class);
        return xstream;
    }

    private XStream getTodoXmlParser() {
        XStream xstream = new XStream(new StaxDriver());

        XStream.setupDefaultSecurity(xstream);
        Class<?>[] classes = new Class[] { Todo.class};
        xstream.allowTypes(classes);


        xstream.alias("todo", Todo.class);
        return xstream;
    }

    private XStream getTodosXmlParser() {
        XStream xstream = new XStream(new StaxDriver());

        XStream.setupDefaultSecurity(xstream);
        Class<?>[] classes = new Class[] { TodoCollectionResponse.class, Todo.class};
        xstream.allowTypes(classes);

        xstream.alias("todos", TodoCollectionResponse.class);
        xstream.addImplicitCollection(TodoCollectionResponse.class, "todos");
        xstream.alias("todo", Todo.class);
        return xstream;
    }

    private class ErrorMessagesResponse{

        String []errorMessages;

    }

    private class TodoResponse{

        Todo todo;
    }

    private class TodoCollectionResponse{

        Todo[] todos;
    }

    private class Todo{

        String guid;
        String title;
    }

}
