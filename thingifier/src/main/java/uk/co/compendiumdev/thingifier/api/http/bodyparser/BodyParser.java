package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import com.google.gson.Gson;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.xml.XMLParserAbstraction;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.xml.XMLParserUsingOrgJson;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

import java.util.*;

public class BodyParser {

    private final HttpApiRequest request;
    private final List<String> thingNames;
    private final XMLParserAbstraction xmlParser;
    private Map<String, Object> args = null;

    public BodyParser(final HttpApiRequest aGivenRequest, final List<String> thingNames) {
        this.request = aGivenRequest;
        this.thingNames = thingNames;
        this.xmlParser = new XMLParserUsingOrgJson(this.request.getBody(), this.thingNames);
        //this.xmlParser = new XMLParserUsingXstream(this.request.getBody(), this.thingNames);
    }


    /**
     * getStringMap returns the top level values as a map
     */
    public Map<String, String> getStringMap() {
        return stringMap(getMap());
    }


    private Map<String, String> stringMap(final Map<String, Object> args) {
        // todo: configuration to reject if wrong types for field definitions
        // default should be to handle and convert
        Map<String, String> stringsInMap = new HashMap<>();
        for (String key : args.keySet()) {
            Object theValue = args.get(key);

            if (theValue instanceof Boolean ) {
                stringsInMap.put(key, String.valueOf(theValue));
            }

            if (theValue instanceof String ) {
                stringsInMap.put(key, (String) theValue);
            }

            if(theValue instanceof Double){
                stringsInMap.put(key, String.valueOf(theValue));
            }
        }
        return stringsInMap;
    }

    // since complex keys can be duplicated,
    // we can't use a hashmap, so we are using a list of map entries
    // the map entries could be a custom Key Value Pair implementation if we wanted
    public List<Map.Entry<String,String>> getFlattenedStringMap() {
        return flattenToStringMap("", getMap());
    }


    private List<Map.Entry<String,String>> flattenToStringMap(final String prefixkey, final Object theValue) {
        // todo: configuration to reject if wrong types for field definitions
        // default should be to handle and convert
        List<Map.Entry<String,String>> stringsInMap = new ArrayList<>();
        if (theValue instanceof String ) {
            stringsInMap.add(new AbstractMap.SimpleEntry<>(prefixkey, (String)theValue));
        }
        if(theValue instanceof Double){
            stringsInMap.add(new AbstractMap.SimpleEntry<>(prefixkey, String.valueOf(theValue)));
        }
        if(theValue instanceof Boolean){
            stringsInMap.add(new AbstractMap.SimpleEntry<>(prefixkey, String.valueOf(theValue)));
        }
        if(theValue instanceof Integer){
            stringsInMap.add(new AbstractMap.SimpleEntry<>(prefixkey, String.valueOf(theValue)));
        }
        // todo: what else can come in?
        String separator = "";
        if(prefixkey!=null && !prefixkey.isEmpty() && !prefixkey.endsWith(".")){
            separator = ".";
        }
        if(theValue instanceof Map){
            for (Map.Entry<String,Object> entry : ((Map<String,Object>)theValue).entrySet()) {
                String key = entry.getKey();
                Object aValue = entry.getValue();
                List<Map.Entry<String,String>> nestedValues = flattenToStringMap(prefixkey + separator + key, aValue);
                stringsInMap.addAll(nestedValues);
            }
        }
        if(theValue instanceof ArrayList) {
            for(Object aValue : (ArrayList)theValue){
                List<Map.Entry<String,String>> nestedValues = flattenToStringMap(prefixkey + separator, aValue);
                stringsInMap.addAll(nestedValues);
            }
        }
        return stringsInMap;
    }

