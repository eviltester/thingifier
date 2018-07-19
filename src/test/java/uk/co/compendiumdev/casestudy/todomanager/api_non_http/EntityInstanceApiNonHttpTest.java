package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityInstanceApiNonHttpTest {


    private Thingifier todoManager;

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

    /* TODO: additional entity instance test methods required to create visible condition coverage via method names

    - Get can return single entity instance
    - Get can return multiple entity instances
    - Post fail amend due to validation errors
    - Put fail amend due to validation errors
     */

    @Test
    public void postCanCreateAnEntityWhichPassesValidationWithAllFields(){

        // POST project
        Map requestBody = new HashMap<String, String>();
        String title = "My Office Work" + System.currentTimeMillis();
        String description = "MyDescription " + System.currentTimeMillis();
        String doneStatus = "TRUE";

        requestBody.put("title", title);
        requestBody.put("description", description);
        requestBody.put("doneStatus", doneStatus);

        new Gson().toJson(requestBody);

        // create a project with POST
        ApiResponse apiresponse = todoManager.api().post("todo", new Gson().toJson(requestBody));

        Assert.assertEquals(201, apiresponse.getStatusCode());

        ThingInstance createdInstance = apiresponse.getReturnedInstance();

        String officeWorkGuid = createdInstance.getGUID();
        Assert.assertEquals(title, createdInstance.getValue("title"));
        Assert.assertEquals(description, createdInstance.getValue("description"));
        Assert.assertEquals(doneStatus, createdInstance.getValue("doneStatus"));


        // Check header for GUID
        String headerLocation = apiresponse.getHeaderValue("Location");
        String headerGUID = apiresponse.getHeaderValue(ApiResponse.GUID_HEADER);

        Assert.assertEquals(headerGUID, officeWorkGuid);
        Assert.assertEquals("todo/"+officeWorkGuid, headerLocation);

        // check that it is created in the model

        ThingInstance createdProject = todo.findInstanceByGUID(headerGUID);

        Assert.assertEquals(createdProject, createdInstance);

    }

    @Test
    public void postCanCreateAnEntityWhichPassesValidationWithMinimumFields(){

        // POST project
        Map requestBody = new HashMap<String, String>();
        String title = "My Office Work" + System.currentTimeMillis();

        requestBody.put("title", title);

        new Gson().toJson(requestBody);

        // create a project with POST
        ApiResponse apiresponse = todoManager.api().post("todo", new Gson().toJson(requestBody));


        Assert.assertEquals(201, apiresponse.getStatusCode());

        ThingInstance createdInstance = apiresponse.getReturnedInstance();

        String officeWorkGuid = createdInstance.getGUID();
        Assert.assertEquals(title, createdInstance.getValue("title"));
        Assert.assertEquals("", createdInstance.getValue("description"));
        Assert.assertEquals("FALSE", createdInstance.getValue("doneStatus"));

        // Check header for GUID
        String headerLocation = apiresponse.getHeaderValue("Location");
        String headerGUID = apiresponse.getHeaderValue(ApiResponse.GUID_HEADER);

        Assert.assertEquals(headerGUID, officeWorkGuid);
        Assert.assertEquals("todo/"+officeWorkGuid, headerLocation);

        // check that it is created in the model

        ThingInstance createdProject = todo.findInstanceByGUID(headerGUID);

        Assert.assertEquals(createdProject, createdInstance);

    }


    @Test
    public void postCanAmendAnExistingEntity(){

        ThingInstance relTodo = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(relTodo);


        // POST project
        Map requestBody = new HashMap<String, String>();
        String title = "My New Title" + System.currentTimeMillis();
        String description = "My Description " + System.currentTimeMillis();

        requestBody.put("title", title);
        requestBody.put("description", description);

        new Gson().toJson(requestBody);

        // amend a project with POST
        ApiResponse apiresponse = todoManager.api().post("todo/" + relTodo.getGUID(), new Gson().toJson(requestBody));

        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertEquals(0, apiresponse.getHeaders().size());

        // Check response

        ThingInstance createdInstance = apiresponse.getReturnedInstance();

        Assert.assertEquals(relTodo.getGUID(), createdInstance.getGUID());
        Assert.assertEquals(title, createdInstance.getValue("title"));
        Assert.assertEquals(description, createdInstance.getValue("description"));
        Assert.assertEquals("FALSE", createdInstance.getValue("doneStatus"));

    }

    @Test
    public void postFailCannotCreateProjectWithGuidInUrl() {

        int currentProjects = project.countInstances();

        Map requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");

        String guid = UUID.randomUUID().toString();

        ApiResponse apiresponse = todoManager.api().post(String.format("project/%s", guid), new Gson().toJson(requestBody));
        Assert.assertEquals(404, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()>0);

        Assert.assertEquals(currentProjects, project.countInstances());

    }


    @Test
    public void putCanAmendExistingProject() {

        Map requestBody;
        ApiResponse apiresponse;


        // PUT

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");

        ThingInstance officeWork = project.createInstance().setValue("title", "An Existing Project");
        project.addInstance(officeWork);

        String officeWorkGuid = officeWork.getGUID();
        Assert.assertNotNull(officeWorkGuid);

        // amend existing project with PUT - this should validate that all required fields are present
        apiresponse = todoManager.api().put(String.format("project/%s", officeWork.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertEquals("My Office Work", officeWork.getValue("title"));

        officeWork.setValue("title", "office");
        Assert.assertEquals("office", officeWork.getValue("title"));
        Assert.assertNotNull(officeWorkGuid);
    }

    @Test
    public void putCanCreateAnEntityInstanceWithAGivenGUID() {

        Map requestBody;
        ApiResponse apiresponse;


        // PUT

        requestBody = new HashMap<String, String>();
        String title = "My Office Work " + System.currentTimeMillis();
        requestBody.put("title", title);


        int currentProjects = project.countInstances();
        Assert.assertEquals(0, currentProjects);

        // create with a PUT and a given GUID
        String guid = UUID.randomUUID().toString();


        apiresponse = todoManager.api().put(String.format("project/%s", guid), new Gson().toJson(requestBody));
        Assert.assertEquals(201, apiresponse.getStatusCode());

        Assert.assertEquals(guid, apiresponse.getHeaderValue(ApiResponse.GUID_HEADER));
        Assert.assertTrue(apiresponse.getHeaderValue("Location").endsWith(guid));

        Assert.assertEquals(currentProjects + 1, project.countInstances());


        ThingInstance newProject = project.findInstanceByField(FieldValue.is("guid", guid));

        Assert.assertEquals(title, newProject.getValue("title"));
        Assert.assertEquals(guid, newProject.getValue("guid"));
    }

    @Test
    public void deleteAnEntityInstanceAPI() {
        ApiResponse apiresponse;

        ThingInstance officeWork = project.createInstance().setValue("title", "An Existing Project");
        project.addInstance(officeWork);

        Assert.assertEquals(1, project.countInstances());

        apiresponse = todoManager.api().delete(String.format("project/%s", officeWork.getGUID()));
        Assert.assertEquals(200, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()==0);

        Assert.assertEquals(0, project.countInstances());

        apiresponse = todoManager.api().delete(String.format("project/%s", officeWork.getGUID()));
        Assert.assertEquals(404, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()>0);

    }


    @Test
    public void deleteFailToDeleteAGUIDThatDoesNotExistAsAnEntityInstance() {

        ApiResponse apiresponse;

        apiresponse = todoManager.api().delete(String.format("project/%s", UUID.randomUUID().toString()));
        Assert.assertEquals(404, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()>0);

    }

    @Test
    public void postFailCannotCreateValidationErrorsEntityInstanceAPI() {

        Map requestBody;
        ApiResponse apiresponse;

        // TODO: assert that error messages are present in the APIResponse

        // Mandatory field validation on POST create - must have a title
        requestBody = new HashMap<String, String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");

        apiresponse = todoManager.api().post(String.format("todo"), new Gson().toJson(requestBody));
        Assert.assertEquals(400, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()>0);

        // Field validation on boolean for Create with POST
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        apiresponse = todoManager.api().post(String.format("todo"), new Gson().toJson(requestBody));
        Assert.assertEquals(400, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()>0);

        // Field validation on boolean for Amend with POST
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        ThingInstance paperwork = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(paperwork);

        apiresponse = todoManager.api().post(String.format("todo/%s", paperwork.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(400, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()>0);

    }

    @Test
    public void putFailValidationEntityInstanceAPI() {

        Map requestBody;
        ApiResponse apiresponse;

        // TODO: assert that error messages are present in the api response

        // Mandatory field validation on POST create - must have a title
        requestBody = new HashMap<String, String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");

        // Mandatory field validation PUT create
        requestBody = new HashMap<String, String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", UUID.randomUUID().toString()), new Gson().toJson(requestBody));
        Assert.assertEquals(400, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()>0);


        ThingInstance paperwork = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(paperwork);

        // Mandatory field validation PUT amend
        requestBody = new HashMap<String, String>();
        //requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "Amended TODO Item ");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(400, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()>0);


        // Field validation on boolean for Amend with PUT
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), new Gson().toJson(requestBody));
        Assert.assertEquals(400, apiresponse.getStatusCode());
        Assert.assertTrue(apiresponse.getErrorMessages().size()>0);

    }


}
