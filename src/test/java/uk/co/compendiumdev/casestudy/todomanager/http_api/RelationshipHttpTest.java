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
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.UUID;

public class RelationshipHttpTest {

    private Thingifier todoManager;

    Thing todo;
    Thing project;
    Thing categories;

    @Before
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");
        categories = todoManager.getThingNamed("category");


    }

    @Test
    public void canCreateARelationshipBetweenProjectAndTodoViaTasks(){

        final ThingInstance atodo = todo.createInstance().setValue("title", "a TODO");
        todo.addInstance(atodo);

        final ThingInstance aproject = project.createInstance().setValue("title", "a Project");
        project.addInstance(aproject);

        Assert.assertEquals(0,aproject.connectedItems("tasks").size());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getGUID() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        //{"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", atodo.getGUID());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assert.assertEquals(201, response.getStatusCode());

        Assert.assertEquals(1,aproject.connectedItems("tasks").size());

    }

    @Test
    public void canCreateARelationshipAndTodoBetweenProjectAndTodoViaTasks(){

        final ThingInstance aproject = project.createInstance().setValue("title", "a Project");
        project.addInstance(aproject);

        Assert.assertEquals(0,aproject.connectedItems("tasks").size());
        Assert.assertEquals(0,todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getGUID() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        //{"title":"My New Todo"}
        String body = "{\"title\":\"My New Todo\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assert.assertEquals(201, response.getStatusCode());

        Assert.assertEquals(1,aproject.connectedItems("tasks").size());
        Assert.assertEquals(1,todo.countInstances());

        final ThingInstance inMemoryTodo = todo.findInstanceByGUID(response.getHeaders().get(ApiResponse.GUID_HEADER));
        Assert.assertTrue(response.getBody(), response.getBody().contains(inMemoryTodo.getGUID()));

    }

    @Test
    public void cannotCreateARelationshipBetweenProjectAndCategoryViaTasks(){


        final ThingInstance acategory = categories.createInstance().setValue("title", "a Category");
        todo.addInstance(acategory);

        final ThingInstance aproject = project.createInstance().setValue("title", "a Project");
        project.addInstance(aproject);

        Assert.assertEquals(0,aproject.connectedItems("tasks").size());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getGUID() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        //{"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", acategory.getGUID());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assert.assertEquals(400, response.getStatusCode());

        Assert.assertEquals(0,aproject.connectedItems("tasks").size());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assert.assertEquals(1,errors.errorMessages.length);

        Assert.assertEquals("Could not find a relationship named tasks between project and a category", errors.errorMessages[0]);
    }

    @Test
    public void cannotCreateARelationshipWhenGivenGuidDoesNotExist(){


        final ThingInstance atodo = todo.createInstance().setValue("title", "a TODO");
        todo.addInstance(atodo);

        final ThingInstance aproject = project.createInstance().setValue("title", "a Project");
        project.addInstance(aproject);

        Assert.assertEquals(0,aproject.connectedItems("tasks").size());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getGUID() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        //{"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", atodo.getGUID() + "bob");
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assert.assertEquals(404, response.getStatusCode());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);
        Assert.assertEquals(1,errors.errorMessages.length);

        Assert.assertTrue(errors.errorMessages[0],errors.errorMessages[0].startsWith("Could not find thing with GUID "));
        Assert.assertTrue(errors.errorMessages[0],errors.errorMessages[0].endsWith("bob"));
    }

    // need to see if I can create where a relationship name is the same as a plural entity
    @Test
    public void canCreateARelationshipBetweenCategoryAndTodoViaTodos(){


        final ThingInstance acategory = categories.createInstance().setValue("title", "a Category");
        categories.addInstance(acategory);

        final ThingInstance atodo = todo.createInstance().setValue("title", "a TODO");
        todo.addInstance(atodo);

        Assert.assertEquals(0,acategory.connectedItems("todos").size());

        HttpApiRequest request = new HttpApiRequest("categories/" + acategory.getGUID() + "/todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        //{"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", atodo.getGUID());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assert.assertEquals(201, response.getStatusCode());

        Assert.assertEquals(1,acategory.connectedItems("todos").size());

    }

    @Test
    public void canCreateARelationshipAndTodoBetweenCategoryAndTodoViaTodos(){

        final ThingInstance acategory = categories.createInstance().setValue("title", "a Category");
        categories.addInstance(acategory);


        Assert.assertEquals(0,acategory.connectedItems("todos").size());
        Assert.assertEquals(0,todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("categories/" + acategory.getGUID() + "/todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        //{"title":"My New Todo"}
        String body = "{\"title\":\"My New Todo\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assert.assertEquals(201, response.getStatusCode());

        Assert.assertEquals(1,acategory.connectedItems("todos").size());
        Assert.assertEquals(1,todo.countInstances());

        final ThingInstance inMemoryTodo = todo.findInstanceByGUID(response.getHeaders().get(ApiResponse.GUID_HEADER));
        Assert.assertTrue(response.getBody(), response.getBody().contains(inMemoryTodo.getGUID()));

    }

    @Test
    public void canCreateARelationshipBetweenProjectAndTodoViaTasksUsingXml(){

        final ThingInstance atodo = todo.createInstance().setValue("title", "a TODO");
        todo.addInstance(atodo);

        final ThingInstance aproject = project.createInstance().setValue("title", "a Project");
        project.addInstance(aproject);

        Assert.assertEquals(0,aproject.connectedItems("tasks").size());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getGUID() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.containsXml());

        //<todo><guid>%s</guid></todo>}
        String body = String.format("<todo><guid>%s</guid></todo>", atodo.getGUID());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assert.assertEquals(201, response.getStatusCode());

        Assert.assertEquals(1,aproject.connectedItems("tasks").size());

    }

    @Test
    public void canDeleteARelationshipBetweenProjectAndTodoViaTasks(){

        final ThingInstance atodo = todo.createInstance().setValue("title", "a TODO");
        todo.addInstance(atodo);

        final ThingInstance aproject = project.createInstance().setValue("title", "a Project");
        project.addInstance(aproject);

        aproject.connects("tasks", atodo);

        Assert.assertEquals(1,aproject.connectedItems("tasks").size());
        Assert.assertEquals(1, todo.countInstances());
        Assert.assertEquals(1, project.countInstances());


        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getGUID() + "/tasks/" + atodo.getGUID());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assert.assertEquals(200, response.getStatusCode());

        Assert.assertEquals(0,aproject.connectedItems("tasks").size());
        Assert.assertEquals(1, todo.countInstances());
        Assert.assertEquals(1, project.countInstances());

    }

    // need to see if I can delete where a relationship name is the same as a plural entity
    @Test
    public void canDeleteARelationshipBetweenCategoryAndTodoViaTodos(){


        final ThingInstance acategory = categories.createInstance().setValue("title", "a Category");
        categories.addInstance(acategory);

        final ThingInstance atodo = todo.createInstance().setValue("title", "a TODO");
        todo.addInstance(atodo);

        acategory.connects("todos", atodo);

        Assert.assertEquals(1,acategory.connectedItems("todos").size());
        Assert.assertEquals(1, todo.countInstances());
        Assert.assertEquals(1, categories.countInstances());

        final HttpApiRequest request = new HttpApiRequest("categories/" + acategory.getGUID() + "/todos/" + atodo.getGUID());

        HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assert.assertEquals(200, response.getStatusCode());

        Assert.assertEquals(0,acategory.connectedItems("todos").size());
        Assert.assertEquals(1, todo.countInstances());
        Assert.assertEquals(1, categories.countInstances());

        // if relationship doesn't exist, I should get a 404 if I reissue therequest
        response = new ThingifierHttpApi(todoManager).delete(request);
        Assert.assertEquals(404, response.getStatusCode());

        Assert.assertEquals(0,acategory.connectedItems("todos").size());
        Assert.assertEquals(1, todo.countInstances());
        Assert.assertEquals(1, categories.countInstances());
    }

    private class ErrorMessages{

        String[] errorMessages;
    }

    private class Todo{

        String guid;
        String title;
    }
}
