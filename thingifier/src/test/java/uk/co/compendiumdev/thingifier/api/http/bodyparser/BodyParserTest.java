package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;

public class BodyParserTest {

    @Test
    public void simpleJsonParse() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setBody("{'duration':'5'}");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final Map<String, String> map = new BodyParser(request, names).getStringMap();

        Assertions.assertEquals(1, map.keySet().size());
        Assertions.assertTrue(map.keySet().contains("duration"));
        Assertions.assertEquals("5", map.get("duration"));
    }

    @Test
    public void jsonBodyFieldsPreserveTopLevelSourceTypes() {
        HttpApiRequest request = new HttpApiRequest("/items");
        request.setBody(
                "{"
                        + "\"text\":\"hello\","
                        + "\"flag\":true,"
                        + "\"whole\":2,"
                        + "\"decimal\":2.0,"
                        + "\"object\":{\"child\":\"value\"},"
                        + "\"array\":[1,\"two\"],"
                        + "\"nothing\":null"
                        + "}");

        ApiBodyFields fields = new BodyParser(request, List.of("item", "items")).bodyFields();

        Assertions.assertEquals("STRING", sourceType(fields, "text"));
        Assertions.assertEquals("BOOLEAN", sourceType(fields, "flag"));
        Assertions.assertEquals("INTEGER", sourceType(fields, "whole"));
        Assertions.assertEquals("NUMERIC", sourceType(fields, "decimal"));
        Assertions.assertEquals("OBJECT", sourceType(fields, "object"));
        Assertions.assertEquals("ARRAY", sourceType(fields, "array"));
        Assertions.assertEquals("NULL", sourceType(fields, "nothing"));
        Assertions.assertEquals("2", value(fields, "whole"));
        Assertions.assertEquals("2.0", value(fields, "decimal"));
    }

    @Test
    public void nestedJsonValuesStillFlattenToStrings() {
        HttpApiRequest request = new HttpApiRequest("/items");
        request.setBody(
                "{"
                        + "\"relationships\":{\"project\":{\"id\":2,\"weight\":2.5,\"guid\":\"p1\"}},"
                        + "\"metadata\":{\"source\":\"api\"}"
                        + "}");

        ApiBodyFields fields = new BodyParser(request, List.of("item", "items")).bodyFields();
        List<Map.Entry<String, String>> flattened = fields.asFlattenedStringMap();

        Assertions.assertEquals("OBJECT", sourceType(fields, "relationships"));
        Assertions.assertTrue(
                flattened.stream()
                        .anyMatch(
                                entry ->
                                        "relationships.project.id".equals(entry.getKey())
                                                && "2".equals(entry.getValue())));
        Assertions.assertTrue(
                flattened.stream()
                        .anyMatch(
                                entry ->
                                        "relationships.project.weight".equals(entry.getKey())
                                                && "2.5".equals(entry.getValue())));
        Assertions.assertTrue(
                flattened.stream()
                        .anyMatch(
                                entry ->
                                        "relationships.project.guid".equals(entry.getKey())
                                                && "p1".equals(entry.getValue())));
        Assertions.assertTrue(
                flattened.stream()
                        .anyMatch(
                                entry ->
                                        "metadata.source".equals(entry.getKey())
                                                && "api".equals(entry.getValue())));
    }

    @Test
    public void simpleJsonParseErrorMessage() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setHeaders(Map.of("content-type", "application/json"));
        request.setBody("{'duration':'5'");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final String validated = new BodyParser(request, names).validBodyBasedOnContentType();

        Assertions.assertEquals(
                "Invalid Json Payload: please check the syntax of the request body", validated);
    }

    @Test
    public void simpleXmlParseErrorMessage() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setHeaders(Map.of("content-type", "application/xml"));
        request.setBody("<estimate><duration>5</duration>");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final String validated = new BodyParser(request, names).validBodyBasedOnContentType();

        Assertions.assertEquals(
                "Invalid XML Payload: Unclosed tag estimate at 32 [character 33 line 1]",
                validated);
    }

    @Test
    public void simpleTextXmlParseErrorMessage() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setHeaders(Map.of("content-type", "text/xml"));
        request.setBody("<estimate><duration>5</duration>");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final String validated = new BodyParser(request, names).validBodyBasedOnContentType();

        Assertions.assertEquals(
                "Invalid XML Payload: Unclosed tag estimate at 32 [character 33 line 1]",
                validated);
    }

    @Test
    public void simpleVendorXmlParseErrorMessage() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setHeaders(
                Map.of("content-type", "application/vnd.example.estimate+xml; charset=utf-8"));
        request.setBody("<estimate><duration>5</duration>");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final String validated = new BodyParser(request, names).validBodyBasedOnContentType();

        Assertions.assertEquals(
                "Invalid XML Payload: Unclosed tag estimate at 32 [character 33 line 1]",
                validated);
    }

    @Test
    public void unsupportedXmlBasedContentTypeIsNotParsedAsXml() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setHeaders(Map.of("content-type", "application/problem+xml"));
        request.setBody("<estimate><duration>5</duration></estimate>");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final String validated = new BodyParser(request, names).validBodyBasedOnContentType();

        Assertions.assertEquals(
                "Unknown content Type: API cannot parse application/problem+xml", validated);
    }

    @Test
    public void simpleUnknownContentParseErrorMessage() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setHeaders(Map.of("content-type", "application/csv"));
        request.setBody("duration,5");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final String validated = new BodyParser(request, names).validBodyBasedOnContentType();

        Assertions.assertEquals(
                "Unknown content Type: API cannot parse application/csv", validated);
    }

    @Test
    public void embeddedObjectParseIgnoredOnStringMap() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setBody("{'duration':'5', 'estimate' : {'guid' : '1234567890'}}");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final Map<String, String> map = new BodyParser(request, names).getStringMap();

        Assertions.assertEquals(1, map.keySet().size());
        Assertions.assertTrue(map.keySet().contains("duration"));
        Assertions.assertEquals("5", map.get("duration"));
    }

    @Test
    public void embeddedObjectParseFoundOnMapParse() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setBody("{'duration':'5', 'estimate' : {'guid' : '1234567890'}}");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final BodyParser bodyParser = new BodyParser(request, names);

        final Map<String, Object> map = bodyParser.getMap();

        Assertions.assertEquals(2, map.keySet().size());
        Assertions.assertTrue(map.keySet().contains("duration"));
        Assertions.assertEquals("5", map.get("duration"));
        Assertions.assertTrue(map.keySet().contains("estimate"));

        final List<String> objects = bodyParser.getObjectNames();
        Assertions.assertEquals(1, objects.size());
        Assertions.assertEquals("estimate", objects.get(0));

        // estimate object is a LinkedTreeMap

    }

    @Test
    public void embeddedCollectionOfObject() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setBody("{'duration':'5', 'estimate' : [{'guid' : '1234567890'}]}");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final BodyParser bodyParser = new BodyParser(request, names);

        final Map<String, String> valuesmap = bodyParser.getStringMap();

        Assertions.assertEquals(1, valuesmap.keySet().size());
        Assertions.assertTrue(valuesmap.keySet().contains("duration"));
        Assertions.assertEquals("5", valuesmap.get("duration"));

        final Map<String, Object> map = bodyParser.getMap();

        Assertions.assertEquals(2, map.keySet().size());
        Assertions.assertTrue(map.keySet().contains("duration"));
        Assertions.assertEquals("5", map.get("duration"));
        Assertions.assertTrue(map.keySet().contains("estimate"));

        final List<String> objects = bodyParser.getObjectNames();
        Assertions.assertEquals(1, objects.size());
        Assertions.assertEquals("estimate", objects.get(0));

        // estimate is a LinkedTreeMap
    }

    @Test
    public void embeddedCollectionOfObjects() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setBody(
                "{'duration':'5', 'estimate' : [{'guid' : '1234567890'}, {'guid' : '12345678901234567890'}]}");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final BodyParser bodyParser = new BodyParser(request, names);

        final Map<String, String> valuesmap = bodyParser.getStringMap();

        Assertions.assertEquals(1, valuesmap.keySet().size());
        Assertions.assertTrue(valuesmap.keySet().contains("duration"));
        Assertions.assertEquals("5", valuesmap.get("duration"));

        final Map<String, Object> map = bodyParser.getMap();

        Assertions.assertEquals(2, map.keySet().size());
        Assertions.assertTrue(map.keySet().contains("duration"));
        Assertions.assertEquals("5", map.get("duration"));
        Assertions.assertTrue(map.keySet().contains("estimate"));

        final List<String> objects = bodyParser.getObjectNames();
        Assertions.assertEquals(1, objects.size());
        Assertions.assertEquals("estimate", objects.get(0));

        // estimate is an ArrayList of LinkedTreeMap
    }

    @Test
    public void embeddedCollectionOfObjectFromXML() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.addHeader("Content-Type", "application/xml");
        // <estimate><duration>5</duration><estimates><estimate><guid>1234567890</guid></estimate></estimates></estimate>
        request.setBody(
                "<estimate><duration>5</duration><estimate><todo><guid>1234567890</guid></todo></estimate></estimate>");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final BodyParser bodyParser = new BodyParser(request, names);

        final Map<String, String> valuesmap = bodyParser.getStringMap();

        Assertions.assertEquals(1, valuesmap.keySet().size());
        Assertions.assertTrue(valuesmap.keySet().contains("duration"));
        Assertions.assertEquals("5.0", valuesmap.get("duration"));

        final Map<String, Object> map = bodyParser.getMap();

        Assertions.assertEquals(2, map.keySet().size());
        Assertions.assertTrue(map.keySet().contains("duration"));
        Assertions.assertEquals(5.0, map.get("duration"));
        Assertions.assertTrue(map.keySet().contains("estimate"));

        final List<String> objects = bodyParser.getObjectNames();
        Assertions.assertEquals(1, objects.size());
        Assertions.assertEquals("estimate", objects.get(0));

        // estimate is a LinkedTreeMap
    }

    @Test
    public void embeddedCollectionOfObjectFromVendorXML() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.addHeader("Content-Type", "application/vnd.example.estimate+xml");
        request.setBody(
                "<estimate><duration>5</duration><estimate><todo><guid>1234567890</guid></todo></estimate></estimate>");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final BodyParser bodyParser = new BodyParser(request, names);

        final Map<String, String> valuesmap = bodyParser.getStringMap();

        Assertions.assertEquals(1, valuesmap.keySet().size());
        Assertions.assertTrue(valuesmap.keySet().contains("duration"));
        Assertions.assertEquals("5.0", valuesmap.get("duration"));

        final Map<String, Object> map = bodyParser.getMap();

        Assertions.assertEquals(2, map.keySet().size());
        Assertions.assertTrue(map.keySet().contains("duration"));
        Assertions.assertEquals(5.0, map.get("duration"));
        Assertions.assertTrue(map.keySet().contains("estimate"));

        final List<String> objects = bodyParser.getObjectNames();
        Assertions.assertEquals(1, objects.size());
        Assertions.assertEquals("estimate", objects.get(0));
    }

    @Test
    public void embeddedCollectionOfObjectsFromXML() {

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.addHeader("Content-Type", "application/xml");
        // <estimate><duration>5</duration><estimates><estimate><guid>1234567890</guid></estimate></estimates></estimate>
        // this is an estimate which wants to be linked to multiple to dos using the estimate
        // relationship - each estimate can  only be linked to 1 to do
        request.setBody(
                "<estimate><duration>5</duration><estimate><todo><guid>1234567890</guid></todo></estimate></estimate>");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final BodyParser bodyParser = new BodyParser(request, names);

        final Map<String, String> valuesmap = bodyParser.getStringMap();

        Assertions.assertEquals(1, valuesmap.keySet().size());
        Assertions.assertTrue(valuesmap.keySet().contains("duration"));
        Assertions.assertEquals("5.0", valuesmap.get("duration"));

        final Map<String, Object> map = bodyParser.getMap();

        Assertions.assertEquals(2, map.keySet().size());
        Assertions.assertTrue(map.keySet().contains("duration"));
        Assertions.assertEquals(5.0, map.get("duration"));
        Assertions.assertTrue(map.keySet().contains("estimate"));

        final List<String> objects = bodyParser.getObjectNames();
        Assertions.assertEquals(1, objects.size());
        Assertions.assertEquals("estimate", objects.get(0));

        // estimate is a LinkedTreeMap of LinkedTreeMap "to do" of ArrayList of LinkedTreeMap
    }

    private String sourceType(final ApiBodyFields fields, final String name) {
        for (ApiBodyField field : fields.topLevelFields()) {
            if (field.name().equals(name)) {
                return field.sourceType();
            }
        }
        return "";
    }

    private String value(final ApiBodyFields fields, final String name) {
        for (ApiBodyField field : fields.topLevelFields()) {
            if (field.name().equals(name)) {
                return field.value();
            }
        }
        return "";
    }
}
