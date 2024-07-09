package uk.co.compendiumdev.practicemodes.simpleapi.testabstractions;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;

import java.util.HashMap;
import java.util.Map;

public class SimpleAPIApi {
    private final HttpMessageSender http;

    public SimpleAPIApi(HttpMessageSender http) {
        this.http = http;
    }

    /*
        All the api abstraction classes to support the simple HTTP Tests
    */

    public HttpResponseDetails apiGetItemResponse(Integer id) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        return http.send("/simpleapi/items/" + id, "GET", headers, "");
    }

    public Item apiGetItem(Integer id) {
        final HttpResponseDetails response = apiGetItemResponse(id);

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/json", response.getHeader("content-type"));

        // thingifier is configurable and can send a single item back on a GET /things/:id
        return new Gson().fromJson(response.body, Item.class);
    }

    public HttpResponseDetails apiCreateItemResponse(Item itemToCreate) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        return http.send("/simpleapi/items", "POST", headers,
                new Gson().toJson(itemToCreate));
    }

    public Item apiCreateItem(Item itemToCreate) {

        final HttpResponseDetails response = apiCreateItemResponse(itemToCreate);

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertEquals("application/json", response.getHeader("content-type"));

        return new Gson().fromJson(response.body, Item.class);
    }

    public HttpResponseDetails apiDeleteItem(Integer id) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        return http.send("/simpleapi/items/" + id, "DELETE", headers, "");
    }

    public Items apiGetItems() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        final HttpResponseDetails response =
                http.send("/simpleapi/items", "GET", headers,
                        """
                                """.stripIndent());

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/json", response.getHeader("content-type"));

        return new Gson().fromJson(response.body, Items.class);
    }

}
