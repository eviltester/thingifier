package uk.co.compendiumdev.thingifier.api.http.bodyparser.xml;

import java.util.Map;

public interface XMLParserAbstraction {

    public String validateXML();
    public Map<String, Object> xmlAsMap();
}
