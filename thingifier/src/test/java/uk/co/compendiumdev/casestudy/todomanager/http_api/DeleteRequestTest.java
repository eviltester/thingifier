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
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class DeleteRequestTest {

    private Thingifier todoManager;

    EntityDefinition todo;
    EntityDefinition project;

    // TODO: need the http_api tests to achieve 100% of ThingifierRestApiHandler

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

    @Test
    public void canDeleteItem() {

        final EntityInstance instance =
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .createInstance(
                                EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        HttpApiRequest request = new HttpApiRequest("/todos/" + instance.getPrimaryKeyValue());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(
                0,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));
    }

    @Test
    public void cannotDeleteItemThatDoesNotExist() {

        final EntityInstance instance =
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .createInstance(
                                EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        HttpApiRequest request =
                new HttpApiRequest("/todos/" + instance.getPrimaryKeyValue() + "bob");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));
    }

    @Test
    public void cannotDeleteRootItem() {

        todoManager
                .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                .createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        HttpApiRequest request = new HttpApiRequest("/todos");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).delete(request);
        Assertions.assertEquals(405, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assertions.assertEquals(1, errors.errorMessages.length);
        Assertions.assertEquals("Cannot delete root level entity", errors.errorMessages[0]);
    }

    private class ErrorMessages {

        String[] errorMessages;
    }
}
