package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

import java.util.HashSet;
import java.util.Set;


import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
public class VerbGetEntityInstanceApiNonHttpTest {


    private Thingifier todoManager;

    EntityDefinition todo;
    EntityDefinition project;


    // TODO: tests that use the TodoManagerModel were created early and are too complicated - simplify
    // when the thingifier was a prototype and we were building the todo manager at the same
    // time this saved time. Now, the tests are too complicated to maintain because the TodoManagerModel
    // is complex. We should simplify these tests and move them into the actual standAlone
    // projects
    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");
        project = todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("project");

    }
    
       /*


    Non HTTP API Based Tests


    */


    @Test
    public void getCanReturnASingleEntityInstance() {

        todoManager.apiConfig().setReturnSingleGetItemsAsCollection(false);

        // add some data
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));


        EntityInstance findThis = todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));

        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));


        ApiResponse apiResponse = todoManager.api().get("/todo/" + findThis.getPrimaryKeyValue(), new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, apiResponse.getStatusCode());
        Assertions.assertFalse(apiResponse.isCollection(),
                "Should be a single item, rather than a collection");
        Assertions.assertTrue(apiResponse.hasABody());

        Assertions.assertEquals(findThis.getFieldValue("title").asString(), apiResponse.getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(findThis.getFieldValue("guid").asString(), apiResponse.getReturnedInstance().getFieldValue("guid").asString());

        Assertions.assertEquals(findThis, apiResponse.getReturnedInstance());
        Assertions.assertEquals(0, apiResponse.getErrorMessages().size());

    }

    @Test
    public void getCanReturnASingleEntityInstanceAsACollection() {

        todoManager.apiConfig().setReturnSingleGetItemsAsCollection(true);

        // add some data
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));


        EntityInstance findThis = todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));

        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));


        ApiResponse apiResponse = todoManager.api().get("/todo/" + findThis.getPrimaryKeyValue(), new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, apiResponse.getStatusCode());
        Assertions.assertTrue(apiResponse.isCollection(), "Should be a configured to return a collection");
        Assertions.assertTrue(apiResponse.hasABody());

        EntityInstance instance = apiResponse.getReturnedInstanceCollection().get(0);
        Assertions.assertEquals(findThis.getFieldValue("title").asString(), instance.getFieldValue("title").asString());
        Assertions.assertEquals(findThis.getFieldValue("guid").asString(), instance.getFieldValue("guid").asString());

        Assertions.assertEquals(findThis, instance);
        Assertions.assertEquals(0, apiResponse.getErrorMessages().size());

    }

    @Test
    public void getCanReturnMultipleEntityInstances() {

        todoManager.apiConfig().setReturnSingleGetItemsAsCollection(false);

        // add some data
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));


        ApiResponse apiResponse = todoManager.api().get("/todo", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, apiResponse.getStatusCode());
        Assertions.assertTrue(apiResponse.isCollection(),
                "Should be a collection");
        Assertions.assertTrue(apiResponse.hasABody());

        Assertions.assertEquals(todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).countInstances(todo), apiResponse.getReturnedInstanceCollection().size());

        Set<String> guidSet = new HashSet<>();

        for (EntityInstance item : apiResponse.getReturnedInstanceCollection()) {
            guidSet.add(item.getPrimaryKeyValue());
            Assertions.assertNotNull(todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).findInstanceByPrimaryKey(todo, item.getPrimaryKeyValue()));
        }

        Assertions.assertEquals(guidSet.size(), todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).countInstances(todo));
        Assertions.assertEquals(guidSet.size(), apiResponse.getReturnedInstanceCollection().size());

        Assertions.assertEquals(0, apiResponse.getErrorMessages().size());

    }

    @Test
    public void getCanReturnMultipleEntityInstancesFromEndpoint() {

        todoManager.apiConfig().setReturnSingleGetItemsAsCollection(true);

        // add some data
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));
        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "My Title" + System.nanoTime()));


        ApiResponse apiResponse = todoManager.api().get("/todo", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, apiResponse.getStatusCode());
        Assertions.assertTrue(apiResponse.isCollection(),
                "Should be a collection");
        Assertions.assertTrue(apiResponse.hasABody());

        Assertions.assertEquals(todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).countInstances(todo), apiResponse.getReturnedInstanceCollection().size());

        Set<String> guidSet = new HashSet<>();

        for (EntityInstance item : apiResponse.getReturnedInstanceCollection()) {
            guidSet.add(item.getPrimaryKeyValue());
            Assertions.assertNotNull(todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).findInstanceByPrimaryKey(todo, item.getPrimaryKeyValue()));
        }

        Assertions.assertEquals(guidSet.size(), todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).countInstances(todo));
        Assertions.assertEquals(guidSet.size(), apiResponse.getReturnedInstanceCollection().size());

        Assertions.assertEquals(0, apiResponse.getErrorMessages().size());

    }


}
