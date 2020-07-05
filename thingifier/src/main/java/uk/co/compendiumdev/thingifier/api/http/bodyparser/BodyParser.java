package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.json.JSONObject;
import org.json.XML;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BodyParser {

    private final HttpApiRequest request;
    private final List<String> thingNames;
    private Map<String, Object> args = null;

    public BodyParser(final HttpApiRequest aGivenRequest, final List<String> thingNames) {
        this.request = aGivenRequest;
        this.thingNames = thingNames;
    }


    /**
     * getStringMap returns the top level values as a map
     */
    public Map<String, String> getStringMap() {
        return stringMap(getMap());
    }


    private Map<String, String> stringMap(final Map<String, Object> args) {
        Map<String, String> stringsInMap = new HashMap();
        for (String key : args.keySet()) {
            Object theValue = args.get(key);

            if (theValue instanceof String ) {
                stringsInMap.put(key, (String) theValue);
            }
            if(theValue instanceof Double){
                stringsInMap.put(key, String.valueOf(theValue));
            }
        }
        return stringsInMap;
    }

    public Map<String, String> getFlattenedStringMap() {
        Map<String, String> stringsInMap = flattenToStringMap("", getMap());
        return stringsInMap;
    }


    private Map<String, String> flattenToStringMap(final String prefixkey, final Object theValue) {
        Map<String, String> stringsInMap = new HashMap();
        if (theValue instanceof String ) {
            stringsInMap.put(prefixkey, (String) theValue);
        }
        if(theValue instanceof Double){
            stringsInMap.put(prefixkey, String.valueOf(theValue));
        }
        String separator = "";
        if(prefixkey!=null && prefixkey.length() > 0 && !prefixkey.endsWith(".")){
            separator = ".";
        }
        if(theValue instanceof Map){
            for (String key : ((Map<String,Object>)theValue).keySet()) {
                Object aValue = ((Map<String,Object>)theValue).get(key);
                Map<String,String> nestedValues = flattenToStringMap(prefixkey + separator + key, aValue);
                stringsInMap.putAll(nestedValues);
            }
        }
        if(theValue instanceof ArrayList) {
            for(Object aValue : (ArrayList)theValue){
                Map<String,String> nestedValues = flattenToStringMap(prefixkey + separator, aValue);
                stringsInMap.putAll(nestedValues);
            }
        }
        return stringsInMap;
    }

    public List<String> getObjectNames(){
        List<String> objectOrCollectionNames = new ArrayList();
        for (String key : args.keySet()) {
            if (!(args.get(key) instanceof String || args.get(key) instanceof Double)) {
                objectOrCollectionNames.add(key);
            }
        }
        return objectOrCollectionNames;
    }

    public Map<String, Object> getMap() {

        parseMap();

        return args;
    }

    /**
     * Only parse it once and then cache the converted map
     */
    private void parseMap() {

        if(args!=null)
            return;

        if(request.getBody().trim().isEmpty()){
            args = new HashMap<>();
            return;
        }

        // because we are using crude XML and JSON parsing
        // <project><title>My posted todo on the project</title></project>
        // would become {"project":{"title":"My posted todo on the project"}}
        // when we want {"title":"My posted todo on the project"}
        // this is just a quick hack to amend it to support XML
        // TODO: try to change this in the future to make it more robust, perhaps the API shouldn't take a String as the body, it should take a parsed class?
        // TODO: BUG - since we remove the wrapper we might send in a POST <project><title>My posted todo on the project</title></project> to /todo and it will work fine if the fields are the same
        if (request.getHeader("Content-Type") != null && request.getHeader("Content-Type").endsWith("/xml")) {

            // PROTOTYPE XML Conversion
            System.out.println(request.getBody());
            System.out.println(XML.toJSONObject(request.getBody()).toString());
            JSONObject conv = XML.toJSONObject(request.getBody());
            if (conv.keySet().size() == 1) {
                // if the key is an entity type then we just want the body
                ArrayList<String> keys = new ArrayList<String>(conv.keySet());

                if (thingNames.contains(keys.get(0))) {
                    // just the body
                    String justTheBody = conv.get(keys.get(0)).toString();
                    System.out.println(justTheBody);
                    args = new Gson().fromJson(justTheBody, Map.class);
                    return;
                }

            }
        }

        args = new Gson().fromJson(request.getBody(), Map.class);
    }


}
