package uk.co.compendiumdev.uirouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UiPagesAreReachableTest {

    /*
           Check UI routing without spinning up a browser test tool
    */

    private static HttpMessageSender http;
    private static ChallengerAuthData challenger;

    @BeforeAll
    static void createHttp(){
        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        http = new HttpMessageSender(Environment.getBaseUri());

        // Basic Browser Headers
        http.clearHeaders();
        http.setHeader("ContentType", "text/html; charset=utf-8");
        http.setHeader("Accept", "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8");
    }

    @Test
    void noProcessingWhenNoBasicAuth(){


        final HttpResponseDetails response = http.send("/", "get");

        Assertions.assertEquals(200, response.statusCode);
        assertContainsHeaderAndFooter(response);

    }

    @Test
    void receive404onMissingPage(){

        final HttpResponseDetails response = http.send("/bob", "get");

        Assertions.assertEquals(404, response.statusCode);
    }

    @Test
    void simulated404PageExistsAndReportsAs404(){

        // we currently don't have 404 because of the way the app is constructed
        // instead we should trap a 404 response and return a 307 redirecting to
        // 404 page with the original url appended to allow javascript to render
        // as if it was a 404 page

        final HttpResponseDetails response = http.send("/gui/404", "get");

        Assertions.assertEquals(404, response.statusCode);
        assertContainsHeaderAndFooter(response);
        Assertions.assertTrue(response.body.contains("<h1>Page Not Found</h1>"));
    }

    @Test
    void simulated404PageExistsAndReportsAs404WithPath(){

        // we currently don't have 404 because of the way the app is constructed
        // instead we should trap a 404 response and return a 307 redirecting to
        // 404 page with the original url appended to allow javascript to render
        // as if it was a 404 page

        final HttpResponseDetails response = http.send("/gui/404/bob/dobbs", "get");

        Assertions.assertEquals(404, response.statusCode);
        assertContainsHeaderAndFooter(response);
        Assertions.assertTrue(response.body.contains("<h1>Page Not Found</h1>"));
    }

    static Stream<Arguments> simplePageRoutingStatus(){
        List<Arguments> args = new ArrayList<>();

        // home page
        args.add(Arguments.of(200, "The API Challenges - API Tutorials and API Testing Practice Exercises", ""));
        args.add(Arguments.of(200, "The API Challenges - API Tutorials and API Testing Practice Exercises", "/"));
        // entities
        args.add(Arguments.of(200, "Entities Menu", "/gui/entities"));
        args.add(Arguments.of(200, "todo Instances", "gui/instances?entity=todo"));

        // Challenges
        args.add(Arguments.of(200, "API Challenges - Improve your API Skills", "/gui/challenges"));
        args.add(Arguments.of(200, "API Challenges - Improve your API Skills", "/gui/challenges/unkownchallenger"));

        // Additional Pages
        args.add(Arguments.of(200, "Learning Utilities and Resources", "/learning"));
        args.add(Arguments.of(200, "API Documentation", "/docs"));
        args.add(Arguments.of(200, "API Challenges Mirror Mode", "/practice-modes/mirror"));
        args.add(Arguments.of(200, "API Challenges Simulation Mode", "/practice-modes/simulation"));
        return args.stream();
    }



    @ParameterizedTest(name = "simple known page routing expected {0} for {1} {2}")
    @MethodSource("simplePageRoutingStatus")
    void simplePageRoutingTest(int statusCode, String title, String url){
        final HttpResponseDetails response =
                http.send(url, "get");

        Assertions.assertEquals(statusCode, response.statusCode);
        Assertions.assertTrue
                (response.body.contains(String.format("<title>%s</title>", title)),
                String.format("Title not found %s", title));
        assertContainsHeaderAndFooter(response);
    }

    private void assertContainsHeaderAndFooter(HttpResponseDetails response) {

        if(!response.body.contains("<div class=\"css-menu\">")){
            Assertions.fail("Page did not contain header menu");
        }
        if(!response.body.contains("<div class='footer'>")){
            Assertions.fail("Page did not contain footer");
        }
        if(!response.body.contains("Copyright Compendium Developments")){
            Assertions.fail("Page did not contain full page");
        }
    }


    @Test
    void canDownloadSwaggerFile(){

        // we currently don't have 404 because of the way the app is constructed
        // instead we should trap a 404 response and return a 307 redirecting to
        // 404 page with the original url appended to allow javascript to render
        // as if it was a 404 page

        final HttpResponseDetails response = http.send("/docs/swagger", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("attachment; filename=\"Simple-Todo-List-swagger.json\"", response.getHeader("Content-Disposition"));
        Assertions.assertTrue(response.body.contains("\"openapi\": \"3.0.1\","));
    }

}
