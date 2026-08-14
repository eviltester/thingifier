package uk.co.compendiumdev.thingifier.api;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;

public class AcceptHeaderParserTest {

    @Test
    public void xmlPriorityIdentified() {

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/xml, application/json");

        Assertions.assertTrue(accept.hasAPreferenceForXml());
        Assertions.assertTrue(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.hasAPreferenceForJson());
        Assertions.assertFalse(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(
                accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.NO_MATCHING_TYPE));
    }

    @Test
    public void jsonPriorityIdentified() {

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/json, application/xml");

        Assertions.assertTrue(accept.hasAPreferenceForJson());
        Assertions.assertTrue(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
        Assertions.assertFalse(accept.hasAPreferenceForXml());
        Assertions.assertFalse(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(
                accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.NO_MATCHING_TYPE));
    }

    @Test
    public void anythingIsNeverPriorityIdentified() {

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/json, application/xml");

        Assertions.assertFalse(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
    }

    @Test
    public void willAcceptEverything() {

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/json, application/xml");

        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertTrue(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAcceptXml());
    }

    @Test
    public void willAcceptJson() {

        final AcceptHeaderParser accept = new AcceptHeaderParser("application/json");

        Assertions.assertTrue(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.JSON));
        Assertions.assertFalse(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.willAcceptXml());
    }

    @Test
    public void willAcceptXml() {

        final AcceptHeaderParser accept = new AcceptHeaderParser("application/xml");

        Assertions.assertTrue(accept.willAcceptXml());
        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.willAcceptJson());
    }

    @Test
    public void willAcceptAnything() {

        final AcceptHeaderParser accept = new AcceptHeaderParser("*/*");

        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertTrue(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAcceptXml());
    }

    @Test
    public void willAcceptAnythingAtAll() {

        final AcceptHeaderParser accept = new AcceptHeaderParser("");

        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertTrue(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAcceptXml());
    }

    /*
    Check has asked for
     */

