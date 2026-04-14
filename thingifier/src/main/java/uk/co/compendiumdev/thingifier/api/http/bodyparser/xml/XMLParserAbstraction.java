package uk.co.compendiumdev.thingifier.api.http.bodyparser.xml;

import java.util.Collection;
import java.util.Map;

public interface XMLParserAbstraction {

    String validateXML();
    Map<String, Object> xmlAsMap();

    String getStringCollectionAsXML(String plural, String single, Collection<String> strings);
}
