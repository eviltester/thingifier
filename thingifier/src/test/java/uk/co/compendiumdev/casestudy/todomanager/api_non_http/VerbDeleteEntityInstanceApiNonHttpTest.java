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

import java.util.*;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
public class VerbDeleteEntityInstanceApiNonHttpTest {


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
    public void deleteAnEntityInstanceAPI() {
        ApiResponse apiresponse;

        EntityInstance officeWork = todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).createInstance(EntityInstanceDraft.forEntity(project).withField("title", "An Existing Project"));

        Assertions.assertEquals(1, todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).countInstances(project));

        apiresponse = todoManager.api().delete(String.format("project/%s", officeWork.getPrimaryKeyValue()), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() == 0);

        Assertions.assertFalse(apiresponse.hasABody());

        Assertions.assertEquals(0, todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).countInstances(project));

        apiresponse = todoManager.api().delete(String.format("project/%s", officeWork.getPrimaryKeyValue()), new HttpHeadersBlock());
        Assertions.assertEquals(404, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);

        Assertions.assertTrue(apiresponse.hasABody());

    }


    @Test
    public void deleteFailToDeleteAGUIDThatDoesNotExistAsAnEntityInstance() {

        ApiResponse apiresponse;

        apiresponse = todoManager.api().delete(String.format("project/%s", UUID.randomUUID().toString()), new HttpHeadersBlock());
        Assertions.assertEquals(404, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);
        Assertions.assertTrue(apiresponse.hasABody());

    }


}
