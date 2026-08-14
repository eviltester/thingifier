package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.*;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.xml.XMLParserAbstraction;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.xml.XMLParserUsingOrgJson;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public class BodyParser {

    private final HttpApiRequest request;
    private final List<String> thingNames;
    private final XMLParserAbstraction xmlParser;
    private Map<String, Object> args = null;

    public BodyParser(final HttpApiRequest aGivenRequest, final List<String> thingNames) {
        this.request = aGivenRequest;
        this.thingNames = thingNames;
        this.xmlParser = new XMLParserUsingOrgJson(this.request.getBody(), this.thingNames);
        // this.xmlParser = new XMLParserUsingXstream(this.request.getBody(), this.thingNames);
    }

    /** getStringMap returns the top level values as a map */
    public Map<String, String> getStringMap() {
        return stringMap(getMap());
    }

    private Map<String, String> stringMap(final Map<String, Object> args) {
        // todo: configuration to reject if wrong types for field definitions
        // default should be to handle and convert
        return ApiBodyFields.fromMap(args).asStringMap();
    }

    // since complex keys can be duplicated,
    // we can't use a hashmap, so we are using a list of map entries
    // the map entries could be a custom Key Value Pair implementation if we wanted
    public List<Map.Entry<String, String>> getFlattenedStringMap() {
        return ApiBodyFields.fromMap(getMap()).asFlattenedStringMap();
    }

    public List<String> getObjectNames() {
        parseMap();
        List<String> objectOrCollectionNames = new ArrayList<>();
        for (String key : args.keySet()) {
            if (!isScalarValue(args.get(key))) {
                objectOrCollectionNames.add(key);
            }
        }
        return objectOrCollectionNames;
    }

    public Map<String, Object> getMap() {

        parseMap();

        return args;
    }

    public String rawBody() {
        return request.getBody();
    }

    public ApiBodyFields bodyFields() {
        return ApiBodyFields.fromMap(getMap());
    }

    /*
       valid if error message returned is empty
    */
    public String validBodyBasedOnContentType() {
        final ContentTypeHeaderParser contentTypeParser =
                new ContentTypeHeaderParser(request.getHeader("content-type"));
        if (contentTypeParser.isXML(thingNames)) {
            String validateResultsErrorReport = this.xmlParser.validateXML();
            if (!validateResultsErrorReport.isEmpty()) {
                return "Invalid XML Payload: " + validateResultsErrorReport;
            }
            return "";
        }

        if (contentTypeParser.isJSON()) {
            try {
                JsonBodyValueConverter.readTree(request.getBody());
                return "";
            } catch (Exception e) {
                // Gson does not give a sensible parse error so use a generic description
                return "Invalid Json Payload: please check the syntax of the request body";
            }
        }

        return "Unknown content Type: API cannot parse %s"
                .formatted(request.getContentTypeHeader());
    }

    /** Only parse it once and then cache the converted map */
    public void parseMap() {

        if (args != null) {
            return;
        }

        String body = request.getBody() == null ? "" : request.getBody();
        if (body.trim().isEmpty()) {
            args = new HashMap<>();
            return;
        }

        // because we are using crude XML and JSON parsing
        // <project><title>My posted to do on the project</title></project>
        // would become {"project":{"title":"My posted to do on the project"}}
        // when we want {"title":"My posted to do on the project"}
        // this is just a quick hack to amend it to support XML
        // TODO: try to change this in the future to make it more robust, perhaps the API shouldn't
        // take a String as the body, it should take a parsed class?
        // TODO: BUG - since we remove the wrapper we might send in a POST <project><title>My posted
        // to do on the project</title></project> to /todo and it will work fine if the fields are
        // the same
        final ContentTypeHeaderParser contentTypeParser =
                new ContentTypeHeaderParser(request.getHeader("content-type"));
        if (contentTypeParser.isXML(thingNames)) {
            System.out.println(request.getBody());
            args = this.xmlParser.xmlAsMap();
        } else {
            // assume it is json
            try {
                args = JsonBodyValueConverter.jsonObjectAsMap(request.getBody());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid JSON Payload", e);
            }
        }

        if (args == null) {
            // something went wrong during conversion, could report as json/xml error
            args = new HashMap<>();
        }
    }

    public ValidationReport validateAgainstType(final EntityDefinition entity) {
        return validateAgainstTypeIgnoring(entity, new ArrayList<>());
    }

    public ValidationReport validateAgainstTypeIgnoring(
            EntityDefinition entity, List<String> doNotValidateFields) {
        ValidationReport report = new ValidationReport();
        for (Map.Entry<String, Object> arg : args.entrySet()) {

            if (entity.hasAnyOfFieldNamesDefined(doNotValidateFields)) {
                continue;
            }

            Field field = entity.getField(arg.getKey());
            if (field == null) {
                continue;
                // should possibly error it? but ignore for now
            }

            Object theValue = arg.getValue();
            String isInstanceType = ApiBodyFields.sourceTypeNameFor(theValue);

            // TODO: add " but was %s" e.g. should be BOOLEAN but was STRING - remember to change in
            // challenges checking
            String errorMessage =
                    String.format(
                            "%s should be %s but was %s",
                            field.getName(), field.getType(), isInstanceType);

            if (field.getType() == FieldType.BOOLEAN) {
                if (!isInstanceType.equals("BOOLEAN")) {
                    report.setValid(false);
                    report.addErrorMessage(errorMessage);
                }
            }
            if (field.getType() == FieldType.INTEGER
                    || field.getType() == FieldType.AUTO_INCREMENT) {
                if (!isInstanceType.equals("INTEGER")) {
                    report.setValid(false);
                    report.addErrorMessage(errorMessage);
                }
            }
            if (field.getType() == FieldType.FLOAT) {
                if (!(isInstanceType.equals("INTEGER") || isInstanceType.equals("NUMERIC"))) {
                    report.setValid(false);
                    report.addErrorMessage(errorMessage);
                }
            }
            if ((field.getType() == FieldType.STRING
                            || field.getType() == FieldType.ENUM
                            || field.getType() == FieldType.DATE
                            || field.getType() == FieldType.AUTO_GUID)
                    && !isInstanceType.equals("STRING")) {
                report.setValid(false);
                report.addErrorMessage(errorMessage);
            }
            if (field.getType() == FieldType.OBJECT && !isInstanceType.equals("OBJECT")) {
                report.setValid(false);
                report.addErrorMessage(errorMessage);
            }
        }
        return report;
    }

    private boolean isScalarValue(final Object value) {
        return value instanceof String || value instanceof Boolean || value instanceof Number;
    }
}
