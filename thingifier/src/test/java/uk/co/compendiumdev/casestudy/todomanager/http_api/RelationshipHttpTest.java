package uk.co.compendiumdev.casestudy.todomanager.http_api;

import com.google.gson.Gson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;


public class RelationshipHttpTest {

    private Thingifier todoManager;

    EntityInstanceCollection todo;
    EntityInstanceCollection project;
    EntityInstanceCollection categories;

    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);
        project = todoManager.getThingInstancesNamed("project", EntityRelModel.DEFAULT_DATABASE_NAME);
        categories = todoManager.getThingInstancesNamed("category", EntityRelModel.DEFAULT_DATABASE_NAME);


    }

    @Test
    public void canCreateARelationshipBetweenProjectAndTodoViaTasks(){

        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO");

        final EntityInstance aproject = project.addInstance(new EntityInstance(project.definition())).setValue("title", "a Project");

        Assertions.assertEquals(0, aproject.getRelationships().getConnectedItems("tasks").size());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        //{"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", atodo.getPrimaryKeyValue());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(1, aproject.getRelationships().getConnectedItems("tasks").size());

    }

    @Test
    public void canCreateARelationshipBetweenProjectAndTodoViaTasksUsingID(){

        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO");

        final EntityInstance aproject = project.addInstance(new EntityInstance(project.definition())).setValue("title", "a Project");

        Assertions.assertEquals(0, aproject.getRelationships().getConnectedItems("tasks").size());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        //{"guid":"%s"}
        String body = String.format("{\"id\":\"%s\"}", atodo.getFieldValue("id").asString());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(1, aproject.getRelationships().getConnectedItems("tasks").size());

    }




    @Test
    public void canCreateARelationshipAndTodoBetweenProjectAndTodoViaTasks(){

        final EntityInstance aproject = project.addInstance(new EntityInstance(project.definition())).setValue("title", "a Project");

        Assertions.assertEquals(0, aproject.getRelationships().getConnectedItems("tasks").size());
        Assertions.assertEquals(0,todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        //{"title":"My New To do"}
        String body = "{\"title\":\"My New To do\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(1, aproject.getRelationships().getConnectedItems("tasks").size());
        Assertions.assertEquals(1,todo.countInstances());

        final EntityInstance inMemoryTodo = todo.findInstanceByPrimaryKey(response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER));
        Assertions.assertTrue(response.getBody().contains(inMemoryTodo.getPrimaryKeyValue()),
                response.getBody());

    }

    @Test
    public void cannotCreateARelationshipBetweenProjectAndCategoryViaTasks(){


        final EntityInstance acategory = categories.addInstance(new EntityInstance(categories.definition())).setValue("title", "a Category");

        final EntityInstance aproject = project.addInstance(new EntityInstance(project.definition())).setValue("title", "a Project");

        Assertions.assertEquals(0, aproject.getRelationships().getConnectedItems("tasks").size());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        //{"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", acategory.getPrimaryKeyValue());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(404, response.getStatusCode());

        Assertions.assertEquals(0, aproject.getRelationships().getConnectedItems("tasks").size());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assertions.assertEquals(1,errors.errorMessages.length);

        Assertions.assertEquals("Could not find thing matching value for guid", errors.errorMessages[0]);
    }

    @Test
    public void cannotCreateARelationshipWhenGivenGuidDoesNotExist(){


        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO");

        final EntityInstance aproject = project.addInstance(new EntityInstance(project.definition())).setValue("title", "a Project");

        Assertions.assertEquals(0, aproject.getRelationships().getConnectedItems("tasks").size());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        //{"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", atodo.getPrimaryKeyValue() + "bob");
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(404, response.getStatusCode());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);
        Assertions.assertEquals(1,errors.errorMessages.length);

        Assertions.assertTrue(errors.errorMessages[0].startsWith("Could not find thing"),
                errors.errorMessages[0]);
    }

    // need to see if I can create where a relationship name is the same as a plural entity
    @Test
    public void canCreateARelationshipBetweenCategoryAndTodoViaTodos(){


        final EntityInstance acategory = categories.addInstance(new EntityInstance(categories.definition())).setValue("title", "a Category");

        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO");

        Assertions.assertEquals(0, acategory.getRelationships().getConnectedItems("todos").size());

        HttpApiRequest request = new HttpApiRequest("categories/" + acategory.getPrimaryKeyValue() + "/todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        //{"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", atodo.getPrimaryKeyValue());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(1, acategory.getRelationships().getConnectedItems("todos").size());

    }

    @Test
    public void canCreateARelationshipAndTodoBetweenCategoryAndTodoViaTodos(){

        final EntityInstance acategory = categories.addInstance(new EntityInstance(categories.definition())).setValue("title", "a Category");

        Assertions.assertEquals(0, acategory.getRelationships().getConnectedItems("todos").size());
        Assertions.assertEquals(0,todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("categories/" + acategory.getPrimaryKeyValue() + "/todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());


        //{"title":"My New To do"}
        String body = "{\"title\":\"My New To do\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(1, acategory.getRelationships().getConnectedItems("todos").size());
        Assertions.assertEquals(1,todo.countInstances());

        final EntityInstance inMemoryTodo = todo.findInstanceByPrimaryKey(response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER));
        Assertions.assertTrue(response.getBody().contains(inMemoryTodo.getPrimaryKeyValue()),
                response.getBody());

    }

    @Test
    public void canCreateARelationshipBetweenProjectAndTodoViaTasksUsingXml(){

        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO");

        final EntityInstance aproject = project.addInstance(new EntityInstance(project.definition())).setValue("title", "a Project");

        Assertions.assertEquals(0, aproject.getRelationships().getConnectedItems("tasks").size());

        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.containsXml());

        String body = String.format("<todo><guid>%s</guid></todo>", atodo.getPrimaryKeyValue());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(1, aproject.getRelationships().getConnectedItems("tasks").size());

    }

    @Test
    public void canDeleteARelationshipBetweenProjectAndTodoViaTasks(){

        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO");

        final EntityInstance aproject = project.addInstance(new EntityInstance(project.definition())).setValue("title", "a Project");

        aproject.getRelationships().connect("tasks", atodo);

        Assertions.assertEquals(1, aproject.getRelationships().getConnectedItems("tasks").size());
        Assertions.assertEquals(1, todo.countInstances());
        Assertions.assertEquals(1, project.countInstances());


        HttpApiRequest request = new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks/" + atodo.getPrimaryKeyValue());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(200, response.getStatusCode());

        Assertions.assertEquals(0, aproject.getRelationships().getConnectedItems("tasks").size());
        Assertions.assertEquals(1, todo.countInstances());
        Assertions.assertEquals(1, project.countInstances());

    }

    // need to see if I can delete where a relationship name is the same as a plural entity
    @Test
    public void canDeleteARelationshipBetweenCategoryAndTodoViaTodos(){


        final EntityInstance acategory = categories.addInstance(new EntityInstance(categories.definition())).setValue("title", "a Category");

        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO");

        acategory.getRelationships().connect("todos", atodo);

        Assertions.assertEquals(1, acategory.getRelationships().getConnectedItems("todos").size());
        Assertions.assertEquals(1, todo.countInstances());
        Assertions.assertEquals(1, categories.countInstances());

        final HttpApiRequest request = new HttpApiRequest("categories/" + acategory.getPrimaryKeyValue() + "/todos/" + atodo.getPrimaryKeyValue());

        HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(200, response.getStatusCode());

        Assertions.assertEquals(0, acategory.getRelationships().getConnectedItems("todos").size());
        Assertions.assertEquals(1, todo.countInstances());
        Assertions.assertEquals(1, categories.countInstances());

        // if relationship doesn't exist, I should get a 404 if I reissue therequest
        response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(404, response.getStatusCode());

        Assertions.assertEquals(0, acategory.getRelationships().getConnectedItems("todos").size());
        Assertions.assertEquals(1, todo.countInstances());
        Assertions.assertEquals(1, categories.countInstances());
    }

    /**
     * Optional Relationships - Mandatory
     *
     * can not create an estimate without a to do
     * can create an estimate when added to a to do directly because relationship is created
     * when delete a to do the estimate is also deleted
     * GET estimates for a to do
     * GET to dos for an estimate
     * TODO: amend relationship to move estimate to another todo (implement with relationships as fields in the object e.g. "todos" : [{"guid": "xxx-xxx-xxx-xxx"}])
     * TODO: cardinality validation on relationship fields e.g. max of 2 etc.
     * TODO: create 'proposed objects' and validate those rather than create and delete (will support amend validation as well)

     */

    // can not create an estimate on its own, without a todo
    @Test
    public void canNotCreateEstimateWithoutMandatoryRelationship(){


        HttpApiRequest request = new HttpApiRequest("estimate");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        String body = "{\"duration\":\"3\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(400, response.getStatusCode());

        Assertions.assertEquals(0, todoManager.getThingInstancesNamed("estimate", EntityRelModel.DEFAULT_DATABASE_NAME).countInstances());
    }

    @Test
    public void canCreateAnEstimateForTodoMandatoryRelationship(){

        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO for estimating");


        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue() + "/estimates" );
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        String body = "{\"duration\":\"3\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(1, todoManager.getThingInstancesNamed("estimate", EntityRelModel.DEFAULT_DATABASE_NAME).countInstances());
        Assertions.assertEquals(1, atodo.getRelationships().getConnectedItems("estimates").size());

    }


    @Test
    public void canDeleteAnEstimateWhenTodoDeletedBecauseOfMandatoryRelationship(){


        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO for estimating");

        final EntityInstanceCollection estimates = todoManager.getThingInstancesNamed("estimate", EntityRelModel.DEFAULT_DATABASE_NAME);
        final EntityInstance anEstimate = estimates.addInstance(new EntityInstance(estimates.definition())).setValue("duration", "7");

        anEstimate.getRelationships().connect("estimate", atodo);

        Assertions.assertEquals(1, atodo.getRelationships().getConnectedItems("estimates").size());
        Assertions.assertEquals(1, estimates.countInstances());
        Assertions.assertEquals(1, todo.countInstances());


        final HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue());

        HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(200, response.getStatusCode());


        Assertions.assertEquals(0, todo.countInstances());
        Assertions.assertEquals(0, estimates.countInstances());

    }

    @Test
    public void canGetEstimatesViaRelationship(){


        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "a TODO for estimating");

        final EntityInstanceCollection estimates = todoManager.getThingInstancesNamed("estimate", EntityRelModel.DEFAULT_DATABASE_NAME);
        final EntityInstance anEstimate = estimates.addInstance(new EntityInstance(estimates.definition())).setValue("duration", "7").setValue("description", "an estimate");

        anEstimate.getRelationships().connect("estimate", atodo);

        Assertions.assertEquals(1, atodo.getRelationships().getConnectedItems("estimates").size());
        Assertions.assertEquals(1, estimates.countInstances());
        Assertions.assertEquals(1, todo.countInstances());


        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue() + "/estimates");

        HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());

        System.out.println(response.getBody());

        final EstimateCollectionResponse estimatesfound = new Gson().fromJson(response.getBody(), EstimateCollectionResponse.class);

        Assertions.assertEquals(1, estimatesfound.estimates.length);
        Assertions.assertEquals("7", estimatesfound.estimates[0].duration);
        Assertions.assertEquals("an estimate", estimatesfound.estimates[0].description);


        request = new HttpApiRequest("estimates/" + anEstimate.getPrimaryKeyValue() + "/estimate");

        response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());

        System.out.println(response.getBody());

        final TodoCollectionResponse todosfound = new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assertions.assertEquals(1, todosfound.todos.length);
        Assertions.assertEquals("a TODO for estimating", todosfound.todos[0].title);


    }


    private class TodoCollectionResponse {

        Todo[] todos;

    }

    private class EstimateCollectionResponse {

        Estimate[] estimates;

    }

    private class Estimate {

        String duration;
        String description;
    }

    private class Todo {

        String guid;
        String title;
    }

    private class ErrorMessages {

        String[] errorMessages;
    }
}
