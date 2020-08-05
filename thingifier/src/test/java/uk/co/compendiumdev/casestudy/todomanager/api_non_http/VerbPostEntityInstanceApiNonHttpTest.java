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
import uk.co.compendiumdev.thingifier.domain.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

import java.util.*;

public class VerbPostEntityInstanceApiNonHttpTest {


    private Thingifier todoManager;

    Thing todo;
    Thing project;


    // TODO: tests that use the TodoManagerModel were created early and are too complicated - simplify
    // when the thingifier was a prototype and we were building the todo manager at the same
    // time this saved time. Now, the tests are too complicated to maintain because the TodoManagerModel
    // is complex. We should simplify these tests and move them into the actual standAlone
    // projects
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
    public void postCanCreateAnEntityWhichPassesValidationWithAllFields() {

        // POST project
        Map requestBody = new HashMap<String, String>();
        String title = "My Office Work" + System.currentTimeMillis();
        String description = "MyDescription " + System.currentTimeMillis();
        String doneStatus = "true";

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
        Assertions.assertEquals("false", createdInstance.getValue("doneStatus"));

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
        Assertions.assertEquals("false", createdInstance.getValue("doneStatus"));

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


        // Mandatory field validation
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "");
        requestBody.put("description", "Amend Failed new TODO Item");
        apiresponse = todoManager.api().post(String.format("todo/%s", amendTodo.getGUID()),  getSimpleParser(requestBody));

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        Assertions.assertEquals(originalTitle, amendTodo.getValue("title"));
        Assertions.assertEquals(originalDescription, amendTodo.getValue("description"));


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


}