    public List<String> getObjectNames(){
        List<String> objectOrCollectionNames = new ArrayList<>();
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

    /*
        valid if error message returned is empty
     */
    public String validBodyBasedOnContentType(){
        final ContentTypeHeaderParser contentTypeParser = new ContentTypeHeaderParser(request.getHeader("content-type"));
        if (contentTypeParser.isXML()) {
            String validateResultsErrorReport = this.xmlParser.validateXML();
            if(!validateResultsErrorReport.isEmpty()){
                return "Invalid XML Payload: " + validateResultsErrorReport;
            }
            return "";
        }

        if(contentTypeParser.isJSON()){
            try{
                new Gson().fromJson(request.getBody(), Map.class);
                return "";
            }catch(Exception e){
                // Gson does not give a sensible parse error so use a generic description
                return "Invalid Json Payload: please check the syntax of the request body";
            }
        }

        return "Unknown content Type: API cannot parse %s".formatted(request.getContentTypeHeader());
    }

    /**
     * Only parse it once and then cache the converted map
     */
    public void parseMap() {

        if(args!=null)
            return;

        if(request.getBody().trim().isEmpty()){
            args = new HashMap<>();
            return;
        }

        // because we are using crude XML and JSON parsing
        // <project><title>My posted to do on the project</title></project>
        // would become {"project":{"title":"My posted to do on the project"}}
        // when we want {"title":"My posted to do on the project"}
        // this is just a quick hack to amend it to support XML
        // TODO: try to change this in the future to make it more robust, perhaps the API shouldn't take a String as the body, it should take a parsed class?
        // TODO: BUG - since we remove the wrapper we might send in a POST <project><title>My posted to do on the project</title></project> to /todo and it will work fine if the fields are the same
        final ContentTypeHeaderParser contentTypeParser = new ContentTypeHeaderParser(request.getHeader("content-type"));
        if (contentTypeParser.isXML()) {
            System.out.println(request.getBody());
            args = this.xmlParser.xmlAsMap();
        }else{
            // assume it is json
            args = new Gson().fromJson(request.getBody(), Map.class);
        }

        if(args==null) {
            // something went wrong during conversion, could report as json/xml error
            args = new HashMap<>();
        }

    }


    public ValidationReport validateAgainstType(final EntityDefinition entity) {
        return validateAgainstTypeIgnoring(entity, new ArrayList<>());
    }


    public ValidationReport validateAgainstTypeIgnoring(EntityDefinition entity, List<String> doNotValidateFields) {
        ValidationReport report = new ValidationReport();
        for(Map.Entry<String, Object>arg : args.entrySet()){

            if(entity.hasAnyOfFieldNamesDefined(doNotValidateFields)){
                continue;
            }

            Field field = entity.getField(arg.getKey());
            if(field==null){
                continue;
                // should possibly error it? but ignore for now
            }

            Object theValue = arg.getValue();
            String isInstanceType = "Something Else";
            if(theValue instanceof String){
                isInstanceType = "STRING";
            }
            if(theValue instanceof Boolean){
                isInstanceType = "BOOLEAN";
            }
            if(theValue instanceof Integer){
                isInstanceType = "INTEGER";
            }
            if(theValue instanceof Float){
                isInstanceType = "NUMERIC";
            }
            if(theValue instanceof Double){
                isInstanceType = "NUMERIC";
            }



            // TODO: add " but was %s" e.g. should be BOOLEAN but was STRING - remember to change in challenges checking
            String errorMessage = String.format("%s should be %s but was %s", field.getName(), field.getType(), isInstanceType);

            if(field.getType()== FieldType.BOOLEAN){
                if (!(theValue instanceof Boolean )) {
                    report.setValid(false);
                    report.addErrorMessage(errorMessage);
                }
            }
            if(field.getType()== FieldType.INTEGER || field.getType()==FieldType.AUTO_INCREMENT){
                if (!(theValue instanceof Double )) {
                    report.setValid(false);
                    report.addErrorMessage(errorMessage);
                }else {
                    // enforce an int
                    arg.setValue(((Double) theValue).intValue());
                }
            }
            if(field.getType()== FieldType.FLOAT){
                if (!(theValue instanceof Double )) {
                    report.setValid(false);
                    report.addErrorMessage(errorMessage);
                }
            }
            // everything else goes
        }
        return report;
    }
}
