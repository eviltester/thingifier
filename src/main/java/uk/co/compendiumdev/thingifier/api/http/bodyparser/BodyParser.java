package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import com.google.gson.Gson;
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

    public BodyParser(final HttpApiRequest aGivenRequest, final List<String> thingNames) {
        this.request = aGivenRequest;
        this.thingNames = thingNames;
    }



    public Map<String, String> getStringMap() {
        if(request.getBody().trim().isEmpty()){
            return new HashMap();
        }

        return stringMap(getMap());
    }

    private Map<String, String> stringMap(final Map<String, Object> args) {
        Map<String, String> stringsInMap = new HashMap();
        for (String key : args.keySet()) {
            if (args.get(key) instanceof String) {
                stringsInMap.put(key, (String) args.get(key));
            }
        }
        return stringsInMap;
    }

    public Map<String, Object> getMap() {
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
                    Map args = new Gson().fromJson(justTheBody, Map.class);
                    return args;
                }

            }
        }

        return new Gson().fromJson(request.getBody(), Map.class);
    }
}
