package uk.co.compendiumdev.casestudy.todomanager;

import com.google.gson.*;
import org.junit.Before;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class TodoManagerApiUsage_Non_HTTP_Test {

    private Thingifier todoManager;

    /* TODO: NEXT ACTION
    to support delete I completely amended the relationship handling to have a Definition, then Vectors which describe
    the direction then instances and although all the automated assertions pass, I'm pretty sure this now has bugs and
    a lot of redundant code. Hit the code at a lower level to test and refactor
     */

    // explore the Thingifier via a todo manager case study

    /*
        Entities:
            TODO:
                - title, description, doneStatus(TRUE,FALSE)
            project:
                - title, description, completed, active

        Relationships:
            Project -> has many -> todos
            todo -> can be part of many -> project

    */



            /*

        Thinking through API

        GET todo/_GUID_   - single todo with all fields   {todo : guid = _GUID_}
        GET todo          - all todos with all fields     {todo}

        // would have to specify - couldn't get for free
        GET todo/done     - all done todos                {todo : doneStatus = TRUE}

        GET project       - all projects                  {project}
        GET project/_GUID_/todos    - all todos for the project    {project : guid = _GUID_} -> "tasks.todo"
        GET project/_GUID_/categories    - all categories for the project  {project : guid = _GUID_} -> "categories.category"

        // would have to specify couldn't get for free
        GET project/completed - all completed projects      {project : completed = TRUE}
        GET project/active    - all active projects         {project : active = TRUE}

        GET category      - all categories                      {category}
        GET category/_GUID_/todos - all todos for that category   {category : guid = _GUID_} -> "todos.todo"
        GET category/_GUID_/projects - all projects for that category {category : guid = _GUID_} -> "projects.project"

        // only PUT should allow adding/amending a GUID

        relationships woudld be

        POST todo/_GUID_/categories
        {category : [{"guid":"12345"}, {"guid":"45678"}]}
        [{"guid":"12345"}, {"guid":"45678"}]
        {"guid":"12345"}

        PUT todo/_GUID_/categories   - would make sure only thes listed categories were associated as relationship

        POST to update any field
        POST todo/_GUID_  {title : "new title", etc.}

        DELETE project/_GUID_   - delete a project

        ?title="test*" match titles - can combine with DELETE and POST to update many

        Should this come for 'free' or do we need to specify it

        e.g. THING/_GUID_/RELATIONSHIP/THING

        could implement a Thingifier URL query matcher to return instances based on query to get much of this for free


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








    /*


    Non HTTP API Based Tests


    */

    @Test
    public void NonHttpApiBasedTests() {

        // simulate a POST request

        // POST project
        Map requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");

        new Gson().toJson(requestBody);

        // create a project with POST
        ApiResponse apiresponse = todoManager.api().post("project", new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());
        String officeWorkProjectGuid = simpleJsonValueGet(apiresponse.getBody(), "project", "guid");

        ThingInstance officeWork = todoManager.findThingInstanceByGuid(officeWorkProjectGuid);

        // amend existing project with POST
        apiresponse = todoManager.api().post(String.format("project/%s", officeWorkProjectGuid), new Gson().toJson(requestBody));
        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertEquals("My Office Work", officeWork.getValue("title"));

        officeWork.setValue("title", "office");
        Assert.assertEquals("office", officeWork.getValue("title"));

        // cannot create a new project with a GUID using POST - it will be treated like an amendment without a thing
        String guid = UUID.randomUUID().toString();
        int currentProjects = todoManager.getThingNamed("project").countInstances();
        apiresponse = todoManager.api().post(String.format("project/%s", guid), new Gson().toJson(requestBody));
        Assert.assertEquals(404, apiresponse.getStatusCode());

        Assert.assertEquals(currentProjects, todoManager.getThingNamed("project").countInstances());

        // CREATE PROJECT WITH POST AND NO GUID
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "MY NEW PROJECT");

        currentProjects = todoManager.getThingNamed("project").countInstances();
        apiresponse = todoManager.api().post(String.format("project"), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        JsonObject projectjson = new JsonParser().parse(apiresponse.getBody()).getAsJsonObject();
        String projectGUID = projectjson.get("project").getAsJsonObject().get("guid").getAsString();

        ThingInstance myNewProject = todoManager.getThingNamed("project").findInstanceByField(FieldValue.is("guid", projectGUID));
        Assert.assertEquals("MY NEW PROJECT", myNewProject.getValue("title"));

        //Assert.assertEquals("My Office Work", officeWork.getValue("title"));

        Assert.assertEquals(currentProjects + 1, todoManager.getThingNamed("project").countInstances());


        // PUT

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");

        String officeWorkGuid = officeWork.getGUID();
        Assert.assertNotNull(officeWorkGuid);

        // amend existing project with PUT - this should validate that all required fields are present
        apiresponse = todoManager.api().put(String.format("project/%s", officeWork.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertEquals("My Office Work", officeWork.getValue("title"));

        officeWork.setValue("title", "office");
        Assert.assertEquals("office", officeWork.getValue("title"));
        Assert.assertNotNull(officeWorkGuid);

        // create with a PUT and a given GUID
        guid = UUID.randomUUID().toString();
        currentProjects = todoManager.getThingNamed("project").countInstances();
        apiresponse = todoManager.api().put(String.format("project/%s", guid), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(currentProjects + 1, todoManager.getThingNamed("project").countInstances());
        Assert.assertEquals("office", officeWork.getValue("title"));
        Assert.assertEquals(officeWorkGuid, officeWork.getGUID());
        ThingInstance newProject = todoManager.getThingNamed("project").findInstanceByField(FieldValue.is("guid", guid));

        Assert.assertEquals("My Office Work", newProject.getValue("title"));
        Assert.assertEquals(guid, newProject.getValue("guid"));


        // DELETE
        apiresponse = todoManager.api().delete(String.format("project/%s", officeWork.getGUID()));
        Assert.assertEquals(200, apiresponse.getStatusCode());

        Assert.assertEquals(currentProjects, todoManager.getThingNamed("project").countInstances());

        apiresponse = todoManager.api().delete(String.format("project/%s", officeWork.getGUID()));
        Assert.assertEquals(404, apiresponse.getStatusCode());


        // create a todo with POST

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Paperwork Todo");

        apiresponse = todoManager.api().post("todo", new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());
        String paperworkTodoGuid = simpleJsonValueGet(apiresponse.getBody(), "todo", "guid");

        ThingInstance paperwork = todoManager.findThingInstanceByGuid(paperworkTodoGuid);

        // Create a relationship with POST
        // POST project/_GUID_/tasks
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        requestBody = new HashMap<String, String>();
        requestBody.put("guid", paperwork.getGUID());

        int numberOfTasks = myNewProject.connectedItems("tasks").size();

        apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(numberOfTasks + 1, myNewProject.connectedItems("tasks").size());

        System.out.println(todoManager);


        // Createa a relationship and a thing with a POST and no GUID
        // POST project/_GUID_/tasks
        // {"title":"A new TODO Item related to project"}
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item related to project");

        numberOfTasks = myNewProject.connectedItems("tasks").size();

        apiresponse = todoManager.api().post(String.format("project/%s/tasks", myNewProject.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        // the created todo is in the body
        JsonObject todojson = new JsonParser().parse(apiresponse.getBody()).getAsJsonObject();
        String todoGUID = todojson.get("todo").getAsJsonObject().get("guid").getAsString();
        Assert.assertEquals("A new TODO Item related to project", todoManager.findThingInstanceByGuid(todoGUID).getValue("title"));

        Assert.assertEquals(numberOfTasks + 1, myNewProject.connectedItems("tasks").size());

        System.out.println(todoManager);


        // DELETE a Relationship
        // DELETE project/_GUID_/tasks/_GUID_
        numberOfTasks = myNewProject.connectedItems("tasks").size();

        apiresponse = todoManager.api().delete(String.format("project/%s/tasks/%s", myNewProject.getGUID(), paperwork.getGUID()));
        Assert.assertEquals(200, apiresponse.getStatusCode());

        Assert.assertEquals(numberOfTasks - 1, myNewProject.connectedItems("tasks").size());
        Assert.assertNotNull("Task should exist, only the relationship should be deleted",
                todoManager.getThingNamed("todo").findInstanceByField(FieldValue.is("guid", paperwork.getGUID())));


        System.out.println(todoManager);


        // Mandatory field validation on POST create - must have a title
        requestBody = new HashMap<String, String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");

        apiresponse = todoManager.api().post(String.format("todo"), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());

        // Mandatory field validation PUT create
        requestBody = new HashMap<String, String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", UUID.randomUUID().toString()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());

        // Mandatory field validation PUT amend
        requestBody = new HashMap<String, String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "Amended TODO Item ");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());


        // Field validation on boolean for Create with POST
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        apiresponse = todoManager.api().post(String.format("todo"), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());

        // Field validation on boolean for Amend with POST
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        apiresponse = todoManager.api().post(String.format("todo/%s", paperwork.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());


        // Field validation on boolean for Amend with PUT
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());


    }


    @Test
    public void createARelationshipUsingAPI_POST() {


        ThingInstance myNewProject = todoManager.getThingNamed("project").createInstance().setValue("title", "Project For Relationships");
        project.addInstance(myNewProject);

        ThingInstance relTodo = todoManager.getThingNamed("todo").createInstance().setValue("title", "Todo for relationship testing");
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
    public void createARelationshipUsingReversalRelationshipAPI() {


        ThingInstance myNewProject = todoManager.getThingNamed("project").createInstance().setValue("title", "Project For Relationships " + System.currentTimeMillis());
        project.addInstance(myNewProject);

        ThingInstance relTodo = todoManager.getThingNamed("todo").createInstance().setValue("title", "Todo for relationship testing " + System.currentTimeMillis());
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
        Assert.assertEquals("Expected A project", myNewProject.getGUID(), simpleJsonValueGet(apiresponse.getBody(), "projects", "0", "guid"));

        System.out.println(todoManager);

    }


    @Test
    public void deleteARelationshipUsingReversalRelationshipAPI() {


        ThingInstance myNewProject = todoManager.getThingNamed("project").createInstance().setValue("title", "Project For Relationships " + System.currentTimeMillis());
        project.addInstance(myNewProject);

        ThingInstance relTodo = todoManager.getThingNamed("todo").createInstance().setValue("title", "Todo for relationship testing " + System.currentTimeMillis());
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


        ThingInstance myNewProject = todoManager.getThingNamed("project").createInstance().setValue("title", "Project For Relationships " + System.currentTimeMillis());
        project.addInstance(myNewProject);

        ThingInstance relTodo = todoManager.getThingNamed("todo").createInstance().setValue("title", "Todo for relationship testing " + System.currentTimeMillis());
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

    // TODO: Create a thing and relate through a reverse relationship e.g. POST todo/GUID/task-of

    @Test
    public void createAReverseRelationshipAndProjectAtSameTimeUsingAPI() {


        ThingInstance relTodo = todoManager.getThingNamed("todo").createInstance().setValue("title", "Todo for relationship testing " + System.currentTimeMillis());
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

    @Test
    public void createARelationshipAndTodoAtSameTimeUsingAPI() {


        ThingInstance myNewProject = todoManager.getThingNamed("project").createInstance().setValue("title", "Project For Relationships");
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

    private String simpleJsonValueGet(String body, String... terms) {

        JsonElement obj = new JsonParser().parse(body);

        String value = "";

        for (int termId = 0; termId < terms.length; termId++) {
            if (termId < terms.length - 1) {
                if (obj.isJsonObject()) {
                    obj = obj.getAsJsonObject();
                    obj = ((JsonObject) obj).get(terms[termId]);
                } else {
                    if (obj.isJsonArray()) {
                        obj = ((JsonArray) obj).get(Integer.valueOf(terms[termId]));
                    }
                }

            } else {
                // TODO: if the final thing is an array of primitives then this won't work
                value = ((JsonObject) obj).get(terms[termId]).getAsString();
            }
        }

        return value;
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
