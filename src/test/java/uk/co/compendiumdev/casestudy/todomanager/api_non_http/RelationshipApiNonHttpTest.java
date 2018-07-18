package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import com.google.gson.*;
import org.junit.Before;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static java.util.Collections.addAll;
import static java.util.Collections.list;

public class RelationshipApiNonHttpTest {

    private Thingifier todoManager;

    /* TODO: NEXT ACTION
    to support delete I completely amended the relationship handling to have a Definition, then Vectors which describe
    the direction then instances and although all the automated assertions pass, I'm pretty sure this now has bugs and
    a lot of redundant code. Hit the code at a lower level to test and refactor
     */




    Thing todo;
    Thing project;
    Thing category;

    @Before
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");
        category = todoManager.getThingNamed("category");
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

        apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(numberOfTasks + 1, myNewProject.connectedItems("tasks").size());

        // todo should also be connected to project via the associated task-of relationship vector
        Collection<ThingInstance> projects = paperwork.connectedItems("task-of");

        Assert.assertEquals(1, projects.size());
        List<ThingInstance> listOfProjects = new ArrayList<ThingInstance>();
        listOfProjects.addAll(projects);

        Assert.assertEquals(myNewProject.getGUID(), listOfProjects.get(0).getGUID());

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

        ApiResponse apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(numberOfTasks + 1, myNewProject.connectedItems("tasks").size());

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
        Assert.assertEquals("Expected no projects", "{}", apiresponse.getBody());

        apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(1, relTodo.connectedItems("task-of").size());

        apiresponse = todoManager.api().get(String.format("todo/%s/task-of", relTodo.getGUID()));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertEquals("Expected A project", myNewProject.getGUID(), SimpleJsonValueParser.get(apiresponse.getBody(), "projects", "0", "guid"));

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

        // Create it
        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(1, relTodo.connectedItems("task-of").size());


        // Delete the relationship
        apiresponse = todoManager.api().delete(String.format("todo/%s/task-of/%s", relTodo.getGUID(), myNewProject.getGUID()));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(200, apiresponse.getStatusCode());

        // project should be related to nothing
        Collection<ThingInstance> items = myNewProject.connectedItems("tasks");
        Assert.assertEquals(0, items.size());

        // todo should be related to nothing
        items = relTodo.connectedItems("task-of");
        Assert.assertEquals(0, items.size());


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

        // Create it
        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(1, relTodo.connectedItems("task-of").size());


        // Delete the relationship
        apiresponse = todoManager.api().delete(String.format("todo/%s", relTodo.getGUID(), myNewProject.getGUID()));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(200, apiresponse.getStatusCode());

        Assert.assertEquals("Should be no stored todos", 0, todo.getInstances().size());


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

        ApiResponse apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());
        String locationGuid = apiresponse.getHeaderValue(ApiResponse.GUID_HEADER);

        Assert.assertTrue("Expected location header to contain the same GUID as the X- GUID header",
                apiresponse.getHeaderValue("Location").contains(locationGuid));

        Assert.assertEquals("Expected number of tasks in project to increase by 1",
                numberOfTasks + 1, myNewProject.connectedItems("tasks").size());

        // check todo exists
        ThingInstance myCreatedTodo = todo.findInstanceByGUID(locationGuid);
        Assert.assertEquals(expectedTitle, myCreatedTodo.getValue("title"));

        // check todo is also related to the project since relationship is two way
        Collection<ThingInstance> items = myCreatedTodo.connectedItems("task-of");
        Assert.assertEquals("Expected task be connected to only 1 project", 1, items.size());

        List<ThingInstance> itemList = new ArrayList<>();
        itemList.addAll(items);

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

        // TODO: currently not creating reverse relationships
        // Create it
        ApiResponse apiresponse = todoManager.api().post(String.format("todo/%s/task-of", relTodo.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(1, relTodo.connectedItems("task-of").size());

        ThingInstance myNewProject = project.findInstanceByGUID(apiresponse.getHeaderValue(ApiResponse.GUID_HEADER));
        Assert.assertNotNull(myNewProject);


        // project should be related to task
        Collection<ThingInstance> items = myNewProject.connectedItems("tasks");
        Assert.assertEquals(1, items.size());

        // todo should be related to project
        items = relTodo.connectedItems("task-of");
        Assert.assertEquals(1, items.size());


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
}
