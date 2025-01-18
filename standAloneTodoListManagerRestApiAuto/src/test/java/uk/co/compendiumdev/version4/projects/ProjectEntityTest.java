package uk.co.compendiumdev.version4.projects;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.version4.api.Api;
import uk.co.compendiumdev.version4.api.Payloads;

public class ProjectEntityTest {


    @Test
    void canCreateAProject(){

        Payloads.ProjectPayload proj = new Payloads.ProjectPayload();
        proj.title = "A Created Project";
        final Response response = Api.createProject(proj);

        Assertions.assertEquals(201, response.getStatusCode());

        final Payloads.ProjectPayload created = response.body().as(
                Payloads.ProjectPayload.class);
        Assertions.assertEquals("A Created Project", created.title);
    }

    @Test
    void canCreateAMinimalProject(){

        Payloads.ProjectPayload proj = new Payloads.ProjectPayload();
        final Response response = Api.createProject(proj);

        Assertions.assertEquals(201, response.getStatusCode());
    }


    @Test
    void canGetASpecificProject(){

        Payloads.ProjectPayload proj = new Payloads.ProjectPayload();
        proj.title = "A Created Project";
        final Response response = Api.createProject(proj);
        final Payloads.ProjectPayload created =
                response.body().as(Payloads.ProjectPayload.class);

        Response getresponse = RestAssured.
                get(Environment.getEnv("/projects/" + created.id));
        Assertions.assertEquals(200, getresponse.getStatusCode());

        final Payloads.ProjectsPayload retrieved =
                getresponse.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertEquals(1, retrieved.projects.size());

        Payloads.ProjectPayload projectretrieved = retrieved.projects.get(0);
        Assertions.assertEquals("A Created Project", projectretrieved.title);
        Assertions.assertEquals(created.id, projectretrieved.id);
    }

    @Test
    void canDeleteASpecificProject(){

        Response response = Api.createProject(new Payloads.ProjectPayload());
        final Payloads.ProjectPayload created =
                response.body().as(Payloads.ProjectPayload.class);

        // delete the project we just created
        response = RestAssured.
                delete(Environment.getEnv("/projects/" + created.id));
        Assertions.assertEquals(200, response.getStatusCode());

        // check it has gone
        response = RestAssured.
                get(Environment.getEnv("/projects/" + created.id));
        Assertions.assertEquals(404, response.getStatusCode());
    }
}
