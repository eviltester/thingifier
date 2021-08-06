package uk.co.compendiumdev.thingifier.api.http.bodyparser.xml;

import java.util.ArrayList;
import java.util.Collection;

public class StringToXML {

    public static String getStringCollectionAsXml(final String plural,
                                                  final String single,
                                                  final Collection<String> strings) {
        return XMLParserFactory.create("", new ArrayList<>()).
                getStringCollectionAsXML(plural, single, strings);
    }

    // TODO: consider, should this return <%1$s/>
    public static String getEmptyElement(final String type) {
        return String.format("<%1$s></%1$s>", type);
    }
}
