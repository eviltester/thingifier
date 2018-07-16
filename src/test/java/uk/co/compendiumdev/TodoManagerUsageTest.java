package uk.co.compendiumdev;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.AndCall;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.Between;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.WithCardinality;
import uk.co.compendiumdev.thingifier.generic.instances.RelationshipInstance;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static uk.co.compendiumdev.thingifier.generic.FieldType.STRING;

public class TodoManagerUsageTest {

    private static Thingifier todoManager;

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


    @BeforeClass
    public static void createDefinitions(){

        todoManager = new Thingifier();

        todoManager.setDocumentation("Todo Manager", "A Simple todo manager");

        Thing todo = todoManager.createThing("todo", "todos");

        todo.definition()
                .addFields( Field.is("title", STRING).
                                mandatory().
                                withValidation(
                                        VRule.NotEmpty(),
                                        VRule.MatchesType()),
                        Field.is("description",STRING),
                        Field.is("doneStatus",FieldType.BOOLEAN).
                                withDefaultValue("FALSE").
                                withValidation(
                                        VRule.MatchesType()))
        ;


        Thing project = todoManager.createThing("project", "projects");

        project.definition()
                .addFields(
                        Field.is("title", STRING),
                        Field.is("description",STRING),
                        Field.is("completed",FieldType.BOOLEAN).
                                withDefaultValue("FALSE").
                                withValidation(VRule.MatchesType()),
                        Field.is("active",FieldType.BOOLEAN).
                                withDefaultValue("TRUE").
                                withValidation(VRule.MatchesType()));


        Thing category = todoManager.createThing("category", "categories");

        category.definition()
                .addFields(
                        Field.is("title", STRING).
                                mandatory().
                                withValidation(VRule.NotEmpty()),
                        Field.is("description",STRING));

        todoManager.defineRelationship(Between.things(project, todo), AndCall.it("tasks"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(project, category), AndCall.it("categories"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, todo), AndCall.it("todos"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, project), AndCall.it("projects"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(todo, category), AndCall.it("categories"), WithCardinality.of("1", "*"));



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
    }


    @Test
    public void ApiPrototypeFreeBackend() {


        // stuff we could get for free from backend
        Thing todo = todoManager.getThingNamed("todo");
        Thing project = todoManager.getThingNamed("project");
        Thing category = todoManager.getThingNamed("category");

        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);
        System.out.println(JsonThing.asJson(paperwork));

        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        ThingInstance officeCategory = category.createInstance().setValue("title", "Office");
        category.addInstance(officeCategory);

        ThingInstance homeCategory = category.createInstance().setValue("title", "Home");
        category.addInstance(homeCategory);


        paperwork.connects("categories", officeCategory);

        // todo
        List<ThingInstance> query = todoManager.simplequery("todo");

        Assert.assertEquals(2, query.size());
        Assert.assertTrue(query.contains(paperwork));
        Assert.assertTrue(query.contains(filework));

        System.out.println(JsonThing.asJson(query));

        // todo/_GUID_
        query = todoManager.simplequery("todo/" + paperwork.getGUID());

        Assert.assertEquals(1, query.size());
        Assert.assertTrue(query.contains(paperwork));
        Assert.assertFalse(query.contains(filework));

        ApiResponse apiresponse = todoManager.api().get("todo/" + paperwork.getGUID());
        Assert.assertEquals(200, apiresponse.getStatusCode());

        // get a todo that does not exist
        apiresponse = todoManager.api().get("todo/" + paperwork.getGUID() + "bob");
        Assert.assertEquals(404, apiresponse.getStatusCode());


        //
        ThingInstance officeWork = project.createInstance().setValue("title", "Office Work");
        project.addInstance(officeWork);

        officeWork.connects("tasks", paperwork);
        officeWork.connects("tasks", filework);


        // match on relationships
        // project/_GUID_/tasks
        query = todoManager.simplequery(String.format("project/%s/tasks", officeWork.getGUID()));

        Assert.assertEquals(2, query.size());
        Assert.assertTrue(query.contains(paperwork));
        Assert.assertTrue(query.contains(filework));

        System.out.println(JsonThing.asJson(query));

        // match on entity types
        // project/_GUID_/todo
        query = todoManager.simplequery(String.format("project/%s/todo", officeWork.getGUID()));

        Assert.assertEquals(2, query.size());
        Assert.assertTrue(query.contains(paperwork));
        Assert.assertTrue(query.contains(filework));

        System.out.println(JsonThing.asJson(query));

        // project/_GUID_/todo/category
        query = todoManager.simplequery(String.format("project/%s/todo/category", officeWork.getGUID()));

        Assert.assertEquals(1, query.size());
        Assert.assertTrue(query.contains(officeCategory));

        System.out.println(JsonThing.asJson(query));

        // invalid query should match nothing there is no entity called task
        // project/_GUID_/task
        query = todoManager.simplequery(String.format("project/%s/task", officeWork.getGUID()));

        Assert.assertEquals(0, query.size());

    }

    @Test
    public void NonHttpApiBasedTests(){

        // simulate a POST request

        // POST project
        Map requestBody = new HashMap<String,String>();
        requestBody.put("title", "My Office Work");

        new Gson().toJson(requestBody);

        // create a project with POST
        ApiResponse apiresponse = todoManager.api().post("project", new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());
        String officeWorkProjectGuid = simpleJsonValueGet(apiresponse.getBody(),"project", "guid");

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
        apiresponse = todoManager.api().post(String.format("project/%s",guid), new Gson().toJson(requestBody));
        Assert.assertEquals(404, apiresponse.getStatusCode());

        Assert.assertEquals(currentProjects, todoManager.getThingNamed("project").countInstances());

        // CREATE PROJECT WITH POST AND NO GUID
        requestBody = new HashMap<String,String>();
        requestBody.put("title", "MY NEW PROJECT");

        currentProjects = todoManager.getThingNamed("project").countInstances();
        apiresponse = todoManager.api().post(String.format("project"), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        JsonObject projectjson= new JsonParser().parse(apiresponse.getBody()).getAsJsonObject();
        String projectGUID = projectjson.get("project").getAsJsonObject().get("guid").getAsString();

        ThingInstance myNewProject = todoManager.getThingNamed("project").findInstance(FieldValue.is("guid", projectGUID));
        Assert.assertEquals("MY NEW PROJECT", myNewProject.getValue("title"));

        //Assert.assertEquals("My Office Work", officeWork.getValue("title"));

        Assert.assertEquals(currentProjects+1, todoManager.getThingNamed("project").countInstances());


        // PUT

        requestBody = new HashMap<String,String>();
        requestBody.put("title", "My Office Work");

        String officeWorkGuid = officeWork.getGUID();
        Assert.assertNotNull(officeWorkGuid);

        // amend existing project with PUT - this should validate that all required fields are present
        apiresponse = todoManager.api().put(String.format("project/%s",officeWork.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertEquals("My Office Work", officeWork.getValue("title"));

        officeWork.setValue("title", "office");
        Assert.assertEquals("office", officeWork.getValue("title"));
        Assert.assertNotNull(officeWorkGuid);

        // create with a PUT and a given GUID
        guid = UUID.randomUUID().toString();
        currentProjects = todoManager.getThingNamed("project").countInstances();
        apiresponse = todoManager.api().put(String.format("project/%s",guid), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(currentProjects+1, todoManager.getThingNamed("project").countInstances());
        Assert.assertEquals("office", officeWork.getValue("title"));
        Assert.assertEquals(officeWorkGuid, officeWork.getGUID());
        ThingInstance newProject = todoManager.getThingNamed("project").findInstance(FieldValue.is("guid", guid));

        Assert.assertEquals("My Office Work", newProject.getValue("title"));
        Assert.assertEquals(guid, newProject.getValue("guid"));


        // DELETE
        apiresponse = todoManager.api().delete(String.format("project/%s",officeWork.getGUID()));
        Assert.assertEquals(200, apiresponse.getStatusCode());

        Assert.assertEquals(currentProjects, todoManager.getThingNamed("project").countInstances());

        apiresponse = todoManager.api().delete(String.format("project/%s",officeWork.getGUID()));
        Assert.assertEquals(404, apiresponse.getStatusCode());



        // create a todo with POST

        requestBody = new HashMap<String,String>();
        requestBody.put("title", "My Paperwork Todo");

        apiresponse = todoManager.api().post("todo", new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());
        String paperworkTodoGuid = simpleJsonValueGet(apiresponse.getBody(),"todo", "guid");

        ThingInstance paperwork = todoManager.findThingInstanceByGuid(paperworkTodoGuid);

        // Create a relationship with POST
        // POST project/_GUID_/tasks
        // {"guid":"_GUID_"} need to find the thing then use that as the relationship type


        // Create a relationship with POST and just a GUID
        //myNewProject
        requestBody = new HashMap<String,String>();
        requestBody.put("guid", paperwork.getGUID());

        int numberOfTasks = myNewProject.connections("tasks").size();

        apiresponse = todoManager.api().post(String.format("project/%s/tasks",myNewProject.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(numberOfTasks+1, myNewProject.connections("tasks").size());

        System.out.println(todoManager);


        // Createa a relationship and a thing with a POST and no GUID
        // POST project/_GUID_/tasks
        // {"title":"A new TODO Item related to project"}
        requestBody = new HashMap<String,String>();
        requestBody.put("title", "A new TODO Item related to project");

        numberOfTasks = myNewProject.connections("tasks").size();

        apiresponse = todoManager.api().post(String.format("project/%s/tasks",myNewProject.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        // the created todo is in the body
        JsonObject todojson= new JsonParser().parse(apiresponse.getBody()).getAsJsonObject();
        String todoGUID = todojson.get("todo").getAsJsonObject().get("guid").getAsString();
        Assert.assertEquals("A new TODO Item related to project", todoManager.findThingInstanceByGuid(todoGUID).getValue("title"));

        Assert.assertEquals(numberOfTasks+1, myNewProject.connections("tasks").size());

        System.out.println(todoManager);



        // DELETE a Relationship
        // DELETE project/_GUID_/tasks/_GUID_
        numberOfTasks = myNewProject.connections("tasks").size();

        apiresponse = todoManager.api().delete(String.format("project/%s/tasks/%s",myNewProject.getGUID(), paperwork.getGUID()));
        Assert.assertEquals(200, apiresponse.getStatusCode());

        Assert.assertEquals(numberOfTasks-1, myNewProject.connections("tasks").size());
        Assert.assertNotNull("Task should exist, only the relationship should be deleted",
                                todoManager.getThingNamed("todo").findInstance(FieldValue.is("guid", paperwork.getGUID())));


        System.out.println(todoManager);


        // Mandatory field validation on POST create - must have a title
        requestBody = new HashMap<String,String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");

        apiresponse = todoManager.api().post(String.format("todo"), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());

        // Mandatory field validation PUT create
        requestBody = new HashMap<String,String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", UUID.randomUUID().toString()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());

        // Mandatory field validation PUT amend
        requestBody = new HashMap<String,String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "Amended TODO Item ");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());


        // Field validation on boolean for Create with POST
        requestBody = new HashMap<String,String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        apiresponse = todoManager.api().post(String.format("todo"), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());

        // Field validation on boolean for Amend with POST
        requestBody = new HashMap<String,String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        apiresponse = todoManager.api().post(String.format("todo/%s", paperwork.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());


        // Field validation on boolean for Amend with PUT
        requestBody = new HashMap<String,String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), new Gson().toJson(requestBody));
        System.out.println(apiresponse.getBody());
        Assert.assertEquals(400, apiresponse.getStatusCode());



    }

    private String simpleJsonValueGet(String body, String... terms) {

        JsonObject obj= new JsonParser().parse(body).getAsJsonObject();
        String value ="";

        for(int termId = 0; termId<terms.length; termId++){
            if(termId<terms.length-1){
                obj = obj.get(terms[termId]).getAsJsonObject();
            }else{
                value= obj.get(terms[termId]).getAsString();
            }
        }

        return value;
    }


    @Test
    public void RelationshipDefinitionCheck(){


        Thing todo = todoManager.getThingNamed("todo");
        Thing project = todoManager.getThingNamed("project");
        Thing category = todoManager.getThingNamed("category");

        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);
        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        ThingInstance officeWork = project.createInstance().setValue("title", "Office Work");
        project.addInstance(officeWork);

        officeWork.connects("tasks", paperwork);
        officeWork.connects("tasks", filework);

        List<RelationshipInstance> relationships = officeWork.connections("tasks");
        List<ThingInstance> relatedItems = new ArrayList<ThingInstance>();
        for(RelationshipInstance relationship : relationships){
            relatedItems.add(relationship.getTo());
        }

        Assert.assertTrue(relatedItems.contains(paperwork));
        Assert.assertTrue(relatedItems.contains(filework));


        relatedItems = officeWork.connectedItems("tasks");
        Assert.assertTrue(relatedItems.contains(paperwork));
        Assert.assertTrue(relatedItems.contains(filework));

        todo.deleteInstance(paperwork.getGUID());

        relatedItems = officeWork.connectedItems("tasks");
        Assert.assertFalse(relatedItems.contains(paperwork));
        Assert.assertTrue(relatedItems.contains(filework));
    }


    @Test
    public void todoModelDefinitionCheck(){


        Thing todo = todoManager.getThingNamed("todo");

        Assert.assertTrue(todo.definition().hasFieldNameDefined("title"));
        Assert.assertTrue(todo.definition().hasFieldNameDefined("description"));
        Assert.assertTrue(todo.definition().hasFieldNameDefined("doneStatus"));

        Assert.assertEquals("FALSE", todo.definition().getField("doneStatus").getDefaultValue());

    }

    @Test
    public void createAndAmendSomeTodos(){

        Thing todos = todoManager.getThingNamed("todo");

        ThingInstance tidy = todos.createInstance().
                                setValue("title", "Tidy up my room").
                                setValue("description", "I need to tidy up my room because it is a mess");

        todos.addInstance(tidy);

        ThingInstance paperwork = todos.createInstance().
                setValue("title","Do Paperwork").
                setValue("description", "Scan everything in, upload to document management system and file paperwork");

        todos.addInstance(paperwork);

        Assert.assertEquals("FALSE", paperwork.getValue("doneStatus"));

        System.out.println(todoManager.toString());

        tidy.setValue("doneStatus", "TRUE");
        Assert.assertEquals("TRUE", tidy.getValue("doneStatus"));
        System.out.println(todoManager.toString());

    }

    @Test
    public void createAndDeleteTodos(){

        Thing todos = todoManager.getThingNamed("todo");

        int originalTodosCount = todos.countInstances();

        ThingInstance tidy = todos.createInstance().
                setValue("title","Delete this todo").
                setValue("description", "I need to be deleted");

        todos.addInstance(tidy);

        ThingInstance foundit = todos.findInstance(tidy.getGUID());

        Assert.assertEquals("Delete this todo", foundit.getValue("title"));

        todos.deleteInstance(foundit.getGUID());
        Assert.assertEquals(originalTodosCount, todos.countInstances());


        foundit = todos.findInstance(tidy.getGUID());

        Assert.assertNull(foundit);


        try{
            todos.deleteInstance(foundit.getGUID());
            Assert.fail("Item already deleted, exception should have been thrown");
        }catch(Exception e){

        }

    }

    @Test
    public void createAmendAndDeleteATodoWithAGivenGUID(){

        Thing todos = todoManager.getThingNamed("todo");

        int originalTodosCount = todos.countInstances();

        String guid="1234-12334-1234-1234";

        ThingInstance tidy = todos.createInstance(guid).setValue("title", "Delete this todo").
                setValue("description", "I need to be deleted");

        todos.addInstance(tidy);

        ThingInstance foundit = todos.findInstance(guid);

        Assert.assertEquals("Delete this todo", foundit.getValue("title"));

        todos.deleteInstance(guid);
        Assert.assertEquals(originalTodosCount, todos.countInstances());


        foundit = todos.findInstance(guid);

        Assert.assertNull(foundit);


        try{
            todos.deleteInstance(foundit.getGUID());
            Assert.fail("Item already deleted, exception should have been thrown");
        }catch(Exception e){

        }

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
