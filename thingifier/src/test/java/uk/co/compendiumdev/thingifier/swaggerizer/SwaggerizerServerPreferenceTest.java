package uk.co.compendiumdev.thingifier.swaggerizer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;

class SwaggerizerServerPreferenceTest {

    @Test
    void keepsLocalhostFirstWhenCurrentRequestIsLocalhost() {
        final String json =
                new Swaggerizer(apiDefn()).asJsonWithPreferredServer("http://localhost:4567");

        Assertions.assertTrue(
                json.indexOf("\"url\" : \"http://localhost:4567\"")
                        < json.indexOf("\"url\" : \"https://apichallenges.eviltester.com\""));
        Assertions.assertFalse(json.contains("\"description\" : \"current request\""));
    }

    @Test
    void prefersConfiguredHttpsServerForSameHostWhenCurrentRequestIsHttp() {
        final String json =
                new Swaggerizer(apiDefn())
                        .asJsonWithPreferredServer("http://apichallenges.eviltester.com");

        Assertions.assertTrue(
                json.indexOf("\"url\" : \"https://apichallenges.eviltester.com\"")
                        < json.indexOf("\"url\" : \"http://localhost:4567\""));
        Assertions.assertFalse(json.contains("\"url\" : \"http://apichallenges.eviltester.com\""));
        Assertions.assertFalse(json.contains("\"description\" : \"current request\""));
    }

    private ThingifierApiDocumentationDefn apiDefn() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.addServer("https://apichallenges.eviltester.com", "cloud hosted version");
        apiDefn.addServer("http://localhost:4567", "local execution");
        return apiDefn;
    }
}
