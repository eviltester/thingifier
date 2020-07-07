package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;

import java.util.*;

public class BodyParserTest {

    @Test
    public void simpleJsonParse(){

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
    public void embeddedObjectParseIgnoredOnStringMap(){

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
    public void embeddedObjectParseFoundOnMapParse(){

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
    public void embeddedCollectionOfObject(){

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
    public void embeddedCollectionOfObjects(){

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setBody("{'duration':'5', 'estimate' : [{'guid' : '1234567890'}, {'guid' : '12345678901234567890'}]}");

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
    public void embeddedCollectionOfObjectFromXML(){

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.addHeader("Content-Type", "application/xml");
        // <estimate><duration>5</duration><estimates><estimate><guid>1234567890</guid></estimate></estimates></estimate>
        request.setBody("<estimate><duration>5</duration><estimate><todo><guid>1234567890</guid></todo></estimate></estimate>");

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
    public void embeddedCollectionOfObjectsFromXML(){

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.addHeader("Content-Type", "application/xml");
        // <estimate><duration>5</duration><estimates><estimate><guid>1234567890</guid></estimate></estimates></estimate>
        // this is an estimate which wants to be linked to multiple to dos using the estimate relationship - each estimate can  only be linked to 1 to do
        request.setBody("<estimate><duration>5</duration><estimate><todo><guid>1234567890</guid></todo></estimate></estimate>");

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
}
