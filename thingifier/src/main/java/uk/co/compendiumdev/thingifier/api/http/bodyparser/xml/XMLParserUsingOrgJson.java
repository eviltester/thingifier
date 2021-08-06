package uk.co.compendiumdev.thingifier.api.http.bodyparser.xml;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.json.XML;

import java.util.*;

public class XMLParserUsingOrgJson implements XMLParserAbstraction {
    private final String xml;
    private final List<String> thingNames;

    // TODO: push all XML json usage in here to build an abstraction
    //       and see what the interface is
    //       then create implementations that wrap other XML libraries
    //       to see what works best for our purpose

    public XMLParserUsingOrgJson(String xml, List<String> thingNames){
        this.xml = xml;
        this.thingNames = thingNames;
    }

    public String validateXML(){
        try{
            XML.toJSONObject(this.xml);
        }catch(Exception e){
            return e.getMessage();
        }
        return "";
    }

    public Map<String, Object> xmlAsMap(){
            System.out.println(XML.toJSONObject(this.xml).toString());
            JSONObject conv = XML.toJSONObject(this.xml);
            if (conv.keySet().size() == 1) {
                // if the key is an entity type then we just want the body
                ArrayList<String> keys = new ArrayList<String>(conv.keySet());

                if (thingNames.contains(keys.get(0))) {
                    // just the body
                    String justTheBody = conv.get(keys.get(0)).toString();
                    System.out.println(justTheBody);
                    return new Gson().fromJson(justTheBody, Map.class);
                }
            }

        return new Gson().fromJson(conv.toString(), Map.class);
    }

    @Override
    public String getStringCollectionAsXML(final String plural, final String single, final Collection<String> strings) {
        Map errorResponseBody = new HashMap<String, Collection<String>>();
        errorResponseBody.put(single, strings);
        return XML.toString(new JSONObject(errorResponseBody), plural);
    }
}
