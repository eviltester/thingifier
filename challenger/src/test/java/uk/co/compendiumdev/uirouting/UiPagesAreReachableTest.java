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
        Assertions.assertTrue(response.body.contains("<meta property='og:type' content='website'>"));
        Assertions.assertTrue(response.body.contains("<meta property='og:url' content='https://apichallenges.eviltester.com'>"));
        Assertions.assertTrue(response.body.contains("application/ld+json"));
        Assertions.assertTrue(response.body.contains("\"@type\":\"Organization\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"WebSite\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"WebPage\""));
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
        args.add(Arguments.of(200, "Learning Utilities and Resources | API Challenges", "/learning"));
        args.add(Arguments.of(200, "Multi-User Instructions | API Challenges Guide", "/gui/multiuser"));
        args.add(Arguments.of(200, "API Documentation", "/docs"));
        args.add(Arguments.of(200, "HTTP Mirror Mode | API Challenges Practice Mode", "/practice-modes/mirror"));
        args.add(Arguments.of(200, "Simulation Mode | API Challenges Practice Mode", "/practice-modes/simulation"));
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
        Assertions.assertTrue(response.body.contains("\"openapi\" : \"3.0.1\","));
    }

    @Test
    void markdownPageWithMetadataOverridesRendersExpectedSeoAndSocialTags(){

        final HttpResponseDetails response = http.send("/seo-metadata-test-page", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<title>Open Graph Metadata Test Page for Validation | API Challenges</title>"));
        Assertions.assertTrue(response.body.contains("<meta name='description' content='Search snippet with Alan&#39;s &quot;special&quot; chars &amp; context.'>"));
        Assertions.assertTrue(response.body.contains("<meta name='robots' content='noindex,nofollow'>"));
        Assertions.assertTrue(response.body.contains("<meta property='og:type' content='article'>"));
        Assertions.assertTrue(response.body.contains("<meta property='og:url' content='https://apichallenges.eviltester.com/seo-metadata-test-page'>"));
        Assertions.assertTrue(response.body.contains("<meta property='og:image' content='https://apichallenges.eviltester.com/images/social/apichallenges-og-1200x630.png'>"));
        Assertions.assertTrue(response.body.contains("<meta property='og:image:alt' content='OG preview image for API Challenges metadata tests'>"));
        Assertions.assertTrue(response.body.contains("<meta name='twitter:card' content='summary'>"));
        Assertions.assertTrue(response.body.contains("<meta name='twitter:site' content='@apichallenges'>"));
        Assertions.assertTrue(response.body.contains("<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/social/apichallenges-og-1200x630.png'>"));
        Assertions.assertTrue(response.body.contains("\"@type\":\"Article\""));
        Assertions.assertTrue(response.body.contains("\"description\":\"Search snippet with Alan's \\\"special\\\" chars & context.\""));
        Assertions.assertTrue(response.body.contains("\"url\":\"https://apichallenges.eviltester.com/seo-metadata-test-page\""));
        Assertions.assertTrue(response.body.contains("\"dateModified\":\"2026-02-18\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"HowTo\""));
        Assertions.assertTrue(response.body.contains("\"name\":\"Open the metadata test page\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"VideoObject\""));
        Assertions.assertTrue(response.body.contains("\"contentUrl\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\""));
        Assertions.assertFalse(response.body.contains("\"@type\":\"BreadcrumbList\""));
    }

    @Test
    void markdownPageWithNoOptionalMetadataUsesFallbackDefaults(){

        final HttpResponseDetails response = http.send("/", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<meta name='description' content='A practice API application with tutorials for HTTP and REST APIs. Guided exercises and gamification hands on learning path.'>"));
        Assertions.assertTrue(response.body.contains("<meta name='robots' content='index,follow'>"));
        Assertions.assertTrue(response.body.contains("<meta property='og:type' content='website'>"));
        Assertions.assertTrue(response.body.contains("<meta property='og:url' content='https://apichallenges.eviltester.com'>"));
        Assertions.assertTrue(response.body.contains("<meta property='og:image' content='https://apichallenges.eviltester.com/images/social/apichallenges-og-1200x630.png'>"));
        Assertions.assertTrue(response.body.contains("<meta name='twitter:card' content='summary_large_image'>"));
        Assertions.assertTrue(response.body.contains("\"@type\":\"WebPage\""));
    }

    @Test
    void markdownContentPageDefaultsToArticleSchema(){

        final HttpResponseDetails response = http.send("/learning", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"@type\":\"Article\""));
        Assertions.assertTrue(response.body.contains("\"url\":\"https://apichallenges.eviltester.com/learning\""));
        Assertions.assertTrue(response.body.contains("\"mainEntityOfPage\":\"https://apichallenges.eviltester.com/learning\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"Person\""));
        Assertions.assertTrue(response.body.contains("\"name\":\"Alan Richardson\""));
        Assertions.assertTrue(response.body.contains("\"jobTitle\":\"Software Testing and Development Consultant\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"Organization\""));
        Assertions.assertTrue(response.body.contains("\"name\":\"eviltester.com\""));
        Assertions.assertTrue(response.body.contains("\"legalName\":\"Compendium Developments Ltd\""));
        Assertions.assertTrue(response.body.contains("\"dateModified\":\"2026-02-18\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"BreadcrumbList\""));
        Assertions.assertTrue(response.body.contains("<aside class='author-bio-snippet'"));
        Assertions.assertTrue(response.body.contains("href='/author/alan-richardson'"));
    }

    @Test
    void authorBioPageIsReachable(){

        final HttpResponseDetails response = http.send("/author/alan-richardson", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<title>Alan Richardson Author Profile and API Testing Credentials</title>"));
        Assertions.assertTrue(response.body.contains("<h1>About Alan Richardson</h1>"));
        Assertions.assertFalse(response.body.contains("<aside class='author-bio-snippet'"));
    }

    @Test
    void solutionPageEmitsHowToVideoAndBreadcrumbSchemasWithExplicitHowToSteps(){

        final HttpResponseDetails response = http.send("/apichallenges/solutions/get/get-todos-200", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"@type\":\"HowTo\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"HowToStep\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"VideoObject\""));
        Assertions.assertTrue(response.body.contains("\"contentUrl\":\"https://www.youtube.com/watch?v=OpisB0UZq0c\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"BreadcrumbList\""));
    }

    @Test
    void articleSchemaIncludesDatePublishedAndDateModifiedWhenDateAndLastmodExist(){

        final HttpResponseDetails response = http.send("/apichallenges/solutions/authentication/post-secret-201", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"datePublished\":\"2021-07-24T08:30:00Z\""));
        Assertions.assertTrue(response.body.contains("\"dateModified\":\"2026-02-18\""));
    }

    @Test
    void sitemapUsesFixedLastmodForPhaseOneUrls(){

        final HttpResponseDetails response = http.send("/sitemap.xml", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<loc>https://apichallenges.eviltester.com</loc>"));
        Assertions.assertTrue(response.body.contains("<loc>https://apichallenges.eviltester.com/docs</loc>"));
        Assertions.assertTrue(response.body.contains("<loc>https://apichallenges.eviltester.com/gui/challenges</loc>"));
        Assertions.assertTrue(response.body.contains("<lastmod>2026-02-18</lastmod>"));
    }

    static Stream<Arguments> legacyUrlRedirects(){
        List<Arguments> args = new ArrayList<>();
        args.add(Arguments.of("/apichallenges/solutions/method-overrides/all-method-overrides",
                "/apichallenges/solutions/method-override/all-method-overrides"));
        args.add(Arguments.of("/tools/clients/soapyi",
                "/tools/clients/soapui"));
        return args.stream();
    }

    @ParameterizedTest(name = "legacy url {0} redirects to {1}")
    @MethodSource("legacyUrlRedirects")
    void legacyUrlsRedirectToCanonicalContent(String legacyUrl, String canonicalUrl){
        final HttpResponseDetails response = http.send(legacyUrl, "get");

        Assertions.assertEquals(301, response.statusCode);
        Assertions.assertEquals(canonicalUrl, response.getHeader("Location"));
    }

}

