package uk.co.compendiumdev.thingifier.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.http.AcceptHeaderParser;

public class AcceptHeaderParserTest {

    @Test
    public void xmlPriorityIdentified(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/xml, application/json");

        Assertions.assertTrue(accept.hasAPreferenceForXml());
        Assertions.assertTrue(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.hasAPreferenceForJson());
        Assertions.assertFalse(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.NO_MATCHING_TYPE));
    }

    @Test
    public void jsonPriorityIdentified(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/json, application/xml");

        Assertions.assertTrue(accept.hasAPreferenceForJson());
        Assertions.assertTrue(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
        Assertions.assertFalse(accept.hasAPreferenceForXml());
        Assertions.assertFalse(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.NO_MATCHING_TYPE));
    }

    @Test
    public void anythingIsNeverPriorityIdentified(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/json, application/xml");

        Assertions.assertFalse(accept.hasAPreferenceFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
    }

    @Test
    public void willAcceptEverything(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/json, application/xml");

        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertTrue(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAcceptXml());
    }

    @Test
    public void willAcceptJson(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/json");

        Assertions.assertTrue(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.JSON));
        Assertions.assertFalse(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.willAcceptXml());
    }

    @Test
    public void willAcceptXml(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/xml");

        Assertions.assertTrue(accept.willAcceptXml());
        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.willAcceptJson());
    }

    @Test
    public void willAcceptAnything(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*");

        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertTrue(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAcceptXml());
    }

    @Test
    public void willAcceptAnythingAtAll(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("");

        Assertions.assertTrue(accept.willAccept(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertTrue(accept.willAcceptJson());
        Assertions.assertTrue(accept.willAcceptXml());
    }


    /*
    Check has asked for
     */

    @Test
    public void hasAskedForEverything(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*, application/json, application/xml");

        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    @Test
    public void hasAskedForJson(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/json");

        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
    }

    @Test
    public void hasAskedForXml(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("application/xml");

        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    @Test
    public void hasAskedForAnything(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("*/*");

        Assertions.assertTrue(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }

    @Test
    public void hasNotAskedForAnythingWillAcceptDefault(){

        final AcceptHeaderParser accept =
                new AcceptHeaderParser("");

        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.ANYTHING));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML));
        Assertions.assertFalse(accept.hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.JSON));
    }
}
