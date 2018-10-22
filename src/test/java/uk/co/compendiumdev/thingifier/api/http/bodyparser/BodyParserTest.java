package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import org.junit.Assert;
import org.junit.Test;
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

        Assert.assertEquals(1, map.keySet().size());
        Assert.assertTrue(map.keySet().contains("duration"));
        Assert.assertEquals("5", map.get("duration"));
    }



    @Test
    public void embeddedObjectParseIgnoredOnStringMap(){

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setBody("{'duration':'5', 'estimate' : {'guid' : '1234567890'}}");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final Map<String, String> map = new BodyParser(request, names).getStringMap();

        Assert.assertEquals(1, map.keySet().size());
        Assert.assertTrue(map.keySet().contains("duration"));
        Assert.assertEquals("5", map.get("duration"));
    }

    @Test
    public void embeddedObjectParseFoundOnMapParse(){

        HttpApiRequest request = new HttpApiRequest("/estimates");
        request.setBody("{'duration':'5', 'estimate' : {'guid' : '1234567890'}}");

        List<String> names = new ArrayList<>();

        names.add("estimate");

        final Map<String, Object> map = new BodyParser(request, names).getMap();

        Assert.assertEquals(2, map.keySet().size());
        Assert.assertTrue(map.keySet().contains("duration"));
        Assert.assertEquals("5", map.get("duration"));
        Assert.assertTrue(map.keySet().contains("estimate"));
    }
}
