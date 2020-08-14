package uk.co.compendiumdev.thingifier.reporting;

import org.json.JSONObject;
import org.json.XML;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class XmlThing {

    private final JsonThing jsonConvertor;

    public XmlThing(final JsonThing jsonThing) {
        this.jsonConvertor = jsonThing;
    }

    public static String getStringCollectionAsXml(final String plural,
                                                  final String single,
                                                  final Collection<String> strings) {

        Map errorResponseBody = new HashMap<String, Collection<String>>();
        errorResponseBody.put(single, strings);
        return XML.toString(new JSONObject(errorResponseBody), plural);
    }

    public String getSingleObjectXml(final ThingInstance instance) {
        String parseForXMLOutput = jsonConvertor.asNamedJsonObject(instance).toString();
        //System.out.println(parseForXMLOutput);
        return XML.toString(new JSONObject(parseForXMLOutput));
    }

    public String getEmptyElement(final String type) {
        return String.format("<%1$s></%1$s>", type);
    }

    public String getCollectionOfThings(final List<ThingInstance> thingsToReturn, final ThingDefinition typeOfThingReturned) {
        String parseForXMLOutput = jsonConvertor.asJsonTypedArrayWithContentsTyped(
                                                thingsToReturn, typeOfThingReturned);

        String output = XML.toString(new JSONObject(parseForXMLOutput));

        // TODO: workaround for this seems like a bug in XML.toString, but work around it at the moment
        // i.e. it outputs <todos><todo>...</todo></todos><todos><todo>...</todo></todos>
        output = output.replace(String.format("</%1$s><%1$s>", thingsToReturn.get(0).getEntity().getPlural()), "");
        return output;
    }

    /*
        a very basic and crude 'pretty printer
        for html or xml - assumes valid xml
     */
    public String prettyPrint(final String someXml) {
        int indentLevel = 0;
        StringBuilder sb = new StringBuilder();
        String process = someXml.trim();
        boolean endTag = false;
        for (int i = 0, length = process.length(); i < length; i++) {
            char c = process.charAt(i);
            switch (c) {
                case '<':
                    if(process.charAt(i+1)=='/'){
                        indentLevel--;
                        if(endTag){
                            // this is wrapping end tag
                            sb.append(String.format("%n"));
                            indent(sb, indentLevel);

                        }
                        endTag=true;
                    }else{
                        endTag=false;
                        if(i!=0){
                            indentLevel++;
                            sb.append(String.format("%n"));
                        }
                        indent(sb, indentLevel);
                    }
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '/':
                    if(process.charAt(i+1)=='>'){
                        // handle self closing empty tags
                        endTag=true;
                        indentLevel--;
                    }
                    sb.append("/");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public String asHtml(final String html) {
        return html.replace("<", "&lt;").replace(">", "&gt;");
    }

    private void indent(final StringBuilder sb, final int indentLevel) {
        String indentAs = "  ";
        for(int spaceCount=0;spaceCount<indentLevel;spaceCount++){
            sb.append(indentAs);
        }
    }

    public String prettyPrintHtml(final String singleObjectXml) {
        return asHtml(prettyPrint(singleObjectXml));
    }
}
