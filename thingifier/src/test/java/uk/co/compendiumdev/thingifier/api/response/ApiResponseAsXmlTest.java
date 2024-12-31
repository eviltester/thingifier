package uk.co.compendiumdev.thingifier.api.response;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

public class ApiResponseAsXmlTest {

    private JsonThing defaultJsonThing = new JsonThing(new ThingifierApiConfig("").jsonOutput());

    @Test
    public void response404HasSingleErrorMessage(){

        ApiResponse response = ApiResponse.error404("oops");

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        String xml = new ApiResponseAsXml(response, defaultJsonThing).getXml();

        System.out.println(xml);

        XStream xstream = getErrorMessagesXmlParser();
        ErrorMessagesResponse messages = (ErrorMessagesResponse) xstream.fromXML(xml);

        Assertions.assertEquals(1, messages.errorMessages.length);
        Assertions.assertEquals("oops", messages.errorMessages[0]);

    }



    @Test
    public void responseError(){

        ApiResponse response = ApiResponse.error(500, "oopsy");

        Assertions.assertEquals(500, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        String xml = new ApiResponseAsXml(response, defaultJsonThing).getXml();

        System.out.println(xml);

        XStream xstream = getErrorMessagesXmlParser();
        ErrorMessagesResponse messages = (ErrorMessagesResponse) xstream.fromXML(xml);

        Assertions.assertEquals(1, messages.errorMessages.length);
        Assertions.assertEquals("oopsy", messages.errorMessages[0]);

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

        String xml = new ApiResponseAsXml(response, defaultJsonThing).getXml();

        System.out.println(xml);

        XStream xstream = getErrorMessagesXmlParser();
        ErrorMessagesResponse messages = (ErrorMessagesResponse) xstream.fromXML(xml);

        Assertions.assertEquals(3, messages.errorMessages.length);

        List<String> checkErrors = Arrays.asList(messages.errorMessages);

        Assertions.assertTrue(checkErrors.contains("oopsy"));
        Assertions.assertTrue(checkErrors.contains("doopsy"));
        Assertions.assertTrue(checkErrors.contains("do"));

    }

    @Test
    public void response200(){

        ApiResponse response = ApiResponse.success();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(false, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        String xml = new ApiResponseAsXml(response, defaultJsonThing).getXml();

        Assertions.assertEquals("", xml);

    }

    @Test
    public void response200WithInstance(){

        Thingifier thingifier = new Thingifier();
        EntityDefinition todo = thingifier.defineThing("todo", "todos");
        todo.addFields( Field.is("title", STRING));
        EntityInstanceCollection todos = thingifier.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);

        EntityInstance aTodo = todos.addInstance(new EntityInstance(todos.definition())).setValue("title", "a todo");

        ApiResponse response = ApiResponse.success().returnSingleInstance(aTodo);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        String xml = new ApiResponseAsXml(response, defaultJsonThing).getXml();

        XStream xstream = getTodoXmlParser();
        Todo myTodo = (Todo) xstream.fromXML(xml);

        Assertions.assertEquals(aTodo.getPrimaryKeyValue(), myTodo.guid);
        Assertions.assertEquals("a todo", myTodo.title);

    }

    @Test
    public void response200WithInstances(){

        Thingifier thingifier = new Thingifier();
        EntityDefinition todo = thingifier.defineThing("todo", "todos");
        todo.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        todo.addFields( Field.is("title", STRING));
        EntityInstanceCollection todos = thingifier.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);

        EntityInstance aTodo = todos.addInstance(new EntityInstance(todos.definition())).setValue("title", "a todo");
        EntityInstance anotherTodo = todos.addInstance(new EntityInstance(todos.definition())).setValue("title", "another todo");

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList(todos.getInstances()));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(true, response.isCollection());


        String xml = new ApiResponseAsXml(response, defaultJsonThing).getXml();
        System.out.println(xml);
        XStream xstream = getTodosXmlParser();
        TodoCollectionResponse myTodo = (TodoCollectionResponse) xstream.fromXML(xml);


        int foundCount=0;
        for(int todoid = 0; todoid < 2; todoid++){

            if (myTodo.todos[todoid].guid.equals(aTodo.getPrimaryKeyValue())) {
                Assertions.assertEquals(aTodo.getPrimaryKeyValue(), myTodo.todos[todoid].guid);
                Assertions.assertEquals("a todo", myTodo.todos[todoid].title);
                foundCount++;
            }else {
                Assertions.assertEquals(anotherTodo.getPrimaryKeyValue(), myTodo.todos[todoid].guid);
                Assertions.assertEquals("another todo", myTodo.todos[todoid].title);
                foundCount++;
            }


        }
        Assertions.assertEquals(2, foundCount);

    }

    @Test
    public void response200WithEmptyInstancesAndNoTypeForXML(){

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList());
        // type has not been set in response

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(true, response.isCollection());

        String xml = new ApiResponseAsXml(response, defaultJsonThing).getXml();
        System.out.println(xml);

        Assertions.assertEquals("", xml);

    }

    @Test
    public void response200WithEmptyInstances(){

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList());
        Thingifier thingifier = new Thingifier();
        EntityDefinition todo = thingifier.defineThing("todo", "todos");
        response.resultContainsType(todo);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(true, response.isCollection());

        String xml = new ApiResponseAsXml(response, defaultJsonThing).getXml();
        System.out.println(xml);
        XStream xstream = getTodosXmlParser();
        TodoCollectionResponse myTodo = (TodoCollectionResponse) xstream.fromXML(xml);

        Assertions.assertEquals("<todos></todos>", xml);

        Assertions.assertNull(myTodo.todos);
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
