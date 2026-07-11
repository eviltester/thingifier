package uk.co.compendiumdev.casestudy.todomanager.http_api;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class RelationshipHttpTest {

    private Thingifier todoManager;

    EntityDefinition todo;
    EntityDefinition project;
    EntityDefinition categories;

    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");
        project =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("project");
        categories =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("category");
    }

    @Test
    public void canCreateARelationshipBetweenProjectAndTodoViaTasks() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todo).withField("title", "a TODO"));

        final EntityInstance aproject =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "a Project"));

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());

        HttpApiRequest request =
                new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", atodo.getPrimaryKeyValue());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());
    }

    @Test
    public void canCreateARelationshipBetweenProjectAndTodoViaTasksUsingID() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todo).withField("title", "a TODO"));

        final EntityInstance aproject =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "a Project"));

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());

        HttpApiRequest request =
                new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"guid":"%s"}
        String body = String.format("{\"id\":\"%s\"}", atodo.getFieldValue("id").asString());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());
    }

    @Test
    public void canCreateARelationshipAndTodoBetweenProjectAndTodoViaTasks() {

        final EntityInstance aproject =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "a Project"));

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());
        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        HttpApiRequest request =
                new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"title":"My New To do"}
        String body = "{\"title\":\"My New To do\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        final EntityInstance inMemoryTodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByPrimaryKey(
                                todo, response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER));
        Assertions.assertTrue(
                response.getBody().contains(inMemoryTodo.getPrimaryKeyValue()), response.getBody());
    }

    @Test
    public void cannotCreateARelationshipBetweenProjectAndCategoryViaTasks() {

        final EntityInstance acategory =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(categories)
                                        .withField("title", "a Category"));

        final EntityInstance aproject =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "a Project"));

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());

        HttpApiRequest request =
                new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", acategory.getPrimaryKeyValue());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(404, response.getStatusCode());

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assertions.assertEquals(1, errors.errorMessages.length);

        Assertions.assertEquals(
                "Could not find thing matching value for guid", errors.errorMessages[0]);
    }

    @Test
    public void cannotCreateARelationshipWhenGivenGuidDoesNotExist() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todo).withField("title", "a TODO"));

        final EntityInstance aproject =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "a Project"));

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());

        HttpApiRequest request =
                new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", atodo.getPrimaryKeyValue() + "bob");
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(404, response.getStatusCode());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);
        Assertions.assertEquals(1, errors.errorMessages.length);

        Assertions.assertTrue(
                errors.errorMessages[0].startsWith("Could not find thing"),
                errors.errorMessages[0]);
    }

    // need to see if I can create where a relationship name is the same as a plural entity
    @Test
    public void canCreateARelationshipBetweenCategoryAndTodoViaTodos() {

        final EntityInstance acategory =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(categories)
                                        .withField("title", "a Category"));

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todo).withField("title", "a TODO"));

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(acategory, "todos")
                        .size());

        HttpApiRequest request =
                new HttpApiRequest("categories/" + acategory.getPrimaryKeyValue() + "/todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"guid":"%s"}
        String body = String.format("{\"guid\":\"%s\"}", atodo.getPrimaryKeyValue());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(acategory, "todos")
                        .size());
    }

    @Test
    public void canCreateARelationshipAndTodoBetweenCategoryAndTodoViaTodos() {

        final EntityInstance acategory =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(categories)
                                        .withField("title", "a Category"));

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(acategory, "todos")
                        .size());
        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        HttpApiRequest request =
                new HttpApiRequest("categories/" + acategory.getPrimaryKeyValue() + "/todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"title":"My New To do"}
        String body = "{\"title\":\"My New To do\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(acategory, "todos")
                        .size());
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        final EntityInstance inMemoryTodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByPrimaryKey(
                                todo, response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER));
        Assertions.assertTrue(
                response.getBody().contains(inMemoryTodo.getPrimaryKeyValue()), response.getBody());
    }

    @Test
    public void canCreateARelationshipBetweenProjectAndTodoViaTasksUsingXml() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todo).withField("title", "a TODO"));

        final EntityInstance aproject =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "a Project"));

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());

        HttpApiRequest request =
                new HttpApiRequest("projects/" + aproject.getPrimaryKeyValue() + "/tasks");
        request.getHeaders().putAll(HeadersSupport.containsXml());

        String body = String.format("<todo><guid>%s</guid></todo>", atodo.getPrimaryKeyValue());
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());
    }

    @Test
    public void canDeleteARelationshipBetweenProjectAndTodoViaTasks() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todo).withField("title", "a TODO"));

        final EntityInstance aproject =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "a Project"));

        todoManager
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .relationships()
                .connect(aproject, "tasks", atodo);

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(project));

        HttpApiRequest request =
                new HttpApiRequest(
                        "projects/"
                                + aproject.getPrimaryKeyValue()
                                + "/tasks/"
                                + atodo.getPrimaryKeyValue());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(200, response.getStatusCode());

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(aproject, "tasks")
                        .size());
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(project));
    }

    // need to see if I can delete where a relationship name is the same as a plural entity
    @Test
    public void canDeleteARelationshipBetweenCategoryAndTodoViaTodos() {

        final EntityInstance acategory =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(categories)
                                        .withField("title", "a Category"));

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todo).withField("title", "a TODO"));

        todoManager
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .relationships()
                .connect(acategory, "todos", atodo);

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(acategory, "todos")
                        .size());
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(categories));

        final HttpApiRequest request =
                new HttpApiRequest(
                        "categories/"
                                + acategory.getPrimaryKeyValue()
                                + "/todos/"
                                + atodo.getPrimaryKeyValue());

        HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(200, response.getStatusCode());

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(acategory, "todos")
                        .size());
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(categories));

        // if relationship doesn't exist, I should get a 404 if I reissue therequest
        response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(404, response.getStatusCode());

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(acategory, "todos")
                        .size());
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(categories));
    }

    /**
     * Optional Relationships - Mandatory
     *
     * <p>can not create an estimate without a to do can create an estimate when added to a to do
     * directly because relationship is created when delete a to do the estimate is also deleted GET
     * estimates for a to do GET to dos for an estimate TODO: amend relationship to move estimate to
     * another todo (implement with relationships as fields in the object e.g. "todos" : [{"guid":
     * "xxx-xxx-xxx-xxx"}]) TODO: cardinality validation on relationship fields e.g. max of 2 etc.
     * TODO: create 'proposed objects' and validate those rather than create and delete (will
     * support amend validation as well)
     */

    // can not create an estimate on its own, without a todo
    @Test
    public void canNotCreateEstimateWithoutMandatoryRelationship() {

        HttpApiRequest request = new HttpApiRequest("estimate");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        String body = "{\"duration\":\"3\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(400, response.getStatusCode());

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(
                                todoManager
                                        .getERmodel()
                                        .getSchema()
                                        .getDefinitionWithSingularOrPluralNamed("estimate")));
    }

    @Test
    public void canCreateAnEstimateForTodoMandatoryRelationship() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "a TODO for estimating"));

        HttpApiRequest request =
                new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue() + "/estimates");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        String body = "{\"duration\":\"3\"}";
        request.setBody(body);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);
        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(
                                todoManager
                                        .getERmodel()
                                        .getSchema()
                                        .getDefinitionWithSingularOrPluralNamed("estimate")));
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(atodo, "estimates")
                        .size());
    }

    @Test
    public void canDeleteAnEstimateWhenTodoDeletedBecauseOfMandatoryRelationship() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "a TODO for estimating"));

        final EntityDefinition estimates =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("estimate");
        final EntityInstance anEstimate =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(estimates)
                                        .withField("duration", "7"));

        todoManager
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .relationships()
                .connect(anEstimate, "estimate", atodo);

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(atodo, "estimates")
                        .size());
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(estimates));
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        final HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue());

        HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(200, response.getStatusCode());

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));
        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(estimates));
    }

    @Test
    public void canGetEstimatesViaRelationship() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "a TODO for estimating"));

        final EntityDefinition estimates =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("estimate");
        final EntityInstance anEstimate =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(estimates)
                                        .withField("duration", "7")
                                        .withField("description", "an estimate"));

        todoManager
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .relationships()
                .connect(anEstimate, "estimate", atodo);

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(atodo, "estimates")
                        .size());
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(estimates));
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        HttpApiRequest request =
                new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue() + "/estimates");

        HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());

        System.out.println(response.getBody());

        final EstimateCollectionResponse estimatesfound =
                new Gson().fromJson(response.getBody(), EstimateCollectionResponse.class);

        Assertions.assertEquals(1, estimatesfound.estimates.length);
        Assertions.assertEquals("7", estimatesfound.estimates[0].duration);
        Assertions.assertEquals("an estimate", estimatesfound.estimates[0].description);

        request = new HttpApiRequest("estimates/" + anEstimate.getPrimaryKeyValue() + "/estimate");

        response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());

        System.out.println(response.getBody());

        final TodoCollectionResponse todosfound =
                new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assertions.assertEquals(1, todosfound.todos.length);
        Assertions.assertEquals("a TODO for estimating", todosfound.todos[0].title);
    }

    private class TodoCollectionResponse {

        Todo[] todos;
    }

    private class EstimateCollectionResponse {

        Estimate[] estimates;
    }

    private class Estimate {

        String duration;
        String description;
    }

    private class Todo {

        String guid;
        String title;
    }

    private class ErrorMessages {

        String[] errorMessages;
    }
}
