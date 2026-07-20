package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import com.google.gson.Gson;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class VerbPostEntityInstanceApiNonHttpTest {

    private Thingifier todoManager;

    EntityDefinition todo;
    EntityDefinition project;

    // TODO: tests that use the TodoManagerModel were created early and are too complicated -
    // simplify
    // when the thingifier was a prototype and we were building the todo manager at the same
    // time this saved time. Now, the tests are too complicated to maintain because the
    // TodoManagerModel
    // is complex. We should simplify these tests and move them into the actual standAlone
    // projects
    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");
        project =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("project");
    }

    /*


    Non HTTP API Based Tests


    */

    @Test
    public void postCanCreateAnEntityWhichPassesValidationWithAllFields() {

        // POST project
        Map<String, String> requestBody = new HashMap<>();
        String title = "My Office Work" + System.currentTimeMillis();
        String description = "MyDescription " + System.currentTimeMillis();
        String doneStatus = "true";

        requestBody.put("title", title);
        requestBody.put("description", description);
        requestBody.put("doneStatus", doneStatus);

        // create a project with POST
        ApiResponse apiresponse =
                todoManager
                        .api()
                        .post("todo", getSimpleParser(requestBody), new HttpHeadersBlock());

        Assertions.assertEquals(201, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(0, apiresponse.getErrorMessages().size());

        EntityInstance createdInstance = apiresponse.getReturnedInstance();

        String officeWorkGuid = createdInstance.getPrimaryKeyValue();
        Assertions.assertEquals(title, createdInstance.getFieldValue("title").asString());
        Assertions.assertEquals(
                description, createdInstance.getFieldValue("description").asString());
        Assertions.assertEquals(doneStatus, createdInstance.getFieldValue("doneStatus").asString());

        // Check header for GUID
        String headerLocation = apiresponse.getHeaderValue("Location");
        String headerGUID = apiresponse.getHeaderValue(ApiResponse.PRIMARY_KEY_HEADER);

        Assertions.assertEquals(headerGUID, officeWorkGuid);
        Assertions.assertEquals("/todos/" + officeWorkGuid, headerLocation);

        // check that it is created in the model

        EntityInstance createdProject =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByPrimaryKey(todo, headerGUID);

        Assertions.assertEquals(createdProject, createdInstance);
    }

    @Test
    public void postFailsWithConflictWhenMaximumInstanceLimitIsReached() {
        Thingifier limitedThingifier = new Thingifier();
        EntityDefinition ticket = limitedThingifier.defineThing("ticket", "tickets", 1);
        ticket.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        ticket.addField(Field.is("title", FieldType.STRING));

        Map<String, String> first = new HashMap<>();
        first.put("title", "First");
        limitedThingifier
                .api()
                .post("tickets", parserFor(limitedThingifier, first), new HttpHeadersBlock());

        Map<String, String> second = new HashMap<>();
        second.put("title", "Second");
        ApiResponse apiresponse =
                limitedThingifier
                        .api()
                        .post(
                                "tickets",
                                parserFor(limitedThingifier, second),
                                new HttpHeadersBlock());

        Assertions.assertEquals(409, apiresponse.getStatusCode());
        Assertions.assertEquals(
                "ERROR: Cannot add instance, maximum limit of 1 reached",
                apiresponse.getErrorMessages().iterator().next());
    }

    private BodyParser getSimpleParser(final Map<String, String> requestBody) {

        final HttpApiRequest arequest =
                new HttpApiRequest("/path").setBody(new Gson().toJson(requestBody));
        return new BodyParser(arequest, todoManager.getThingNames());
    }

    private BodyParser parserFor(
            final Thingifier thingifier, final Map<String, String> requestBody) {
        final HttpApiRequest request =
                new HttpApiRequest("/path").setBody(new Gson().toJson(requestBody));
        return new BodyParser(request, thingifier.getThingNames());
    }

    @Test
    public void postCanCreateAnEntityWhichPassesValidationWithMinimumFields() {

        // POST project
        Map<String, String> requestBody = new HashMap<>();
        String title = "My Office Work" + System.currentTimeMillis();

        requestBody.put("title", title);

        // create a project with POST
        ApiResponse apiresponse =
                todoManager
                        .api()
                        .post("todo", getSimpleParser(requestBody), new HttpHeadersBlock());

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

        EntityInstance createdProject =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByPrimaryKey(todo, headerGUID);

        Assertions.assertEquals(createdProject, createdInstance);
    }

    @Test
    public void postCanAmendAnExistingEntity() {

        EntityInstance relTodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "Todo for amending"));

        // POST project
        Map<String, String> requestBody = new HashMap<>();
        String title = "My New Title" + System.currentTimeMillis();
        String description = "My Description " + System.currentTimeMillis();

        requestBody.put("title", title);
        requestBody.put("description", description);

        // amend a project with POST
        ApiResponse apiresponse =
                todoManager
                        .api()
                        .post(
                                "todo/" + relTodo.getPrimaryKeyValue(),
                                getSimpleParser(requestBody),
                                new HttpHeadersBlock());

        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals(0, apiresponse.getHeaders().size());
        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(0, apiresponse.getErrorMessages().size());

        // Check response

        EntityInstance createdInstance = apiresponse.getReturnedInstance();

        Assertions.assertEquals(relTodo.getPrimaryKeyValue(), createdInstance.getPrimaryKeyValue());
        Assertions.assertEquals(title, createdInstance.getFieldValue("title").asString());
        Assertions.assertEquals(
                description, createdInstance.getFieldValue("description").asString());
        Assertions.assertEquals("false", createdInstance.getFieldValue("doneStatus").asString());
    }

    @Test
    public void postFailCannotCreateProjectWithGuidInUrl() {

        int currentProjects =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(project);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", "My Office Work");

        String guid = UUID.randomUUID().toString();

        ApiResponse apiresponse =
                todoManager
                        .api()
                        .post(
                                String.format("project/%s", guid),
                                getSimpleParser(requestBody),
                                new HttpHeadersBlock());
        Assertions.assertEquals(404, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

        Assertions.assertEquals(
                currentProjects,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(project));
    }

    @Test
    public void postFailCannotAmendEntityInstanceWhenValidationErrorsAPI() {

        Map<String, String> requestBody;
        ApiResponse apiresponse;

        String originalTitle = "Todo for amending " + System.currentTimeMillis();
        String originalDescription = "my description " + System.currentTimeMillis();

        EntityInstance amendTodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", originalTitle)
                                        .withField("description", originalDescription));

        // Mandatory field validation
        requestBody = new HashMap<>();
        requestBody.put("title", "");
        requestBody.put("description", "Amend Failed new TODO Item");
        apiresponse =
                todoManager
                        .api()
                        .post(
                                String.format("todo/%s", amendTodo.getPrimaryKeyValue()),
                                getSimpleParser(requestBody),
                                new HttpHeadersBlock());

        Assertions.assertEquals(422, apiresponse.getStatusCode());
        Assertions.assertFalse(apiresponse.getErrorMessages().isEmpty());
        Assertions.assertTrue(apiresponse.hasABody());

        Assertions.assertEquals(originalTitle, amendTodo.getFieldValue("title").asString());
        Assertions.assertEquals(
                originalDescription, amendTodo.getFieldValue("description").asString());

        requestBody = new HashMap<>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("description", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");
        apiresponse =
                todoManager
                        .api()
                        .post(
                                String.format("todo/%s", amendTodo.getPrimaryKeyValue()),
                                getSimpleParser(requestBody),
                                new HttpHeadersBlock());

        Assertions.assertEquals(422, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

        Assertions.assertEquals(originalTitle, amendTodo.getFieldValue("title").asString());
        Assertions.assertEquals(
                originalDescription, amendTodo.getFieldValue("description").asString());
    }

    @Test
    public void putCanAmendExistingProject() {

        Map<String, String> requestBody;
        ApiResponse apiresponse;

        // PUT

        requestBody = new HashMap<>();
        requestBody.put("title", "My Office Work");

        EntityInstance officeWork =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "An Existing Project"));

        String officeWorkGuid = officeWork.getPrimaryKeyValue();
        Assertions.assertNotNull(officeWorkGuid);

        // amend existing project with PUT - this should validate that all required fields are
        // present
        apiresponse =
                todoManager
                        .api()
                        .put(
                                String.format("project/%s", officeWork.getPrimaryKeyValue()),
                                getSimpleParser(requestBody),
                                new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        officeWork =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByPrimaryKey(project, officeWorkGuid);
        Assertions.assertEquals("My Office Work", officeWork.getFieldValue("title").asString());

        officeWork =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .patch(
                                officeWork,
                                EntityInstanceDraft.forEntity(officeWork.getEntity())
                                        .withField("title", "office"));
        Assertions.assertEquals("office", officeWork.getFieldValue("title").asString());
        Assertions.assertNotNull(officeWorkGuid);

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(
                officeWorkGuid, apiresponse.getReturnedInstance().getPrimaryKeyValue());
    }

    @Test
    public void postFailCannotCreateEntityInstanceWhenValidationErrorsAPI() {

        Map<String, String> requestBody;
        ApiResponse apiresponse;

        // Mandatory field validation on POST create - must have a title
        requestBody = new HashMap<>();
        requestBody.put("description", "A new TODO Item"); // 422 because it should be "title"

        apiresponse =
                todoManager
                        .api()
                        .post(
                                String.format("todo"),
                                getSimpleParser(requestBody),
                                new HttpHeadersBlock());
        Assertions.assertEquals(422, apiresponse.getStatusCode());
        Assertions.assertFalse(apiresponse.getErrorMessages().isEmpty());
        Assertions.assertEquals(
                "title : field is mandatory", apiresponse.getErrorMessages().iterator().next());
        Assertions.assertTrue(apiresponse.hasABody());

        // Field validation on boolean for Create with POST
        requestBody = new HashMap<>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        apiresponse =
                todoManager
                        .api()
                        .post(
                                String.format("todo"),
                                getSimpleParser(requestBody),
                                new HttpHeadersBlock());
        Assertions.assertEquals(422, apiresponse.getStatusCode());
        Assertions.assertFalse(apiresponse.getErrorMessages().isEmpty());
        Assertions.assertTrue(apiresponse.hasABody());

        // Field validation on boolean for Amend with POST
        requestBody = new HashMap<>();
        requestBody.put("title", "A new TODO Item");
        requestBody.put("doneStatus", "FALSEY");

        EntityInstance paperwork =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "Todo for amending"));

        apiresponse =
                todoManager
                        .api()
                        .post(
                                String.format("todo/%s", paperwork.getPrimaryKeyValue()),
                                getSimpleParser(requestBody),
                                new HttpHeadersBlock());
        Assertions.assertEquals(422, apiresponse.getStatusCode());
        Assertions.assertFalse(apiresponse.getErrorMessages().isEmpty());
        Assertions.assertTrue(apiresponse.hasABody());
    }
}
