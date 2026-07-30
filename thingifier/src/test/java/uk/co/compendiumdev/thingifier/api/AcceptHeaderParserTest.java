package uk.co.compendiumdev.thingifier.api;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
    public void textWildcardIsNotASupportedResponseRepresentation() {
        final AcceptHeaderParser accept = new AcceptHeaderParser("text/*");

        Assertions.assertFalse(accept.isSupportedHeader());
    }
}
