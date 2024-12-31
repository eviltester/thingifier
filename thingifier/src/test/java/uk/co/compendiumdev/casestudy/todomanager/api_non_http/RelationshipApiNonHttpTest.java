package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import com.google.gson.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsXml;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

import java.util.*;

public class RelationshipApiNonHttpTest {

    private Thingifier todoManager;



    EntityInstanceCollection todo;
    EntityInstanceCollection project;
    private JsonThing jsonThing;


    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();
        jsonThing = new JsonThing(todoManager.apiConfig().jsonOutput());
        todo = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);
        project = todoManager.getThingInstancesNamed("project", EntityRelModel.DEFAULT_DATABASE_NAME);

    }


        /*
        Get todo
        Amend todo POST  /todo/guid
                        {"guid":"ab32fc3f-5dfe-4217-ac54-98ff66d66239"}
        Fail amend due to missing mandatory field
        Fail amend due to failed validation field

        404 Amend todo that does not exist
        404 amend(POST)/GET entity type does not exist e.g. bob
            - receives a generic 404 with no error message
            // TODO: investigate top level 404 handling - can we have 404 handling for "no such entity" to allow an error message in the 404

        DELETE todo

        GET Todos for a project http://localhost:4567/project/d719b9a2-c74f-4ca3-a4ca-a3fffc74cf65/tasks
        Create Todo for a project
        Fail to create todo for project - field validation

     */


    private BodyParser getSimpleParser(final Map requestBody) {

        String body = new Gson().toJson(requestBody);
        System.out.println(body);
        final HttpApiRequest arequest = new HttpApiRequest("/path").setBody(body);
        return new BodyParser(arequest, todoManager.getThingNames());
    }

    @Test
    public void getCanReturnInstancesOfARelationship(){

        ApiResponse apiresponse;


        EntityInstance paperwork = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for relating");

        EntityInstance myNewProject = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "My New Project " + System.currentTimeMillis());

        myNewProject.getRelationships().connect("tasks", paperwork);

        int numberOfTasks = myNewProject.getRelationships().getConnectedItems("tasks").size();
        Assertions.assertEquals(1, numberOfTasks);

        apiresponse = todoManager.api().get(String.format("project/%s/tasks", myNewProject.getPrimaryKeyValue()), new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertTrue(apiresponse.isCollection());
        Assertions.assertFalse(apiresponse.isErrorResponse());

        EntityInstance foundInstance = todo.findInstanceByFieldNameAndValue("guid", paperwork.getPrimaryKeyValue());
        Assertions.assertNotNull(
                foundInstance,
                "Task should exist, only the relationship should be deleted");

        Assertions.assertTrue(apiresponse.getReturnedInstanceCollection().size()==1);
        Assertions.assertEquals(foundInstance, apiresponse.getReturnedInstanceCollection().get(0));

        System.out.println(todoManager);

    }


    @Test
    public void deleteCanDeleteARelationship(){

        ApiResponse apiresponse;


        EntityInstance paperwork = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for amending");


        EntityInstance myNewProject = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "My New Project " + System.currentTimeMillis());

        myNewProject.getRelationships().connect("tasks", paperwork);

        // DELETE a Relationship
        // DELETE project/_GUID_/tasks/_GUID_
        int numberOfTasks = myNewProject.getRelationships().getConnectedItems("tasks").size();
        Assertions.assertEquals(1, numberOfTasks);

        apiresponse = todoManager.api().delete(String.format("project/%s/tasks/%s", myNewProject.getPrimaryKeyValue(), paperwork.getPrimaryKeyValue()), new HttpHeadersBlock());

        Assertions.assertEquals(200, apiresponse.getStatusCode());

        Assertions.assertEquals(numberOfTasks - 1, myNewProject.getRelationships().getConnectedItems("tasks").size());
        Assertions.assertNotNull(
                todo.findInstanceByFieldNameAndValue("guid", paperwork.getPrimaryKeyValue()),
                "Task should exist, only the relationship should be deleted");

        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertEquals("", new ApiResponseAsXml(apiresponse,jsonThing).getXml().trim(),"Should have no body");
        Assertions.assertEquals( "", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim(),"Should have no body");

        System.out.println(todoManager);

    }



    @Test
    public void deleteCanDeleteAThingInARelationship(){

        ApiResponse apiresponse;


        EntityInstance paperwork = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for amending");

        EntityInstance myNewProject = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "My New Project " + System.currentTimeMillis());


        myNewProject.getRelationships().connect("tasks", paperwork);

        // DELETE the todo
        // DELETE todo/_guid_
        int numberOfTasks = myNewProject.getRelationships().getConnectedItems("tasks").size();
        Assertions.assertEquals(1, numberOfTasks);

        apiresponse = todoManager.api().delete(String.format("todo/%s", paperwork.getPrimaryKeyValue()), new HttpHeadersBlock());

        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertFalse(apiresponse.hasABody());
        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);


        Assertions.assertEquals(numberOfTasks - 1, myNewProject.getRelationships().getConnectedItems("tasks").size());
        Assertions.assertNull(
                todo.findInstanceByFieldNameAndValue("guid", paperwork.getPrimaryKeyValue()),
                "Task should not exist");
        Assertions.assertEquals(0, todo.countInstances());
        Assertions.assertEquals(1, project.countInstances());


        System.out.println(todoManager);

    }




    @Test
    public void postCanCreateARelationship(){
        Map requestBody;
        ApiResponse apiresponse;


        EntityInstance paperwork = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for amending");

        EntityInstance myNewProject = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "My New Project " + System.currentTimeMillis());


        // Create a relationship with POST
        // POST project/_GUID_/tasks
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject

        int numberOfTasks = myNewProject.getRelationships().getConnectedItems("tasks").size();

        requestBody = new HashMap<String, String>();
        requestBody.put("guid", paperwork.getPrimaryKeyValue());

        apiresponse = todoManager.api().post(String.format("project/%s/tasks",
                                            myNewProject.getPrimaryKeyValue()),
                                            getSimpleParser(requestBody), new HttpHeadersBlock());

        Assertions.assertEquals(201, apiresponse.getStatusCode());

        Assertions.assertEquals(numberOfTasks + 1, myNewProject.getRelationships().getConnectedItems("tasks").size());

        // todo should also be connected to project via the associated task-of relationship vector
        Collection<EntityInstance> projects = paperwork.getRelationships().getConnectedItems("task-of");

        Assertions.assertEquals(1, projects.size());
        List<EntityInstance> listOfProjects = new ArrayList<EntityInstance>(projects);

        Assertions.assertEquals(myNewProject.getPrimaryKeyValue(), listOfProjects.get(0).getPrimaryKeyValue());

        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertEquals( "", new ApiResponseAsXml(apiresponse, jsonThing).getXml().trim(),"Should have no body");
        Assertions.assertEquals("", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim(),"Should have no body");

        System.out.println(todoManager);
    }



    @Test
    public void postCanCreateARelationshipUsingAPI() {


        EntityInstance myNewProject = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "Project For Relationships");

        EntityInstance relTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for relationship testing");


        // Create a relationship with POST
        // POST project/_GUID_/tasks
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        HashMap<String, String> requestBody = new HashMap<String, String>();
        requestBody.put("guid", relTodo.getPrimaryKeyValue());

        int numberOfTasks = myNewProject.getRelationships().getConnectedItems("tasks").size();

        ApiResponse apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());

        Assertions.assertEquals(201, apiresponse.getStatusCode());

        Assertions.assertEquals(numberOfTasks + 1, myNewProject.getRelationships().getConnectedItems("tasks").size());

        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertEquals( "", new ApiResponseAsXml(apiresponse, jsonThing).getXml().trim(),"Should have no body");
        Assertions.assertEquals( "", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim(),"Should have no body");

        System.out.println(todoManager);

    }

    @Test
    public void postCanCreateARelationshipUsingReversalRelationshipAPI() {


        EntityInstance myNewProject = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "Project For Relationships " + System.currentTimeMillis());


        EntityInstance relTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for relationship testing " + System.currentTimeMillis());

        // Create a relationship with POST
        // POST todo/_GUID_/task-of
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        HashMap<String, String> requestBody = new HashMap<String, String>();
        requestBody.put("guid", myNewProject.getPrimaryKeyValue());

        int numberOfProjects = relTodo.getRelationships().getConnectedItems("task-of").size();
        Assertions.assertEquals(0, numberOfProjects);

        // get current related projects through api
        ApiResponse apiresponse = todoManager.api().get(String.format("todo/%s/task-of", relTodo.getPrimaryKeyValue()), new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals(0, apiresponse.getReturnedInstanceCollection().size());
        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertTrue(new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim().contains("[]"),
                "Should have no array content");
        Assertions.assertTrue(new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim().startsWith("{\"projects\":"),
                "Should have name of thing");






        apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(201, apiresponse.getStatusCode());

        Assertions.assertEquals(1, relTodo.getRelationships().getConnectedItems("task-of").size());
        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertEquals( "", new ApiResponseAsXml(apiresponse, jsonThing).getXml().trim(),"Should have no body");
        Assertions.assertEquals( "", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim(),"Should have no body");






        apiresponse = todoManager.api().get(String.format("todo/%s/task-of", relTodo.getPrimaryKeyValue()), new QueryFilterParams(), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());

        Assertions.assertEquals(1, apiresponse.getReturnedInstanceCollection().size());
        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertTrue(apiresponse.hasABody());

        Assertions.assertEquals(
                myNewProject.getPrimaryKeyValue(),
                apiresponse.getReturnedInstanceCollection().get(0).getPrimaryKeyValue(),
                "Expected A project");

        System.out.println(todoManager);

    }


    @Test
    public void deleteARelationshipUsingReversalRelationshipAPI() {


        EntityInstance myNewProject = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "Project For Relationships " + System.currentTimeMillis());

        EntityInstance relTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for relationship testing " + System.currentTimeMillis());

        // Create a relationship with POST
        // POST todo/_GUID_/task-of
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        HashMap<String, String> requestBody = new HashMap<String, String>();
        requestBody.put("guid", myNewProject.getPrimaryKeyValue());

        int numberOfProjects = relTodo.getRelationships().getConnectedItems("task-of").size();
        Assertions.assertEquals(0, numberOfProjects);

        // Create a relationship
        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getPrimaryKeyValue()),getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(201, apiresponse.getStatusCode());

        Assertions.assertEquals(1, relTodo.getRelationships().getConnectedItems("task-of").size());

        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertFalse(apiresponse.hasABody());
        Assertions.assertEquals("", new ApiResponseAsXml(apiresponse,jsonThing).getXml().trim());
        Assertions.assertEquals("", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim(),"Should have no body");


        // Delete the relationship
        apiresponse = todoManager.api().delete(String.format("todo/%s/task-of/%s", relTodo.getPrimaryKeyValue(), myNewProject.getPrimaryKeyValue()), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());

        // project should be related to nothing
        Collection<EntityInstance> items = myNewProject.getRelationships().getConnectedItems("tasks");
        Assertions.assertEquals(0, items.size());

        // todo should be related to nothing
        items = relTodo.getRelationships().getConnectedItems("task-of");
        Assertions.assertEquals(0, items.size());

        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertFalse(apiresponse.hasABody());
        Assertions.assertEquals( "", new ApiResponseAsXml(apiresponse, jsonThing).getXml().trim(), "Should have no body");
        Assertions.assertEquals("", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim(), "Should have no body");

        System.out.println(todoManager);

    }


    // Delete a thing in a reversable relationship and ensure relationship is deleted
    @Test
    public void deleteAThingInAReversalRelationship() {


        EntityInstance myNewProject = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "Project For Relationships " + System.currentTimeMillis());

        EntityInstance relTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for relationship testing " + System.currentTimeMillis());

        // Create a relationship with POST
        // POST todo/_GUID_/task-of
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        HashMap<String, String> requestBody = new HashMap<String, String>();
        requestBody.put("guid", myNewProject.getPrimaryKeyValue());

        int numberOfProjects = relTodo.getRelationships().getConnectedItems("task-of").size();
        Assertions.assertEquals(0, numberOfProjects);

        // Create relationship
        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(201, apiresponse.getStatusCode());

        Assertions.assertEquals(1, relTodo.getRelationships().getConnectedItems("task-of").size());
        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertFalse(apiresponse.hasABody());
        Assertions.assertEquals( "", new ApiResponseAsXml(apiresponse, jsonThing).getXml().trim(), "Should have no body");
        Assertions.assertEquals("", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim(), "Should have no body");


        // Delete the relationship
        apiresponse = todoManager.api().delete(String.format("todo/%s", relTodo.getPrimaryKeyValue()), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());

        Assertions.assertEquals(0, todo.getInstances().size(),"Should be no stored todos");
        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertFalse(apiresponse.hasABody());
        Assertions.assertEquals( "", new ApiResponseAsXml(apiresponse, jsonThing).getXml().trim(), "Should have no body");
        Assertions.assertEquals( "", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim(), "Should have no body");



        // project should be related to nothing
        Collection<EntityInstance> items = myNewProject.getRelationships().getConnectedItems("tasks");
        Assertions.assertEquals(0, items.size());

        // todo should be related to nothing
        items = relTodo.getRelationships().getConnectedItems("task-of");
        Assertions.assertEquals(0, items.size());


        System.out.println(todoManager);

    }


    @Test
    public void postCanCreateARelationshipAndTodoAtSameTimeUsingAPI() {


        EntityInstance myNewProject = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "Project For Relationships");


        // Createa a relationship and a thing with a POST and no GUID
        // POST project/_GUID_/tasks
        // {"title":"A new TODO Item related to project"}
        HashMap<String, String> requestBody = new HashMap<String, String>();
        String expectedTitle = "A new TODO Item related to project " + System.currentTimeMillis();

        requestBody.put("title", expectedTitle);

        int numberOfTasks = myNewProject.getRelationships().getConnectedItems("tasks").size();

        ApiResponse apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(201, apiresponse.getStatusCode());
        String locationGuid = apiresponse.getHeaderValue(ApiResponse.PRIMARY_KEY_HEADER);
        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);

        Assertions.assertTrue(
                apiresponse.getHeaderValue("Location").contains(locationGuid),
                "Expected location header to contain the same GUID as the X- GUID header");

        Assertions.assertEquals(
                numberOfTasks + 1, myNewProject.getRelationships().getConnectedItems("tasks").size(),
                "Expected number of tasks in project to increase by 1");

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertNotEquals("Should have a body", "", new ApiResponseAsXml(apiresponse, jsonThing).getXml().trim());
        Assertions.assertNotEquals("Should have a body", "", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim());

        Assertions.assertFalse(apiresponse.isCollection());



        // check todo exists
        EntityInstance myCreatedTodo = todo.findInstanceByPrimaryKey(locationGuid);
        Assertions.assertEquals(expectedTitle, myCreatedTodo.getFieldValue("title").asString());

        // check todo is also related to the project since relationship is two way
        Collection<EntityInstance> items = myCreatedTodo.getRelationships().getConnectedItems("task-of");
        Assertions.assertEquals(1, items.size(),
                "Expected task be connected to only 1 project");

        List<EntityInstance> itemList = new ArrayList<>(items);

        // item should be myNewProject
        Assertions.assertEquals( myNewProject.getPrimaryKeyValue(), itemList.get(0).getPrimaryKeyValue(),"Expected to be connected to project");


    }

    @Test
    public void postCanCreateAReverseRelationshipAndProjectAtSameTimeUsingAPI() {

        // Create a thing and relate through a reverse relationship e.g. POST todo/GUID/task-of

        EntityInstance relTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for relationship testing " + System.currentTimeMillis());


        // Createa a relationship and a thing with a POST and no GUID
        // POST project/_GUID_/tasks
        // {"title":"A new TODO Item related to project"}
        HashMap<String, String> requestBody = new HashMap<String, String>();
        String expectedTitle = "A new project related to the task " + System.currentTimeMillis();

        requestBody.put("title", expectedTitle);


        int numberOfProjects = relTodo.getRelationships().getConnectedItems("task-of").size();
        Assertions.assertEquals(0, numberOfProjects);

        // Create it
        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(201, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assertions.assertEquals(1, relTodo.getRelationships().getConnectedItems("task-of").size());

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertNotEquals("Should have a body", "", new ApiResponseAsXml(apiresponse, jsonThing).getXml().trim());
        Assertions.assertNotEquals("Should have a body", "", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim());
        Assertions.assertFalse(apiresponse.isCollection());

        EntityInstance myNewProject = project.findInstanceByPrimaryKey(apiresponse.getHeaderValue(ApiResponse.PRIMARY_KEY_HEADER));
        Assertions.assertNotNull(myNewProject);


        // project should be related to task
        Collection<EntityInstance> items = myNewProject.getRelationships().getConnectedItems("tasks");
        Assertions.assertEquals(1, items.size());

        // a todo instance should be related to project
        items = relTodo.getRelationships().getConnectedItems("task-of");
        Assertions.assertEquals(1, items.size());


    }


    // TODO rest api needs to enforce optionality of relationships during creation of entities
    // at REST API level
    // DONE: create todo/estimates should create the item and the relationships so should validate
    // DONE: create /estimate should fail because there is no relationship
    // TODO: currently no way to amend a relationship and move something to something else this would have to be a PUT
    //     - but we currently have no way to amend relationships in the body of a message
    // TODO: currently no way to create an entity with multiple relationships
    //     - need to have a way to define relationships in the body of the message
    // TODO: add http tests for optional relationships


    @Test
    public void postCanCreateAMandatoryRelationshipFromEstimateAndTodoAtSameTimeUsingAPI() {


        EntityInstance myTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "an estimated todo");

        final EntityInstanceCollection estimates = todoManager.getThingInstancesNamed("estimate", EntityRelModel.DEFAULT_DATABASE_NAME);
        int numberOfEstimates = estimates.countInstances();
        Assertions.assertEquals(0, numberOfEstimates );



        // Createa a relationship and a thing with a POST and no GUID
        // POST todos/_GUID_/estimates
        // {"duration":"3", "description", "a test estimate xxxxxxxx"}
        HashMap<String, String> requestBody = new HashMap<String, String>();
        String expectedDescription = "a test estimate " + System.currentTimeMillis();

        requestBody.put("description", expectedDescription);
        requestBody.put("duration", "3");

        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/estimates", myTodo.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(201, apiresponse.getStatusCode());
        String locationGuid = apiresponse.getHeaderValue(ApiResponse.PRIMARY_KEY_HEADER);
        Assertions.assertTrue(apiresponse.getErrorMessages().size()==0);

        Assertions.assertTrue(
                apiresponse.getHeaderValue("Location").contains(locationGuid),
                "Expected location header to contain the same GUID as the X- GUID header");

        Assertions.assertEquals(
                numberOfEstimates + 1, estimates.countInstances(),
                "Expected number of estimates in project to increase by 1");

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertNotEquals("Should have a body", "", new ApiResponseAsXml(apiresponse, jsonThing).getXml().trim());
        Assertions.assertNotEquals("Should have a body", "", new ApiResponseAsJson(apiresponse, jsonThing).getJson().trim());

        Assertions.assertFalse(apiresponse.isCollection());



        // check estimate exists
        EntityInstance myCreatedItem = estimates.findInstanceByPrimaryKey(locationGuid);
        Assertions.assertEquals(expectedDescription, myCreatedItem.getFieldValue("description").asString());
        Assertions.assertEquals("3", myCreatedItem.getFieldValue("duration").asString());

        // check estimate is related to the todo since relationship is two way
        Collection<EntityInstance> items = myCreatedItem.getRelationships().getConnectedItems("estimate");
        Assertions.assertEquals( 1, items.size(), "Expected estimate to be connected to only 1 todo");
        Assertions.assertTrue(items.contains(myTodo));

        // check todo also recognises the relationship
        items = myTodo.getRelationships().getConnectedItems("estimates");
        Assertions.assertEquals( 1, items.size(), "Expected todo to be connected to only 1 estimate");
        Assertions.assertTrue(items.contains(myCreatedItem));

    }

    @Test
    public void postCanNotCreateEstimateWithoutAMandatoryRelationshipUsingAPI() {

        EntityInstance myTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "an estimated todo");

        final EntityInstanceCollection estimates = todoManager.getThingInstancesNamed("estimate", EntityRelModel.DEFAULT_DATABASE_NAME);
        int numberOfEstimates = estimates.countInstances();
        Assertions.assertEquals(0, numberOfEstimates );


        HashMap<String, String> requestBody = new HashMap<String, String>();
        String expectedDescription = "a test estimate " + System.currentTimeMillis();

        requestBody.put("description", expectedDescription);
        requestBody.put("duration", "3");

        ApiResponse apiresponse = todoManager.api().post("estimate", getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());

        Assertions.assertEquals(
                numberOfEstimates, estimates.countInstances(),
                "Expected number of estimates in project to not increase");

    }

    @Test
    public void postCanCreateEstimateAMandatoryRelationshipUsingAPI() {

        EntityInstance myTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title",
                                    "an estimated todo");

        // todo has no estimates
        Assertions.assertEquals(
                0, myTodo.getRelationships().getConnectedItems("estimates").size());

        // there are no estimates at all
        final EntityInstanceCollection estimates = todoManager.getThingInstancesNamed("estimate", EntityRelModel.DEFAULT_DATABASE_NAME);
        int numberOfEstimates = estimates.countInstances();
        Assertions.assertEquals(0, numberOfEstimates );


        HashMap<String, Object> requestBody = new HashMap<String, Object>();
        String expectedDescription = "a test estimate " + System.currentTimeMillis();

        requestBody.put("description", expectedDescription);
        requestBody.put("duration", "3");

        // relationship to request
        // estimate: [{"guid", "..."}]
        List<HashMap> estimateTodoGuids = new ArrayList<>();
        final HashMap<String, String > todoGuid = new HashMap<>();
        todoGuid.put("guid",myTodo.getPrimaryKeyValue());
        estimateTodoGuids.add(todoGuid);
        requestBody.put("estimate", estimateTodoGuids);

        ApiResponse apiresponse = todoManager.api().post("estimate", getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(201, apiresponse.getStatusCode());

        Assertions.assertEquals(
                numberOfEstimates+1, estimates.countInstances(),
                "Expected number of estimates in project to increase");

        // todo now has an estimate
        Assertions.assertEquals(
                1, myTodo.getRelationships().getConnectedItems("estimates").size());

        // and it is the estimate we expected
        final ArrayList<EntityInstance> estimatesList = new ArrayList();
        estimatesList.addAll(myTodo.getRelationships().getConnectedItems("estimates"));
        Assertions.assertEquals(expectedDescription, estimatesList.get(0).getFieldValue("description").asString());

    }

    // TODO: cardinality is enforced so this would not be valid because it has multiple todos in the estimate relationship
    // "<estimate><duration>5</duration><estimate><todo><guid>1234567890</guid></todo><todo><guid>999991234567890</guid></todo></estimate></estimate>"

}
