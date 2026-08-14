package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;

class ContentTypeHeaderParserTest {

    @Test
    void recognizesSupportedXmlMediaTypes() {
        for (String mediaType : List.of("application/xml", "text/xml")) {
            Assertions.assertTrue(new ContentTypeHeaderParser(mediaType).isXML(), mediaType);
        }
    }

    @Test
    void recognizesModelMatchingStructuredXmlMediaTypes() {
        for (String mediaType :
                List.of(
                        "application/todo+xml",
                        "application/todos+xml",
                        "application/vnd.example.todo+xml",
                        "application/vnd.example.todo+xml; charset=utf-8")) {
            Assertions.assertTrue(
                    new ContentTypeHeaderParser(mediaType).isXML(List.of("todo", "todos")),
                    mediaType);
        }
    }

    @Test
    void doesNotTreatUnsupportedXmlBasedMediaTypesAsNormalXml() {
        for (String mediaType :
                List.of(
                        "application/vnd.example.project+xml",
                        "application/problem+xml",
                        "application/soap+xml",
                        "application/xhtml+xml",
                        "image/svg+xml",
                        "application/atom+xml",
                        "application/rss+xml",
                        "application/*+xml")) {
            Assertions.assertFalse(
                    new ContentTypeHeaderParser(mediaType).isXML(List.of("todo", "todos")),
                    mediaType);
        }
    }

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
