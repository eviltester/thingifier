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

public class VerbDeleteEntityInstanceApiNonHttpTest {


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


}
