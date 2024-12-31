package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.*;

public class VerbDeleteEntityInstanceApiNonHttpTest {


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
    public void deleteAnEntityInstanceAPI() {
        ApiResponse apiresponse;

        EntityInstance officeWork = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "An Existing Project");

        Assertions.assertEquals(1, project.countInstances());

        apiresponse = todoManager.api().delete(String.format("project/%s", officeWork.getPrimaryKeyValue()), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() == 0);

        Assertions.assertFalse(apiresponse.hasABody());

        Assertions.assertEquals(0, project.countInstances());

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
