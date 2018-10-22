package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import com.google.gson.*;
import org.junit.Before;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsXml;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class RelationshipApiNonHttpTest {

    private Thingifier todoManager;



    Thing todo;
    Thing project;


    @Before
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");

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

        final HttpApiRequest arequest = new HttpApiRequest("/path").setBody(new Gson().toJson(requestBody));
        return new BodyParser(arequest, todoManager.getThingNames());
    }

    @Test
    public void getCanReturnInstancesOfARelationship(){

        ApiResponse apiresponse;


        ThingInstance paperwork = todo.createInstance().setValue("title", "Todo for relating");
        todo.addInstance(paperwork);

        ThingInstance myNewProject = project.createInstance().setValue("title", "My New Project " + System.currentTimeMillis());
        project.addInstance(myNewProject);

        myNewProject.connects("tasks", paperwork);

        int numberOfTasks = myNewProject.connectedItems("tasks").size();
        Assert.assertEquals(1, numberOfTasks);

        apiresponse = todoManager.api().get(String.format("project/%s/tasks", myNewProject.getGUID()));

        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.hasABody());
        Assert.assertTrue(apiresponse.isCollection());
        Assert.assertFalse(apiresponse.isErrorResponse());

        ThingInstance foundInstance = todo.findInstanceByField(FieldValue.is("guid", paperwork.getGUID()));
        Assert.assertNotNull("Task should exist, only the relationship should be deleted",
                foundInstance);

        Assert.assertTrue(apiresponse.getReturnedInstanceCollection().size()==1);
        Assert.assertEquals(foundInstance, apiresponse.getReturnedInstanceCollection().get(0));

        System.out.println(todoManager);

    }


    @Test
    public void deleteCanDeleteARelationship(){

        ApiResponse apiresponse;


        ThingInstance paperwork = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(paperwork);

        ThingInstance myNewProject = project.createInstance().setValue("title", "My New Project " + System.currentTimeMillis());
        project.addInstance(myNewProject);

        myNewProject.connects("tasks", paperwork);

        // DELETE a Relationship
        // DELETE project/_GUID_/tasks/_GUID_
        int numberOfTasks = myNewProject.connectedItems("tasks").size();
        Assert.assertEquals(1, numberOfTasks);

        apiresponse = todoManager.api().delete(String.format("project/%s/tasks/%s", myNewProject.getGUID(), paperwork.getGUID()));

        Assert.assertEquals(200, apiresponse.getStatusCode());

        Assert.assertEquals(numberOfTasks - 1, myNewProject.connectedItems("tasks").size());
        Assert.assertNotNull("Task should exist, only the relationship should be deleted",
                todo.findInstanceByField(FieldValue.is("guid", paperwork.getGUID())));

        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertEquals("Should have no body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsJson(apiresponse).getJson().trim());

        System.out.println(todoManager);

    }



    @Test
    public void deleteCanDeleteAThingInARelationship(){

        ApiResponse apiresponse;


        ThingInstance paperwork = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(paperwork);

        ThingInstance myNewProject = project.createInstance().setValue("title", "My New Project " + System.currentTimeMillis());
        project.addInstance(myNewProject);

        myNewProject.connects("tasks", paperwork);

        // DELETE the todo
        // DELETE todo/_guid_
        int numberOfTasks = myNewProject.connectedItems("tasks").size();
        Assert.assertEquals(1, numberOfTasks);

        apiresponse = todoManager.api().delete(String.format("todo/%s", paperwork.getGUID()));

        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertFalse(apiresponse.hasABody());
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);


        Assert.assertEquals(numberOfTasks - 1, myNewProject.connectedItems("tasks").size());
        Assert.assertNull("Task should not exist",
                todo.findInstanceByField(FieldValue.is("guid", paperwork.getGUID())));
        Assert.assertEquals(0, todo.countInstances());
        Assert.assertEquals(1, project.countInstances());


        System.out.println(todoManager);

    }




    @Test
    public void postCanCreateARelationship(){
        Map requestBody;
        ApiResponse apiresponse;


        ThingInstance paperwork = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(paperwork);

        ThingInstance myNewProject = project.createInstance().setValue("title", "My New Project " + System.currentTimeMillis());
        project.addInstance(myNewProject);


        // Create a relationship with POST
        // POST project/_GUID_/tasks
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject

        int numberOfTasks = myNewProject.connectedItems("tasks").size();

        requestBody = new HashMap<String, String>();
        requestBody.put("guid", paperwork.getGUID());

        apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getGUID()), getSimpleParser(requestBody));

        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(numberOfTasks + 1, myNewProject.connectedItems("tasks").size());

        // todo should also be connected to project via the associated task-of relationship vector
        Collection<ThingInstance> projects = paperwork.connectedItems("task-of");

        Assert.assertEquals(1, projects.size());
        List<ThingInstance> listOfProjects = new ArrayList<ThingInstance>(projects);

        Assert.assertEquals(myNewProject.getGUID(), listOfProjects.get(0).getGUID());

        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertEquals("Should have no body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsJson(apiresponse).getJson().trim());

        System.out.println(todoManager);
    }



    @Test
    public void postCanCreateARelationshipUsingAPI() {


        ThingInstance myNewProject = project.createInstance().setValue("title", "Project For Relationships");
        project.addInstance(myNewProject);

        ThingInstance relTodo = todo.createInstance().setValue("title", "Todo for relationship testing");
        todo.addInstance(relTodo);

        // Create a relationship with POST
        // POST project/_GUID_/tasks
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        HashMap<String, String> requestBody = new HashMap<String, String>();
        requestBody.put("guid", relTodo.getGUID());

        int numberOfTasks = myNewProject.connectedItems("tasks").size();

        ApiResponse apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getGUID()), getSimpleParser(requestBody));

        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(numberOfTasks + 1, myNewProject.connectedItems("tasks").size());

        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertEquals("Should have no body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsJson(apiresponse).getJson().trim());

        System.out.println(todoManager);

    }

    @Test
    public void postCanCreateARelationshipUsingReversalRelationshipAPI() {


        ThingInstance myNewProject = project.createInstance().setValue("title", "Project For Relationships " + System.currentTimeMillis());
        project.addInstance(myNewProject);

        ThingInstance relTodo = todo.createInstance().setValue("title", "Todo for relationship testing " + System.currentTimeMillis());
        todo.addInstance(relTodo);

        // Create a relationship with POST
        // POST todo/_GUID_/task-of
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        HashMap<String, String> requestBody = new HashMap<String, String>();
        requestBody.put("guid", myNewProject.getGUID());

        int numberOfProjects = relTodo.connectedItems("task-of").size();
        Assert.assertEquals(0, numberOfProjects);

        // get current related projects through api
        ApiResponse apiresponse = todoManager.api().get(String.format("todo/%s/task-of", relTodo.getGUID()));

        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertEquals(0, apiresponse.getReturnedInstanceCollection().size());
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertTrue(apiresponse.hasABody());
        Assert.assertTrue("Should have no array content", new ApiResponseAsJson(apiresponse).getJson().trim().contains("[]"));
        Assert.assertTrue("Should have name of thing",  new ApiResponseAsJson(apiresponse).getJson().trim().startsWith("{\"projects\":"));






        apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getGUID()), getSimpleParser(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(1, relTodo.connectedItems("task-of").size());
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertEquals("Should have no body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsJson(apiresponse).getJson().trim());






        apiresponse = todoManager.api().get(String.format("todo/%s/task-of", relTodo.getGUID()));
        Assert.assertEquals(200, apiresponse.getStatusCode());

        Assert.assertEquals(1, apiresponse.getReturnedInstanceCollection().size());
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertTrue(apiresponse.hasABody());




        Assert.assertEquals("Expected A project", myNewProject.getGUID(), apiresponse.getReturnedInstanceCollection().get(0).getGUID());

        System.out.println(todoManager);

    }


    @Test
    public void deleteARelationshipUsingReversalRelationshipAPI() {


        ThingInstance myNewProject = project.createInstance().setValue("title", "Project For Relationships " + System.currentTimeMillis());
        project.addInstance(myNewProject);

        ThingInstance relTodo = todo.createInstance().setValue("title", "Todo for relationship testing " + System.currentTimeMillis());
        todo.addInstance(relTodo);

        // Create a relationship with POST
        // POST todo/_GUID_/task-of
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        HashMap<String, String> requestBody = new HashMap<String, String>();
        requestBody.put("guid", myNewProject.getGUID());

        int numberOfProjects = relTodo.connectedItems("task-of").size();
        Assert.assertEquals(0, numberOfProjects);

        // Create a relationship
        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getGUID()),getSimpleParser(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(1, relTodo.connectedItems("task-of").size());

        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertFalse(apiresponse.hasABody());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsJson(apiresponse).getJson().trim());


        // Delete the relationship
        apiresponse = todoManager.api().delete(String.format("todo/%s/task-of/%s", relTodo.getGUID(), myNewProject.getGUID()));
        Assert.assertEquals(200, apiresponse.getStatusCode());

        // project should be related to nothing
        Collection<ThingInstance> items = myNewProject.connectedItems("tasks");
        Assert.assertEquals(0, items.size());

        // todo should be related to nothing
        items = relTodo.connectedItems("task-of");
        Assert.assertEquals(0, items.size());

        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertFalse(apiresponse.hasABody());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsJson(apiresponse).getJson().trim());

        System.out.println(todoManager);

    }


    // Delete a thing in a reversable relationship and ensure relationship is deleted
    @Test
    public void deleteAThingInAReversalRelationship() {


        ThingInstance myNewProject = project.createInstance().setValue("title", "Project For Relationships " + System.currentTimeMillis());
        project.addInstance(myNewProject);

        ThingInstance relTodo = todo.createInstance().setValue("title", "Todo for relationship testing " + System.currentTimeMillis());
        todo.addInstance(relTodo);

        // Create a relationship with POST
        // POST todo/_GUID_/task-of
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        HashMap<String, String> requestBody = new HashMap<String, String>();
        requestBody.put("guid", myNewProject.getGUID());

        int numberOfProjects = relTodo.connectedItems("task-of").size();
        Assert.assertEquals(0, numberOfProjects);

        // Create relationship
        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getGUID()), getSimpleParser(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(1, relTodo.connectedItems("task-of").size());
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertFalse(apiresponse.hasABody());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsJson(apiresponse).getJson().trim());


        // Delete the relationship
        apiresponse = todoManager.api().delete(String.format("todo/%s", relTodo.getGUID()));
        Assert.assertEquals(200, apiresponse.getStatusCode());

        Assert.assertEquals("Should be no stored todos", 0, todo.getInstances().size());
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertFalse(apiresponse.hasABody());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertEquals("Should have no body", "", new ApiResponseAsJson(apiresponse).getJson().trim());



        // project should be related to nothing
        Collection<ThingInstance> items = myNewProject.connectedItems("tasks");
        Assert.assertEquals(0, items.size());

        // todo should be related to nothing
        items = relTodo.connectedItems("task-of");
        Assert.assertEquals(0, items.size());


        System.out.println(todoManager);

    }


    @Test
    public void postCanCreateARelationshipAndTodoAtSameTimeUsingAPI() {


        ThingInstance myNewProject = project.createInstance().setValue("title", "Project For Relationships");
        project.addInstance(myNewProject);


        // Createa a relationship and a thing with a POST and no GUID
        // POST project/_GUID_/tasks
        // {"title":"A new TODO Item related to project"}
        HashMap<String, String> requestBody = new HashMap<String, String>();
        String expectedTitle = "A new TODO Item related to project " + System.currentTimeMillis();

        requestBody.put("title", expectedTitle);

        int numberOfTasks = myNewProject.connectedItems("tasks").size();

        ApiResponse apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getGUID()), getSimpleParser(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());
        String locationGuid = apiresponse.getHeaderValue(ApiResponse.GUID_HEADER);
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);

        Assert.assertTrue("Expected location header to contain the same GUID as the X- GUID header",
                apiresponse.getHeaderValue("Location").contains(locationGuid));

        Assert.assertEquals("Expected number of tasks in project to increase by 1",
                numberOfTasks + 1, myNewProject.connectedItems("tasks").size());

        Assert.assertTrue(apiresponse.hasABody());
        Assert.assertNotEquals("Should have a body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertNotEquals("Should have a body", "", new ApiResponseAsJson(apiresponse).getJson().trim());

        Assert.assertFalse(apiresponse.isCollection());



        // check todo exists
        ThingInstance myCreatedTodo = todo.findInstanceByGUID(locationGuid);
        Assert.assertEquals(expectedTitle, myCreatedTodo.getValue("title"));

        // check todo is also related to the project since relationship is two way
        Collection<ThingInstance> items = myCreatedTodo.connectedItems("task-of");
        Assert.assertEquals("Expected task be connected to only 1 project", 1, items.size());

        List<ThingInstance> itemList = new ArrayList<>(items);

        // item should be myNewProject
        Assert.assertEquals("Expected to be connected to project", myNewProject.getGUID(), itemList.get(0).getGUID());


    }

    @Test
    public void postCanCreateAReverseRelationshipAndProjectAtSameTimeUsingAPI() {

        // Create a thing and relate through a reverse relationship e.g. POST todo/GUID/task-of

        ThingInstance relTodo = todo.createInstance().setValue("title", "Todo for relationship testing " + System.currentTimeMillis());
        todo.addInstance(relTodo);


        // Createa a relationship and a thing with a POST and no GUID
        // POST project/_GUID_/tasks
        // {"title":"A new TODO Item related to project"}
        HashMap<String, String> requestBody = new HashMap<String, String>();
        String expectedTitle = "A new project related to the task " + System.currentTimeMillis();

        requestBody.put("title", expectedTitle);


        int numberOfProjects = relTodo.connectedItems("task-of").size();
        Assert.assertEquals(0, numberOfProjects);

        // Create it
        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getGUID()), getSimpleParser(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);
        Assert.assertEquals(1, relTodo.connectedItems("task-of").size());

        Assert.assertTrue(apiresponse.hasABody());
        Assert.assertNotEquals("Should have a body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertNotEquals("Should have a body", "", new ApiResponseAsJson(apiresponse).getJson().trim());
        Assert.assertFalse(apiresponse.isCollection());

        ThingInstance myNewProject = project.findInstanceByGUID(apiresponse.getHeaderValue(ApiResponse.GUID_HEADER));
        Assert.assertNotNull(myNewProject);


        // project should be related to task
        Collection<ThingInstance> items = myNewProject.connectedItems("tasks");
        Assert.assertEquals(1, items.size());

        // a todo instance should be related to project
        items = relTodo.connectedItems("task-of");
        Assert.assertEquals(1, items.size());


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



        ThingInstance myTodo = todo.createInstance().setValue("title", "an estimated todo");
        todo.addInstance(myTodo);

        final Thing estimates = todoManager.getThingNamed("estimate");
        int numberOfEstimates = estimates.countInstances();
        Assert.assertEquals(0, numberOfEstimates );



        // Createa a relationship and a thing with a POST and no GUID
        // POST todos/_GUID_/estimates
        // {"duration":"3", "description", "a test estimate xxxxxxxx"}
        HashMap<String, String> requestBody = new HashMap<String, String>();
        String expectedDescription = "a test estimate " + System.currentTimeMillis();

        requestBody.put("description", expectedDescription);
        requestBody.put("duration", "3");

        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/estimates", myTodo.getGUID()), getSimpleParser(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());
        String locationGuid = apiresponse.getHeaderValue(ApiResponse.GUID_HEADER);
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);

        Assert.assertTrue("Expected location header to contain the same GUID as the X- GUID header",
                apiresponse.getHeaderValue("Location").contains(locationGuid));

        Assert.assertEquals("Expected number of estimates in project to increase by 1",
                numberOfEstimates + 1, estimates.countInstances());

        Assert.assertTrue(apiresponse.hasABody());
        Assert.assertNotEquals("Should have a body", "", new ApiResponseAsXml(apiresponse).getXml().trim());
        Assert.assertNotEquals("Should have a body", "", new ApiResponseAsJson(apiresponse).getJson().trim());

        Assert.assertFalse(apiresponse.isCollection());



        // check estimate exists
        ThingInstance myCreatedItem = estimates.findInstanceByGUID(locationGuid);
        Assert.assertEquals(expectedDescription, myCreatedItem.getValue("description"));
        Assert.assertEquals("3", myCreatedItem.getValue("duration"));

        // check estimate is related to the todo since relationship is two way
        Collection<ThingInstance> items = myCreatedItem.connectedItems("estimate");
        Assert.assertEquals("Expected estimate to be connected to only 1 todo", 1, items.size());
        Assert.assertTrue(items.contains(myTodo));

        // check todo also recognises the relationship
        items = myTodo.connectedItems("estimates");
        Assert.assertEquals("Expected todo to be connected to only 1 estimate", 1, items.size());
        Assert.assertTrue(items.contains(myCreatedItem));

    }

    @Test
    public void postCanNotCreateEstimateWithoutAMandatoryRelationshipUsingAPI() {



        ThingInstance myTodo = todo.createInstance().setValue("title", "an estimated todo");
        todo.addInstance(myTodo);

        final Thing estimates = todoManager.getThingNamed("estimate");
        int numberOfEstimates = estimates.countInstances();
        Assert.assertEquals(0, numberOfEstimates );



        // Createa a relationship and a thing with a POST and no GUID
        // POST todos/_GUID_/estimates
        // {"duration":"3", "description", "a test estimate xxxxxxxx"}
        HashMap<String, String> requestBody = new HashMap<String, String>();
        String expectedDescription = "a test estimate " + System.currentTimeMillis();

        requestBody.put("description", expectedDescription);
        requestBody.put("duration", "3");

        ApiResponse apiresponse = todoManager.api().post(String.format("estimate", myTodo.getGUID()), getSimpleParser(requestBody));
        Assert.assertEquals(400, apiresponse.getStatusCode());

        Assert.assertEquals("Expected number of estimates in project to not increase",
                numberOfEstimates, estimates.countInstances());



    }
}
