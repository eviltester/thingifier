package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.*;

public class EntityInstanceApiNonHttpTest {


    private Thingifier todoManager;

    Thing todo;
    Thing project;


    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");

    }
    
       /*


    Non HTTP API Based Tests


    */


    @Test
    public void getCanReturnASingleEntityInstance() {

        // add some data
        todo.addInstance(todo.createInstance().setValue("title", "My Title" + System.nanoTime()));
        todo.addInstance(todo.createInstance().setValue("title", "My Title" + System.nanoTime()));


        ThingInstance findThis = todo.createInstance().setValue("title", "My Title" + System.nanoTime());
        todo.addInstance(findThis);

        todo.addInstance(todo.createInstance().setValue("title", "My Title" + System.nanoTime()));


        ApiResponse apiResponse = todoManager.api().get("/todo/" + findThis.getGUID());

        Assertions.assertEquals(200, apiResponse.getStatusCode());
        Assertions.assertFalse(apiResponse.isCollection(),
                "Should be a single item, rather than a collection");
        Assertions.assertTrue(apiResponse.hasABody());

        Assertions.assertEquals(findThis.getValue("title"), apiResponse.getReturnedInstance().getValue("title"));
        Assertions.assertEquals(findThis.getValue("guid"), apiResponse.getReturnedInstance().getValue("guid"));

        Assertions.assertEquals(findThis, apiResponse.getReturnedInstance());
        Assertions.assertEquals(0, apiResponse.getErrorMessages().size());

    }

    @Test
    public void getCanReturnMultipleEntityInstances() {

        // add some data
        todo.addInstance(todo.createInstance().setValue("title", "My Title" + System.nanoTime()));
        todo.addInstance(todo.createInstance().setValue("title", "My Title" + System.nanoTime()));
        todo.addInstance(todo.createInstance().setValue("title", "My Title" + System.nanoTime()));
        todo.addInstance(todo.createInstance().setValue("title", "My Title" + System.nanoTime()));


        ApiResponse apiResponse = todoManager.api().get("/todo");

        Assertions.assertEquals(200, apiResponse.getStatusCode());
        Assertions.assertTrue(apiResponse.isCollection(),
                "Should be a collection");
        Assertions.assertTrue(apiResponse.hasABody());

        Assertions.assertEquals(todo.countInstances(), apiResponse.getReturnedInstanceCollection().size());

        Set<String> guidSet = new HashSet<>();

        for (ThingInstance item : apiResponse.getReturnedInstanceCollection()) {
            guidSet.add(item.getGUID());
            Assertions.assertNotNull(todo.findInstanceByGUID(item.getGUID()));
        }

        Assertions.assertEquals(guidSet.size(), todo.countInstances());
        Assertions.assertEquals(guidSet.size(), apiResponse.getReturnedInstanceCollection().size());

        Assertions.assertEquals(0, apiResponse.getErrorMessages().size());

    }


    @Test
    public void postCanCreateAnEntityWhichPassesValidationWithAllFields() {

        // POST project
        Map requestBody = new HashMap<String, String>();
        String title = "My Office Work" + System.currentTimeMillis();
        String description = "MyDescription " + System.currentTimeMillis();
        String doneStatus = "TRUE";

        requestBody.put("title", title);
        requestBody.put("description", description);
        requestBody.put("doneStatus", doneStatus);

        // create a project with POST
        ApiResponse apiresponse = todoManager.api().post("todo", getSimpleParser(requestBody));

        Assertions.assertEquals(201, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(0, apiresponse.getErrorMessages().size());

        ThingInstance createdInstance = apiresponse.getReturnedInstance();

        String officeWorkGuid = createdInstance.getGUID();
        Assertions.assertEquals(title, createdInstance.getValue("title"));
        Assertions.assertEquals(description, createdInstance.getValue("description"));
        Assertions.assertEquals(doneStatus, createdInstance.getValue("doneStatus"));


        // Check header for GUID
        String headerLocation = apiresponse.getHeaderValue("Location");
        String headerGUID = apiresponse.getHeaderValue(ApiResponse.GUID_HEADER);

        Assertions.assertEquals(headerGUID, officeWorkGuid);
        Assertions.assertEquals("todos/" + officeWorkGuid, headerLocation);

        // check that it is created in the model

        ThingInstance createdProject = todo.findInstanceByGUID(headerGUID);

        Assertions.assertEquals(createdProject, createdInstance);

    }

    private BodyParser getSimpleParser(final Map requestBody) {

            final HttpApiRequest arequest = new HttpApiRequest("/path").setBody(new Gson().toJson(requestBody));
            return new BodyParser(arequest, todoManager.getThingNames());
    }

    @Test
    public void postCanCreateAnEntityWhichPassesValidationWithMinimumFields() {

        // POST project
        Map requestBody = new HashMap<String, String>();
        String title = "My Office Work" + System.currentTimeMillis();

        requestBody.put("title", title);

        // create a project with POST
        ApiResponse apiresponse = todoManager.api().post("todo",  getSimpleParser(requestBody));


        Assertions.assertEquals(201, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(0, apiresponse.getErrorMessages().size());

        ThingInstance createdInstance = apiresponse.getReturnedInstance();

        String officeWorkGuid = createdInstance.getGUID();
        Assertions.assertEquals(title, createdInstance.getValue("title"));
        Assertions.assertEquals("", createdInstance.getValue("description"));
        Assertions.assertEquals("FALSE", createdInstance.getValue("doneStatus"));

        // Check header for GUID
        String headerLocation = apiresponse.getHeaderValue("Location");
        String headerGUID = apiresponse.getHeaderValue(ApiResponse.GUID_HEADER);

        Assertions.assertEquals(headerGUID, officeWorkGuid);
        Assertions.assertEquals("todos/" + officeWorkGuid, headerLocation);

        // check that it is created in the model

        ThingInstance createdProject = todo.findInstanceByGUID(headerGUID);

        Assertions.assertEquals(createdProject, createdInstance);

    }


    @Test
    public void postCanAmendAnExistingEntity() {

        ThingInstance relTodo = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(relTodo);


        // POST project
        Map requestBody = new HashMap<String, String>();
        String title = "My New Title" + System.currentTimeMillis();
        String description = "My Description " + System.currentTimeMillis();

        requestBody.put("title", title);
        requestBody.put("description", description);

        // amend a project with POST
        ApiResponse apiresponse = todoManager.api().post("todo/" + relTodo.getGUID(),  getSimpleParser(requestBody));

        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals(0, apiresponse.getHeaders().size());
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(0, apiresponse.getErrorMessages().size());

        // Check response

        ThingInstance createdInstance = apiresponse.getReturnedInstance();

        Assertions.assertEquals(relTodo.getGUID(), createdInstance.getGUID());
        Assertions.assertEquals(title, createdInstance.getValue("title"));
        Assertions.assertEquals(description, createdInstance.getValue("description"));
        Assertions.assertEquals("FALSE", createdInstance.getValue("doneStatus"));

    }

    @Test
    public void postFailCannotCreateProjectWithGuidInUrl() {

        int currentProjects = project.countInstances();

        Map requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");

        String guid = UUID.randomUUID().toString();

        ApiResponse apiresponse = todoManager.api().post(String.format("project/%s", guid),  getSimpleParser(requestBody));
        Assertions.assertEquals(404, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        Assertions.assertEquals(currentProjects, project.countInstances());

    }

    @Test
    public void postFailCannotAmendEntityInstanceWhenValidationErrorsAPI() {

        Map requestBody;
        ApiResponse apiresponse;

        String originalTitle = "Todo for amending " + System.currentTimeMillis();
        String originalDescription = "my description " + System.currentTimeMillis();

        ThingInstance amendTodo = todo.createInstance().setValue("title", originalTitle).setValue("description", originalDescription);
        todo.addInstance(amendTodo);


        // Mandatory field validation PUT title
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "");
        requestBody.put("description", "Amend Failed new TODO Item");
        apiresponse = todoManager.api().post(String.format("todo/%s", amendTodo.getGUID()),  getSimpleParser(requestBody));

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        Assertions.assertEquals(originalTitle, amendTodo.getValue("title"));
        Assertions.assertEquals(originalDescription, amendTodo.getValue("description"));


        // Field validation on boolean for Amend with PUT
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse = todoManager.api().post(String.format("todo/%s", amendTodo.getGUID()),  getSimpleParser(requestBody));

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        Assertions.assertEquals(originalTitle, amendTodo.getValue("title"));
        Assertions.assertEquals(originalDescription, amendTodo.getValue("description"));

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
        Assertions.assertNotNull(officeWorkGuid);

        // amend existing project with PUT - this should validate that all required fields are present
        apiresponse = todoManager.api().put(String.format("project/%s", officeWork.getGUID()),  getSimpleParser(requestBody));
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals("My Office Work", officeWork.getValue("title"));

        officeWork.setValue("title", "office");
        Assertions.assertEquals("office", officeWork.getValue("title"));
        Assertions.assertNotNull(officeWorkGuid);

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(officeWorkGuid, apiresponse.getReturnedInstance().getGUID());

    }

    @Test
    public void putCanAmendExistingProjectByUSingDefaultFieldValues() {

        Map requestBody;
        ApiResponse apiresponse;


        // PUT


        ThingInstance officeWork = project.createInstance().
                setValue("title", "An Existing Project").
                setValue("description", "my original description");
        project.addInstance(officeWork);

        String officeWorkGuid = officeWork.getGUID();
        Assertions.assertNotNull(officeWorkGuid);

        // amend existing project with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        // note, I haven't added a description

        apiresponse = todoManager.api().put(String.format("project/%s", officeWork.getGUID()),  getSimpleParser(requestBody));
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals("My Office Work", officeWork.getValue("title"));
        Assertions.assertEquals("", officeWork.getValue("description"));

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(officeWorkGuid, apiresponse.getReturnedInstance().getGUID());

    }


    @Test
    public void putCanNotAmendGUID() {

        Map requestBody;
        ApiResponse apiresponse;


        // PUT


        ThingInstance officeWork = project.createInstance().
                setValue("title", "An Existing Project").
                setValue("description", "my original description");
        project.addInstance(officeWork);

        String originalGUID = officeWork.getGUID();
        Assertions.assertNotNull(originalGUID);

        // amend existing project with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        String newGUID = UUID.randomUUID().toString();
        requestBody.put("guid", newGUID);

        apiresponse = todoManager.api().put(String.format("project/%s", originalGUID),  getSimpleParser(requestBody));

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertEquals("An Existing Project", officeWork.getValue("title"));
        Assertions.assertEquals("my original description", officeWork.getValue("description"));
        Assertions.assertEquals(originalGUID, officeWork.getValue("guid"));

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertTrue(apiresponse.isErrorResponse());

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
        Assertions.assertEquals(0, currentProjects);

        // create with a PUT and a given GUID
        String guid = UUID.randomUUID().toString();


        apiresponse = todoManager.api().put(String.format("project/%s", guid),  getSimpleParser(requestBody));
        Assertions.assertEquals(201, apiresponse.getStatusCode());


        Assertions.assertEquals(guid, apiresponse.getHeaderValue(ApiResponse.GUID_HEADER));
        Assertions.assertTrue(apiresponse.getHeaderValue("Location").endsWith(guid));

        Assertions.assertEquals(currentProjects + 1, project.countInstances());


        ThingInstance newProject = project.findInstanceByField(FieldValue.is("guid", guid));

        Assertions.assertEquals(title, newProject.getValue("title"));
        Assertions.assertEquals(guid, newProject.getValue("guid"));

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() == 0);
        Assertions.assertEquals(newProject, apiresponse.getReturnedInstance());
    }

    @Test
    public void deleteAnEntityInstanceAPI() {
        ApiResponse apiresponse;

        ThingInstance officeWork = project.createInstance().setValue("title", "An Existing Project");
        project.addInstance(officeWork);

        Assertions.assertEquals(1, project.countInstances());

        apiresponse = todoManager.api().delete(String.format("project/%s", officeWork.getGUID()));
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() == 0);

        Assertions.assertFalse(apiresponse.hasABody());

        Assertions.assertEquals(0, project.countInstances());

        apiresponse = todoManager.api().delete(String.format("project/%s", officeWork.getGUID()));
        Assertions.assertEquals(404, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);

        Assertions.assertTrue(apiresponse.hasABody());

    }


    @Test
    public void deleteFailToDeleteAGUIDThatDoesNotExistAsAnEntityInstance() {

        ApiResponse apiresponse;

        apiresponse = todoManager.api().delete(String.format("project/%s", UUID.randomUUID().toString()));
        Assertions.assertEquals(404, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

    }

    @Test
    public void postFailCannotCreateEntityInstanceWhenValidationErrorsAPI() {

        Map requestBody;
        ApiResponse apiresponse;

        // Mandatory field validation on POST create - must have a title
        requestBody = new HashMap<String, String>();
        requestBody.put("description", "A new TODO Item"); // 400 because it should be "title"

        apiresponse = todoManager.api().post(String.format("todo"),  getSimpleParser(requestBody));
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

        // Field validation on boolean for Create with POST
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        apiresponse = todoManager.api().post(String.format("todo"), getSimpleParser(requestBody));
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

        // Field validation on boolean for Amend with POST
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        ThingInstance paperwork = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(paperwork);

        apiresponse = todoManager.api().post(String.format("todo/%s", paperwork.getGUID()), getSimpleParser(requestBody));
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

    }

    @Test
    public void putFailValidationEntityInstanceAPI() {

        Map requestBody;
        ApiResponse apiresponse;

        // Mandatory field validation PUT create
        requestBody = new HashMap<String, String>();
        // will generate 400 because description should be title
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", UUID.randomUUID().toString()), getSimpleParser(requestBody));
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        ThingInstance paperwork = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(paperwork);

        // Mandatory field validation PUT amend
        requestBody = new HashMap<String, String>();
        // will generate 400 because description should be title
        requestBody.put("description", "Amended TODO Item ");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), getSimpleParser(requestBody));
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        // Field validation on boolean for Amend with PUT
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), getSimpleParser(requestBody));
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

    }

    @Test
    public void putFailCannotCreateEntityInstanceWhenValidationErrors() {

        Map requestBody;
        ApiResponse apiresponse;

        // Mandatory field validation PUT create
        requestBody = new HashMap<String, String>();
        // will generate 400 because description should be title
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", UUID.randomUUID().toString()), getSimpleParser(requestBody));
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

    }

    @Test
    public void putFailCannotAmendEntityInstanceWhenValidationErrors() {

        Map requestBody;
        ApiResponse apiresponse;


        ThingInstance paperwork = todo.createInstance().setValue("title", "Todo for amending");
        todo.addInstance(paperwork);

        // Mandatory field validation PUT amend
        requestBody = new HashMap<String, String>();
        // will generate 400 because description should be title
        requestBody.put("description", "Amended TODO Item ");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), getSimpleParser(requestBody));
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        // Field validation on boolean for Amend with PUT
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getGUID()), getSimpleParser(requestBody));
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

    }
}
