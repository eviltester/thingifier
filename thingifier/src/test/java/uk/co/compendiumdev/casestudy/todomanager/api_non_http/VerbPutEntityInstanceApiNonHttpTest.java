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
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.*;

public class VerbPutEntityInstanceApiNonHttpTest {


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

    private BodyParser getSimpleParser(final Map requestBody) {

        final HttpApiRequest arequest = new HttpApiRequest("/path").setBody(new Gson().toJson(requestBody));
        return new BodyParser(arequest, todoManager.getThingNames());
    }


    @Test
    public void putCanAmendExistingProject() {

        Map requestBody;
        ApiResponse apiresponse;


        // PUT

        requestBody = new HashMap<String, String>();
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
    public void putCanAmendExistingProjectByUsingDefaultFieldValues() {

        Map requestBody;
        ApiResponse apiresponse;


        // PUT


        EntityInstance officeWork = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "An Existing Project").
                setValue("description", "my original description");

        String officeWorkGuid = officeWork.getPrimaryKeyValue();
        Assertions.assertNotNull(officeWorkGuid);

        // amend existing project with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        // note, I haven't added a description

        apiresponse = todoManager.api().put(String.format("project/%s", officeWork.getPrimaryKeyValue()),  getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals("My Office Work", officeWork.getFieldValue("title").asString());
        Assertions.assertEquals("", officeWork.getFieldValue("description").asString());

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(officeWorkGuid, apiresponse.getReturnedInstance().getPrimaryKeyValue());

    }


    @Test
    public void putCanNotAmendGUID() {

        Map requestBody;
        ApiResponse apiresponse;


        // PUT


        EntityInstance officeWork = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "An Existing Project").
                setValue("description", "my original description");

        String originalGUID = officeWork.getPrimaryKeyValue();
        Assertions.assertNotNull(originalGUID);

        // amend existing project with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        String newGUID = UUID.randomUUID().toString();
        requestBody.put("guid", newGUID);

        apiresponse = todoManager.api().put(String.format("project/%s", originalGUID),  getSimpleParser(requestBody), new HttpHeadersBlock());

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertEquals("An Existing Project", officeWork.getFieldValue("title").asString());
        Assertions.assertEquals("my original description", officeWork.getFieldValue("description").asString());
        Assertions.assertEquals(originalGUID, officeWork.getFieldValue("guid").asString());

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertTrue(apiresponse.isErrorResponse());

    }


//    @Test
//    public void putCanCreateAnEntityInstanceWithAGivenGUID() {
//
//        Map requestBody;
//        ApiResponse apiresponse;
//
//
//        // PUT
//
//        requestBody = new HashMap<String, String>();
//        String title = "My Office Work " + System.currentTimeMillis();
//        requestBody.put("title", title);
//
//
//        int currentProjects = project.countInstances();
//        Assertions.assertEquals(0, currentProjects);
//
//        // create with a PUT and a given GUID
//        String guid = UUID.randomUUID().toString();
//
//
//        apiresponse = todoManager.api().put(String.format("project/%s", guid),  getSimpleParser(requestBody), new HttpHeadersBlock());
//        Assertions.assertEquals(201, apiresponse.getStatusCode());
//
//
//        Assertions.assertEquals(guid, apiresponse.getHeaderValue(ApiResponse.PRIMARY_KEY_HEADER));
//        Assertions.assertTrue(apiresponse.getHeaderValue("Location").endsWith(guid));
//
//        Assertions.assertEquals(currentProjects + 1, project.countInstances());
//
//
//        EntityInstance newProject = project.findInstanceByFieldNameAndValue("guid", guid);
//
//        Assertions.assertEquals(title, newProject.getFieldValue("title").asString());
//        Assertions.assertEquals(guid, newProject.getFieldValue("guid").asString());
//
//        Assertions.assertTrue(apiresponse.hasABody());
//        Assertions.assertFalse(apiresponse.isCollection());
//        Assertions.assertTrue(apiresponse.getErrorMessages().size() == 0);
//        Assertions.assertEquals(newProject, apiresponse.getReturnedInstance());
//    }


//    @Test
//    public void putCanCreateAnEntityInstanceWithAGivenGUIDAndIds() {
//
//        Map requestBody;
//        ApiResponse apiresponse;
//
//        project = todoManager.getThingInstancesNamed("project", EntityRelModel.DEFAULT_DATABASE_NAME);
//        final Field anIdField = Field.is("id", FieldType.AUTO_INCREMENT);
//        project.definition().addField(anIdField);
//
//        // PUT
//        requestBody = new HashMap<String, String>();
//        String title = "My Office Work " + System.currentTimeMillis();
//        requestBody.put("title", title);
//        requestBody.put("id", "12");
//
//
//        int currentProjects = project.countInstances();
//        Assertions.assertEquals(0, currentProjects);
//
//        // create with a PUT and a given GUID
//        String guid = UUID.randomUUID().toString();
//
//
//        apiresponse = todoManager.api().put(String.format("project/%s", guid),  getSimpleParser(requestBody), new HttpHeadersBlock());
//        Assertions.assertEquals(201, apiresponse.getStatusCode());
//
//
//        EntityInstance newProject = project.findInstanceByFieldNameAndValue("guid", guid);
//        Assertions.assertEquals("12", newProject.getFieldValue("id").asString());
//
//        Assertions.assertEquals(13, project.getCounters().get("id").getCurrentValue());
//    }


    @Test
    public void putCanNotCreateAnEntityInstanceWithADuplicateId() {

        Map requestBody;
        ApiResponse apiresponse;

        project = todoManager.getThingInstancesNamed("project", EntityRelModel.DEFAULT_DATABASE_NAME);
        final Field anIdField = Field.is("id", FieldType.AUTO_INCREMENT);
        project.definition().addField(anIdField);

        final EntityInstance instance = project.addInstance(new EntityInstance(project.definition()));

        // Want to PUT
        requestBody = new HashMap<String, String>();
        String title = "My Office Work " + System.currentTimeMillis();
        requestBody.put("title", title);
        // duplicate id
        requestBody.put("id", String.valueOf(instance.getFieldValue("id").asString()));

        Assertions.assertEquals(1, project.countInstances());

        // create with a PUT and a given GUID
        String guid = UUID.randomUUID().toString();

        apiresponse = todoManager.api().put(String.format("project/%s", guid),  getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());

        Assertions.assertEquals(1, project.countInstances());
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
        apiresponse = todoManager.api().put(String.format("todo/%s", UUID.randomUUID().toString()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        EntityInstance paperwork = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "Todo for amending");

        // Mandatory field validation PUT amend
        requestBody = new HashMap<String, String>();
        // will generate 400 because description should be title
        requestBody.put("description", "Amended TODO Item ");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        // Field validation on boolean for Amend with PUT
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
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
        apiresponse = todoManager.api().put(String.format("todo/%s", UUID.randomUUID().toString()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

    }

    @Test
    public void putFailCannotAmendEntityInstanceWhenValidationErrors() {

        Map requestBody;
        ApiResponse apiresponse;


        EntityInstance paperwork = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "Todo for amending");

        // Mandatory field validation PUT amend
        requestBody = new HashMap<String, String>();
        // will generate 400 because description should be title
        requestBody.put("description", "Amended TODO Item ");
        requestBody.put("doneStatus", "TRUE");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());


        // Field validation on boolean for Amend with PUT
        requestBody = new HashMap<String, String>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse = todoManager.api().put(String.format("todo/%s", paperwork.getPrimaryKeyValue()), getSimpleParser(requestBody), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

    }
}
