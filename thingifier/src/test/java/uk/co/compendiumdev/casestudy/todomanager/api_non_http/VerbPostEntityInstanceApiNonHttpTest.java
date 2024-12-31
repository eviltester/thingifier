package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import com.google.gson.Gson;
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
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.*;

public class VerbPostEntityInstanceApiNonHttpTest {


    private Thingifier todoManager;

    EntityInstanceCollection todo;
    EntityInstanceCollection project;


    // TODO: tests that use the TodoManagerModel were created early and are too complicated - simplify
    // when the thingifier was a prototype and we were building the todo manager at the same
    // time this saved time. Now, the tests are too complicated to maintain because the TodoManagerModel
    // is complex. We should simplify these tests and move them into the actual standAlone
    // projects
    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);
        project = todoManager.getThingInstancesNamed("project", EntityRelModel.DEFAULT_DATABASE_NAME);

    }
    
       /*


    Non HTTP API Based Tests


    */



    @Test
    public void postCanCreateAnEntityWhichPassesValidationWithAllFields() {

        // POST project
        Map<String,String> requestBody = new HashMap<>();
        String title = "My Office Work" + System.currentTimeMillis();
        String description = "MyDescription " + System.currentTimeMillis();
        String doneStatus = "true";

        requestBody.put("title", title);
        requestBody.put("description", description);
        requestBody.put("doneStatus", doneStatus);

        // create a project with POST
        ApiResponse apiresponse = todoManager.api().post("todo", getSimpleParser(requestBody), new HttpHeadersBlock());

        Assertions.assertEquals(201, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(0, apiresponse.getErrorMessages().size());

        EntityInstance createdInstance = apiresponse.getReturnedInstance();

        String officeWorkGuid = createdInstance.getPrimaryKeyValue();
        Assertions.assertEquals(title, createdInstance.getFieldValue("title").asString());
        Assertions.assertEquals(description, createdInstance.getFieldValue("description").asString());
        Assertions.assertEquals(doneStatus, createdInstance.getFieldValue("doneStatus").asString());


        // Check header for GUID
        String headerLocation = apiresponse.getHeaderValue("Location");
        String headerGUID = apiresponse.getHeaderValue(ApiResponse.PRIMARY_KEY_HEADER);

        Assertions.assertEquals(headerGUID, officeWorkGuid);
        Assertions.assertEquals("/todos/" + officeWorkGuid, headerLocation);

        // check that it is created in the model

        EntityInstance createdProject = todo.findInstanceByPrimaryKey(headerGUID);

        Assertions.assertEquals(createdProject, createdInstance);

    }

    private BodyParser getSimpleParser(final Map<String,String> requestBody) {

            final HttpApiRequest arequest = new HttpApiRequest("/path").setBody(new Gson().toJson(requestBody));
            return new BodyParser(arequest, todoManager.getThingNames());
    }

    @Test
    public void postCanCreateAnEntityWhichPassesValidationWithMinimumFields() {

        // POST project
        Map<String, String> requestBody = new HashMap<>();
        String title = "My Office Work" + System.currentTimeMillis();

        requestBody.put("title", title);

        // create a project with POST
        ApiResponse apiresponse = todoManager.api().post("todo",  getSimpleParser(requestBody), new HttpHeadersBlock());


        Assertions.assertEquals(201, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(0, apiresponse.getErrorMessages().size());

        EntityInstance createdInstance = apiresponse.getReturnedInstance();

        String officeWorkGuid = createdInstance.getPrimaryKeyValue();
        Assertions.assertEquals(title, createdInstance.getFieldValue("title").asString());
        Assertions.assertEquals("", createdInstance.getFieldValue("description").asString());
        Assertions.assertEquals("false", createdInstance.getFieldValue("doneStatus").asString());

        // Check header for GUID
        String headerLocation = apiresponse.getHeaderValue("Location");
        String headerGUID = apiresponse.getHeaderValue(ApiResponse.PRIMARY_KEY_HEADER);

        Assertions.assertEquals(headerGUID, officeWorkGuid);
        Assertions.assertEquals("/todos/" + officeWorkGuid, headerLocation);

        // check that it is created in the model

        EntityInstance createdProject = todo.findInstanceByPrimaryKey(headerGUID);

        Assertions.assertEquals(createdProject, createdInstance);

    }


    @Test
    public void postCanAmendAnExistingEntity() {

        EntityInstance relTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for amending");


        // POST project
        Map<String, String> requestBody = new HashMap<>();
        String title = "My New Title" + System.currentTimeMillis();
        String description = "My Description " + System.currentTimeMillis();

        requestBody.put("title", title);
        requestBody.put("description", description);

        // amend a project with POST
        ApiResponse apiresponse = todoManager.api().post("todo/" + relTodo.getPrimaryKeyValue(),  getSimpleParser(requestBody), new HttpHeadersBlock());

        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals(0, apiresponse.getHeaders().size());
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(0, apiresponse.getErrorMessages().size());

        // Check response

        EntityInstance createdInstance = apiresponse.getReturnedInstance();

        Assertions.assertEquals(relTodo.getPrimaryKeyValue(), createdInstance.getPrimaryKeyValue());
        Assertions.assertEquals(title, createdInstance.getFieldValue("title").asString());
        Assertions.assertEquals(description, createdInstance.getFieldValue("description").asString());
        Assertions.assertEquals("false", createdInstance.getFieldValue("doneStatus").asString());

    }

    @Test
    public void postFailCannotCreateProjectWithGuidInUrl() {

        int currentProjects = project.countInstances();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", "My Office Work");

        String guid = UUID.randomUUID().toString();

        ApiResponse apiresponse = todoManager.api().post(String.format("project/%s", guid),  getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(404, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        Assertions.assertEquals(currentProjects, project.countInstances());

    }

    @Test
    public void postFailCannotAmendEntityInstanceWhenValidationErrorsAPI() {

        Map<String, String> requestBody;
        ApiResponse apiresponse;

        String originalTitle = "Todo for amending " + System.currentTimeMillis();
        String originalDescription = "my description " + System.currentTimeMillis();

        EntityInstance amendTodo = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", originalTitle).setValue("description", originalDescription);


        // Mandatory field validation
        requestBody = new HashMap<>();
        requestBody.put("title", "");
        requestBody.put("description", "Amend Failed new TODO Item");
        apiresponse = todoManager.api().post(String.format("todo/%s", amendTodo.getPrimaryKeyValue()),  getSimpleParser(requestBody), new HttpHeadersBlock());

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertFalse(apiresponse.getErrorMessages().isEmpty());
        Assertions.assertTrue(apiresponse.hasABody());


        Assertions.assertEquals(originalTitle, amendTodo.getFieldValue("title").asString());
        Assertions.assertEquals(originalDescription, amendTodo.getFieldValue("description").asString());


        requestBody = new HashMap<>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse = todoManager.api().post(String.format("todo/%s", amendTodo.getPrimaryKeyValue()),  getSimpleParser(requestBody), new HttpHeadersBlock());

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        Assertions.assertEquals(originalTitle, amendTodo.getFieldValue("title").asString());
        Assertions.assertEquals(originalDescription, amendTodo.getFieldValue("description").asString());

    }


    @Test
    public void putCanAmendExistingProject() {

        Map<String, String> requestBody;
        ApiResponse apiresponse;


        // PUT

        requestBody = new HashMap<>();
        requestBody.put("title", "My Office Work");

        EntityInstance officeWork = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "An Existing Project");

        String officeWorkGuid = officeWork.getPrimaryKeyValue();
        Assertions.assertNotNull(officeWorkGuid);

        // amend existing project with PUT - this should validate that all required fields are present
        apiresponse = todoManager.api().put(String.format("project/%s", officeWork.getPrimaryKeyValue()),  getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals("My Office Work", officeWork.getFieldValue("title").asString());

        officeWork.setValue("title", "office");
        Assertions.assertEquals("office", officeWork.getFieldValue("title").asString());
        Assertions.assertNotNull(officeWorkGuid);

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(officeWorkGuid, apiresponse.getReturnedInstance().getPrimaryKeyValue());

    }


    @Test
    public void postFailCannotCreateEntityInstanceWhenValidationErrorsAPI() {

        Map<String, String> requestBody;
        ApiResponse apiresponse;

        // Mandatory field validation on POST create - must have a title
        requestBody = new HashMap<>();
        requestBody.put("description", "A new TODO Item"); // 400 because it should be "title"

        apiresponse = todoManager.api().post(String.format("todo"),  getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertFalse(apiresponse.getErrorMessages().isEmpty());
        Assertions.assertTrue(apiresponse.hasABody());

        // Field validation on boolean for Create with POST
        requestBody = new HashMap<>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        apiresponse = todoManager.api().post(String.format("todo"), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertFalse(apiresponse.getErrorMessages().isEmpty());
        Assertions.assertTrue(apiresponse.hasABody());

        // Field validation on boolean for Amend with POST
        requestBody = new HashMap<>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        EntityInstance paperwork = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "Todo for amending");

        apiresponse = todoManager.api().post(String.format("todo/%s", paperwork.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertFalse(apiresponse.getErrorMessages().isEmpty());
        Assertions.assertTrue(apiresponse.hasABody());

    }


}
