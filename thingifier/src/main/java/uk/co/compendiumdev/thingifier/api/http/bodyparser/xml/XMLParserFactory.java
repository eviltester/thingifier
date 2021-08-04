package uk.co.compendiumdev.thingifier.api.http.bodyparser.xml;

import java.util.List;

/* utility to make it easy to switch between parsers */
public class XMLParserFactory {
    public static XMLParserAbstraction create(String xml, List<String> thingNames){
        return new XMLParserUsingOrgJson(xml, thingNames);
    }
}
