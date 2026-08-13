package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;

class ContentTypeHeaderParserTest {

    @Test
    void recognizesThingifierStructuredQueryJsonMediaType() {
        ContentTypeHeaderParser parser =
                new ContentTypeHeaderParser(
                        ThingifierHttpApi.STRUCTURED_QUERY_CONTENT_TYPE + "; charset=utf-8");

        Assertions.assertTrue(parser.isStructuredQueryJson());
    }

    @Test
    void doesNotRecognizeTodoSpecificStructuredQueryJsonMediaType() {
        ContentTypeHeaderParser parser =
                new ContentTypeHeaderParser(
                        "application/vnd.apichallenges." + "todo-" + "query+json");

        Assertions.assertFalse(parser.isStructuredQueryJson());
    }
}
