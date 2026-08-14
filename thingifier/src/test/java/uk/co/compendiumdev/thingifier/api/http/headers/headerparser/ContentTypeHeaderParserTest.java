package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;

class ContentTypeHeaderParserTest {

    @ParameterizedTest
    @MethodSource("supportedXmlMediaTypes")
    void recognizesSupportedXmlMediaType(final String mediaType) {
        Assertions.assertTrue(new ContentTypeHeaderParser(mediaType).isXML(), mediaType);
    }

    @ParameterizedTest
    @MethodSource("modelMatchingStructuredXmlMediaTypes")
    void recognizesModelMatchingStructuredXmlMediaType(final String mediaType) {
        Assertions.assertTrue(
                new ContentTypeHeaderParser(mediaType).isXML(List.of("todo", "todos")), mediaType);
    }

    @ParameterizedTest
    @MethodSource("unsupportedXmlBasedMediaTypes")
    void doesNotTreatUnsupportedXmlBasedMediaTypeAsNormalXml(final String mediaType) {
        Assertions.assertFalse(
                new ContentTypeHeaderParser(mediaType).isXML(List.of("todo", "todos")), mediaType);
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

    private static Stream<String> supportedXmlMediaTypes() {
        return Stream.of("application/xml", "text/xml");
    }

    private static Stream<String> modelMatchingStructuredXmlMediaTypes() {
        return Stream.of(
                "application/todo+xml",
                "application/todos+xml",
                "application/vnd.example.todo+xml",
                "application/vnd.example.todo+xml; charset=utf-8");
    }

    private static Stream<String> unsupportedXmlBasedMediaTypes() {
        return Stream.of(
                "application/vnd.example.project+xml",
                "application/problem+xml",
                "application/soap+xml",
                "application/xhtml+xml",
                "image/svg+xml",
                "application/atom+xml",
                "application/rss+xml",
                "application/*+xml");
    }
}