    @Test
    public void hasAskedForEverything() {

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/json, application/xml");

        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    @Test
    public void hasAskedForJson() {

        final AcceptHeaderParser accept = new AcceptHeaderParser("application/json");

        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
    }

    @Test
    public void hasAskedForXml() {

        final AcceptHeaderParser accept = new AcceptHeaderParser("application/xml");

        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    @Test
    public void hasAskedForAnything() {

        final AcceptHeaderParser accept = new AcceptHeaderParser("*/*");

        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    @Test
    public void hasNotAskedForAnythingWillAcceptDefault() {

        final AcceptHeaderParser accept = new AcceptHeaderParser("");

        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    @Test
    public void willAcceptAdditionalResponseRepresentations() {
        Assertions.assertTrue(
                new AcceptHeaderParser("text/csv").willAccept(AcceptHeaderParser.ACCEPT_TYPE.CSV));
        Assertions.assertTrue(new AcceptHeaderParser("text/plain").willAcceptText());
        Assertions.assertTrue(
                new AcceptHeaderParser("text/html")
                        .willAccept(AcceptHeaderParser.ACCEPT_TYPE.HTML));
        Assertions.assertTrue(
                new AcceptHeaderParser("application/x-ndjson")
                        .willAccept(AcceptHeaderParser.ACCEPT_TYPE.NDJSON));
        Assertions.assertTrue(
                new AcceptHeaderParser("application/jsonl")
                        .willAccept(AcceptHeaderParser.ACCEPT_TYPE.JSONL));
        Assertions.assertTrue(
                new AcceptHeaderParser("application/json-seq")
                        .willAccept(AcceptHeaderParser.ACCEPT_TYPE.JSON_SEQ));
        Assertions.assertTrue(
                new AcceptHeaderParser("text/tab-separated-values")
                        .willAccept(AcceptHeaderParser.ACCEPT_TYPE.TSV));
        Assertions.assertTrue(
                new AcceptHeaderParser("text/xml")
                        .willAccept(AcceptHeaderParser.ACCEPT_TYPE.TEXT_XML));
    }

    @Test
    public void matchesMediaTypesBeforeHeaderParameters() {
        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/json-seq; charset=utf-8");

        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON_SEQ));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    @Test
    public void identifiesFirstSupportedRepresentationInHeaderOrder() {
        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/unknown, text/html, text/csv");

        Assertions.assertEquals(
                List.of(AcceptHeaderParser.ACCEPT_TYPE.HTML, AcceptHeaderParser.ACCEPT_TYPE.CSV),
                accept.getSupportedTypesInPreferenceOrder());
        Assertions.assertTrue(accept.hasAPreferenceForHtml());
        Assertions.assertFalse(accept.hasAPreferenceForCsv());
    }

    @Test
    public void textWildcardMatchesTextResponseRepresentations() {
        final AcceptHeaderParser accept = new AcceptHeaderParser("text/*");

        Assertions.assertTrue(accept.isSupportedHeader());
        Assertions.assertEquals(
                List.of(
                        AcceptHeaderParser.ACCEPT_TYPE.CSV,
                        AcceptHeaderParser.ACCEPT_TYPE.TEXT,
                        AcceptHeaderParser.ACCEPT_TYPE.HTML,
                        AcceptHeaderParser.ACCEPT_TYPE.TSV,
                        AcceptHeaderParser.ACCEPT_TYPE.TEXT_XML),
                accept.getSupportedTypesInPreferenceOrder());
        Assertions.assertTrue(accept.willAcceptText());
        Assertions.assertTrue(accept.willAcceptXml());
        Assertions.assertFalse(accept.willAcceptJson());
    }

    @Test
    public void applicationWildcardMatchesApplicationResponseRepresentations() {
        final AcceptHeaderParser accept = new AcceptHeaderParser("application/*");

        Assertions.assertTrue(accept.isSupportedHeader());
        Assertions.assertEquals(
                List.of(
                        AcceptHeaderParser.ACCEPT_TYPE.JSON,
                        AcceptHeaderParser.ACCEPT_TYPE.XML,
                        AcceptHeaderParser.ACCEPT_TYPE.NDJSON,
                        AcceptHeaderParser.ACCEPT_TYPE.JSONL,
                        AcceptHeaderParser.ACCEPT_TYPE.JSON_SEQ),
                accept.getSupportedTypesInPreferenceOrder());
        Assertions.assertTrue(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAcceptXml());
        Assertions.assertFalse(accept.willAcceptCsv());
    }

    @Test
    public void textXmlNegotiatesToXmlRepresentation() {
        final AcceptHeaderParser textXml = new AcceptHeaderParser("text/xml");

        Assertions.assertTrue(textXml.isSupportedHeader());
        Assertions.assertTrue(textXml.willAcceptXml());
        Assertions.assertTrue(textXml.hasAPreferenceForXml());
        Assertions.assertEquals(
                List.of(AcceptHeaderParser.ACCEPT_TYPE.TEXT_XML),
                textXml.getSupportedTypesInPreferenceOrder());
    }

    @Test
    public void modelMatchingStructuredXmlMediaTypeNegotiatesToXmlRepresentation() {
        final AcceptHeaderParser vendorXml =
                new AcceptHeaderParser("application/vnd.example.todo+xml");

        Assertions.assertFalse(vendorXml.isSupportedHeader());
        Assertions.assertTrue(vendorXml.isSupportedHeader(List.of("todo", "todos")));
        Assertions.assertEquals(
                "application/vnd.example.todo+xml",
                vendorXml
                        .preferredSupportedMediaType(
                                AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes(),
                                AcceptHeaderParser.ACCEPT_TYPE.JSON,
                                List.of("todo", "todos"))
                        .mediaType());
    }

    @Test
    public void structuredXmlWildcardNegotiatesToModelDerivedXmlRepresentation() {
        final AcceptHeaderParser suffixWildcard = new AcceptHeaderParser("application/*+xml");

        Assertions.assertFalse(suffixWildcard.isSupportedHeader());
        Assertions.assertTrue(suffixWildcard.isSupportedHeader(List.of("todo", "todos")));
        Assertions.assertEquals(
                "application/todo+xml",
                suffixWildcard
                        .preferredSupportedMediaType(
                                AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes(),
                                AcceptHeaderParser.ACCEPT_TYPE.JSON,
                                List.of("todo", "todos"))
                        .mediaType());
    }

    @Test
    public void unsupportedXmlBasedMediaTypesAreNotNormalXmlRepresentations() {
        for (String mediaType :
                List.of(
                        "application/problem+xml",
                        "application/soap+xml",
                        "application/xhtml+xml",
                        "image/svg+xml",
                        "application/atom+xml",
                        "application/rss+xml")) {
            final AcceptHeaderParser accept = new AcceptHeaderParser(mediaType);

            Assertions.assertFalse(accept.isSupportedHeader(), mediaType);
            Assertions.assertFalse(accept.willAcceptXml(), mediaType);
            Assertions.assertTrue(accept.getSupportedTypesInPreferenceOrder().isEmpty(), mediaType);
        }
    }

    @Test
    public void qValuesControlPreferenceOrderForXmlCompatibleTypes() {
        final AcceptHeaderParser accept =
                new AcceptHeaderParser(
                        "application/json;q=0.5, "
                                + "application/xml;q=0.6, "
                                + "text/xml;q=0.9, "
                                + "application/vnd.example.todo+xml;q=0.8");

        Assertions.assertEquals(
                "text/xml",
                accept.preferredSupportedMediaType(
                                AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes(),
                                AcceptHeaderParser.ACCEPT_TYPE.JSON,
                                List.of("todo", "todos"))
                        .mediaType());
        Assertions.assertEquals(
                List.of(
                        AcceptHeaderParser.ACCEPT_TYPE.TEXT_XML,
                        AcceptHeaderParser.ACCEPT_TYPE.XML,
                        AcceptHeaderParser.ACCEPT_TYPE.JSON),
                accept.getSupportedTypesInPreferenceOrder());
        Assertions.assertTrue(accept.hasAPreferenceForXml());
        Assertions.assertFalse(accept.hasAPreferenceForJson());
    }

    @ParameterizedTest
    @MethodSource("issue103QualityValueExamples")
    public void issue103ExamplesNegotiateSupportedTypesByQualityValues(
            final String acceptHeader, final AcceptHeaderParser.ACCEPT_TYPE expectedType) {
        assertPreferredType(acceptHeader, expectedType);
    }

    @Test
    public void issue103ExampleWithOnlyQZeroSupportedTypesHasNoAcceptableRepresentation() {
        final AcceptHeaderParser allRejected =
                new AcceptHeaderParser("application/json;q=0, application/xml;q=0");

        Assertions.assertTrue(allRejected.isSupportedHeader());
        Assertions.assertTrue(allRejected.getSupportedTypesInPreferenceOrder().isEmpty());
        Assertions.assertEquals(
                AcceptHeaderParser.ACCEPT_TYPE.NO_MATCHING_TYPE,
                allRejected.preferredSupportedType(
                        AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes(),
                        AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    @Test
    public void qValuesSelectBetweenXmlCompatibleMediaTypes() {
        assertPreferredMediaType(
                "application/json;q=0.5, "
                        + "application/xml;q=0.6, "
                        + "text/xml;q=0.8, "
                        + "application/vnd.example.todo+xml;q=0.9",
                "application/vnd.example.todo+xml");
        assertPreferredMediaType(
                "application/json;q=0.5, "
                        + "application/xml;q=0.6, "
                        + "text/xml;q=0.8, "
                        + "application/*+xml;q=0.9",
                "application/todo+xml");
    }

    @Test
    public void unsupportedHigherQualityMediaRangesFallThroughToSupportedTypes() {
        assertPreferredMediaType("application/problem+xml;q=1, text/xml;q=0.4", "text/xml");
        assertPreferredMediaType(
                "application/soap+xml;q=1, application/*+xml;q=0.4", "application/todo+xml");
        assertPreferredMediaType(
                "application/problem+json;q=1, application/vnd.example.todo+xml;q=0.4",
                "application/vnd.example.todo+xml");
    }

    @Test
    public void specificityBeatsHeaderOrderWhenQualityValuesTie() {
        assertPreferredMediaType("application/*;q=0.8, application/xml;q=0.8", "application/xml");
        assertPreferredMediaType("text/*;q=0.8, text/xml;q=0.8", "text/xml");
        assertPreferredMediaType(
                "application/*+xml;q=0.8, application/vnd.example.todo+xml;q=0.8",
                "application/vnd.example.todo+xml");
    }

    @Test
    public void clientOrderBreaksTiesWhenQualityAndSpecificityAreTheSame() {
        assertPreferredMediaType("text/xml;q=0.8, application/xml;q=0.8", "text/xml");
        assertPreferredMediaType("application/xml;q=0.8, text/xml;q=0.8", "application/xml");
        assertPreferredMediaType(
                "application/vnd.example.todo+xml;q=0.8, text/xml;q=0.8",
                "application/vnd.example.todo+xml");
        assertPreferredMediaType(
                "text/xml;q=0.8, application/vnd.example.todo+xml;q=0.8", "text/xml");
    }

    @Test
    public void qZeroExcludesExactXmlCompatibleMediaTypesBeforeWildcardFallback() {
        assertPreferredMediaType("text/xml;q=0, text/*;q=0.8", "text/csv");
        assertPreferredMediaType("application/xml;q=0, application/*;q=0.8", "application/json");

        final AcceptHeaderParser structuredXml =
                new AcceptHeaderParser("application/todo+xml;q=0, application/*+xml;q=0.8");

        Assertions.assertEquals(
                AcceptHeaderParser.ACCEPT_TYPE.NO_MATCHING_TYPE,
                structuredXml
                        .preferredSupportedMediaType(
                                AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes(),
                                AcceptHeaderParser.ACCEPT_TYPE.JSON,
                                List.of("todo", "todos"))
                        .type());
    }

    @Test
    public void qValuesControlPreferenceOrder() {
        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/xml;q=0.2, application/json;q=0.9");

        Assertions.assertEquals(
                List.of(AcceptHeaderParser.ACCEPT_TYPE.JSON, AcceptHeaderParser.ACCEPT_TYPE.XML),
                accept.getSupportedTypesInPreferenceOrder());
        Assertions.assertTrue(accept.hasAPreferenceForJson());
        Assertions.assertFalse(accept.hasAPreferenceForXml());
    }

    @Test
    public void qZeroExcludesMediaTypeFromNegotiation() {
        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/json;q=0, application/xml");

        Assertions.assertTrue(accept.isSupportedHeader());
        Assertions.assertFalse(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAcceptXml());
        Assertions.assertEquals(
                List.of(AcceptHeaderParser.ACCEPT_TYPE.XML),
                accept.getSupportedTypesInPreferenceOrder());
    }

    @Test
    public void qZeroExactMatchOverridesWildcardForThatMediaType() {
        final AcceptHeaderParser accept = new AcceptHeaderParser("application/json;q=0, */*");

        Assertions.assertFalse(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAcceptXml());
        Assertions.assertEquals(
                AcceptHeaderParser.ACCEPT_TYPE.XML,
                accept.getSupportedTypesInPreferenceOrder().get(0));
    }

    @Test
    public void unsupportedMediaTypesAreIgnoredWhenSupportedAlternativeExists() {
        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/problem+json, application/json;q=0.5");

        Assertions.assertEquals(
                List.of(AcceptHeaderParser.ACCEPT_TYPE.JSON),
                accept.getSupportedTypesInPreferenceOrder());
        Assertions.assertTrue(accept.willAcceptJson());
    }

    @Test
    public void concreteStructuredJsonTypesDoNotMatchPlainJson() {
        Assertions.assertFalse(new AcceptHeaderParser("application/problem+json").willAcceptJson());
        Assertions.assertFalse(new AcceptHeaderParser("application/vnd.api+json").willAcceptJson());
        Assertions.assertFalse(new AcceptHeaderParser("application/hal+json").willAcceptJson());
    }

    @Test
    public void structuredJsonWildcardDoesNotMatchPlainJson() {
        final AcceptHeaderParser accept = new AcceptHeaderParser("application/*+json");

        Assertions.assertFalse(accept.isSupportedHeader());
        Assertions.assertFalse(accept.willAcceptJson());
        Assertions.assertTrue(accept.getSupportedTypesInPreferenceOrder().isEmpty());
    }

    @Test
    public void parametersDoNotPreventQValueProcessing() {
        final AcceptHeaderParser accept =
                new AcceptHeaderParser(
                        "application/json; charset=utf-8; q=0.3, "
                                + "application/xml; version=1; q=0.8");

        Assertions.assertEquals(
                List.of(AcceptHeaderParser.ACCEPT_TYPE.XML, AcceptHeaderParser.ACCEPT_TYPE.JSON),
                accept.getSupportedTypesInPreferenceOrder());
    }

    @Test
    public void invalidQValuesMakeMediaRangesUnacceptable() {
        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/json;q=nope, application/xml;q=1.1");

        Assertions.assertTrue(accept.isSupportedHeader());
        Assertions.assertFalse(accept.willAcceptJson());
        Assertions.assertFalse(accept.willAcceptXml());
        Assertions.assertTrue(accept.getSupportedTypesInPreferenceOrder().isEmpty());
    }

    private void assertPreferredType(
            final String acceptHeader, final AcceptHeaderParser.ACCEPT_TYPE expectedType) {
        final AcceptHeaderParser accept = new AcceptHeaderParser(acceptHeader);

        Assertions.assertTrue(accept.isSupportedHeader(), acceptHeader);
        Assertions.assertEquals(expectedType, accept.getSupportedTypesInPreferenceOrder().get(0));
        Assertions.assertEquals(
                expectedType,
                accept.preferredSupportedType(
                        AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes(),
                        AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    private void assertPreferredMediaType(final String acceptHeader, final String mediaType) {
        final AcceptHeaderParser accept = new AcceptHeaderParser(acceptHeader);

        Assertions.assertTrue(accept.isSupportedHeader(List.of("todo", "todos")), acceptHeader);
        Assertions.assertEquals(
                mediaType,
                accept.preferredSupportedMediaType(
                                AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes(),
                                AcceptHeaderParser.ACCEPT_TYPE.JSON,
                                List.of("todo", "todos"))
                        .mediaType());
    }

    private static Stream<Arguments> issue103QualityValueExamples() {
        return Stream.of(
                Arguments.of(
                        "application/xml;q=1, application/json;q=0.5",
                        AcceptHeaderParser.ACCEPT_TYPE.XML),
                Arguments.of(
                        "application/json;q=1, application/xml;q=0.5",
                        AcceptHeaderParser.ACCEPT_TYPE.JSON),
                Arguments.of(
                        "application/xml;q=0.2, application/json;q=0.9",
                        AcceptHeaderParser.ACCEPT_TYPE.JSON),
                Arguments.of(
                        "application/json;q=0, application/xml;q=1",
                        AcceptHeaderParser.ACCEPT_TYPE.XML),
                Arguments.of(
                        "application/xml, application/json;q=0.5",
                        AcceptHeaderParser.ACCEPT_TYPE.XML),
                Arguments.of(
                        "*/*;q=0.8, application/xml;q=0.9", AcceptHeaderParser.ACCEPT_TYPE.XML),
                Arguments.of(
                        "application/gzip;q=1, application/json;q=0.5",
                        AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }
}
