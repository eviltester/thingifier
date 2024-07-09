package uk.co.compendiumdev.practicemodes.simpleapi;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.practicemodes.simpleapi.testabstractions.Item;
import uk.co.compendiumdev.practicemodes.simpleapi.testabstractions.SimpleAPIApi;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.Random;

public class SimpleApiCrudTest {

    /*
        Simple tests based on CRUD flows for ELH

                    Item
              /      |         \
        Create    Existing (*)  Delete
                /          \
              Read (o)     Update (o)
     */

    /*
        Paths To Cover:

        - C - leave alone
        - CD
        - CRD
        - CRUD
        - existing variants (how far to push it?)
           - CRRRD  - how many? why? this would primarily be for multi-user interaction (can we see others' amendments)
           - CUUUUD - how many? why? this would be for field considerations and potential multi-user update clashes
           - CRRUURRD - variants of RU - again why? at a path level this is overkill, what do we think we would gain from this?

        For a CRUD flow to work it has to be valid, so all of the above are 'valid'

        e.g.

        - C
          - valid details (valid values for all fields)
          - minimum (mandatory fields only)
          - mix of optional fields + mandatory fields
          - maximum (all fields)
          - field formats? e.g. can use strings for price? "12.12" [exclude from CRUD]
          - field values? min max etc. [exclude from CRUD]
        - R
          - existing
        - U
          - existing
          - minimum (mandatory fields only) (PUT and POST are different)
          - mix ( mix of optional and mandatory)
          - maximum (all fields)
        - D
          - existing
          - update based on a variety of low level field changes
          - REST Overlay (POST, PUT)
            - expand diagram to include a new layer under update
            - POST (all field update)
            - PUT (all field update)

        Invalid operations:

        - C
          - with invalid data such that it is not created
          - duplicate
        - R
          - a non-existing item
             - (not-created yet, previously deleted)
          - invalid for rendering in output(could invalid data get into the system?)
              [doesn't seem feasible with current data fields]
          - in invalid state (e.g. multi-user, while being updated) [exclude from CRUD]
        - U
          - update a non-existing item
             - (not-created yet, previously deleted)
          - update to become duplicate item should fail
             - can't update id [double check this]
             - can't have duplicate iban13
          - update when someone else has updated (multi-user) [exclude from CRUD]
          - PUT and POST have different update conditions?
          - treat updates as a Matrix transformation of field conditions
            - below minimum (none of the fields - error or no update?)
            - above maximum (more fields than required: extra fields, duplicate fields) [exclude from CRUD?]
        - D
          - delete a non-existing item
            - (not-created yet, previously deleted)

     */


    private static HttpMessageSender http;
    private static SimpleAPIApi api;
    private static Random random;


    @BeforeAll
    static void createHttp(){
        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        http = new HttpMessageSender(Environment.getBaseUri());
        api = new SimpleAPIApi(http);
        random = new Random();
    }

    @Test
    public void createAnItemAndLeaveItUntouched(){

        Item create = new Item();
        create.isbn13 = Item.randomIsbn(random);
        create.type = "dvd";
        create.price = 01.99F;
        
        HttpResponseDetails response = api.apiCreateItemResponse(create);

        System.out.println(response.body);
        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(response.getHeader("location").startsWith("/simpleapi/items/"));

        Item created = new Gson().fromJson(response.body, Item.class);

        Assertions.assertEquals(create.type, created.type);
        Assertions.assertEquals(create.isbn13, created.isbn13);
        Assertions.assertEquals(create.price, created.price);
        Assertions.assertEquals(0, created.numberinstock);


    }
}
