package uk.co.compendiumdev.simpleapi;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.simpleapi.payloads.Item;
import uk.co.compendiumdev.simpleapi.payloads.Items;
import uk.co.compendiumdev.sparkstart.Environment;

import java.math.BigDecimal;

public class BasicSimpleApiCrudCoverageTest {

    String apiPathPrefix = Environment.getBaseUri();

    // TODO: risk that the isbns here are not unique and tests fail intermittently

    @BeforeAll
    static void logRestAssuredCalls(){
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    void canGetAllItems(){

        Items response = RestAssured.
                given().
                accept("application/json").
                get(apiPath( "/items")).
                then().
                statusCode(200).
                contentType(ContentType.JSON).extract().response().body().as(Items.class);

        Assertions.assertFalse(response.items.isEmpty());
    }

    @Test
    void canCreateAnItemWithPost(){

        Item anItem = new Item();
        // anItem.id is auto generated
        anItem.isbn13 = "999-9-99-123456-1";
        anItem.numberinstock =23;
        anItem.type="dvd";
        anItem.price = new BigDecimal("51.29");

        Item response = RestAssured.
                given().contentType(ContentType.JSON).body(anItem).
                accept("application/json").
                post(apiPath( "/items")).
                then().
                statusCode(201).
                contentType(ContentType.JSON).extract().response().body().as(Item.class);

        Assertions.assertTrue(response.id > 0);
        Assertions.assertEquals(anItem.isbn13, response.isbn13);
        Assertions.assertEquals(anItem.type, response.type);
        Assertions.assertEquals(anItem.price, response.price);
    }

    @Test
    void canGetACreatedItem(){

        Item anItem = new Item();
        // anItem.id is auto generated
        anItem.isbn13 = "999-9-99-123456-2";
        anItem.numberinstock =23;
        anItem.type="dvd";
        anItem.price = new BigDecimal("51.29");

        Item response = RestAssured.
                given().contentType(ContentType.JSON).body(anItem).
                accept("application/json").
                post(apiPath( "/items")).
                then().
                statusCode(201).
                contentType(ContentType.JSON).extract().response().body().as(Item.class);

        Item getResponse = RestAssured.
                given().
                accept("application/json").
                get(apiPath( "/items/" + response.id)).
                then().
                statusCode(200).
                contentType(ContentType.JSON).extract().response().body().as(Item.class);

        Assertions.assertEquals(response.id, getResponse.id);
        Assertions.assertEquals(anItem.isbn13, getResponse.isbn13);
        Assertions.assertEquals(anItem.type, getResponse.type);
        Assertions.assertEquals(anItem.price, getResponse.price);
    }

    @Test
    void canDeleteAnItem(){

        // get all items
        Items response = RestAssured.
                given().
                accept("application/json").
                get(apiPath( "/items")).
                then().
                statusCode(200).
                contentType(ContentType.JSON).extract().response().body().as(Items.class);

        // delete the first one
        Integer deleteId = response.items.get(0).id;
        RestAssured.
                given().
                accept("application/json").
                delete(apiPath( "/items/" + deleteId)).
                then().
                statusCode(200);

        // check it is deleted
        RestAssured.
                given().
                accept("application/json").
                get(apiPath( "/items/" + deleteId)).
                then().
                statusCode(404);

    }

    private String apiPath(String postfix) {
        return apiPathPrefix + "/simpleapi" + postfix;
    }
}
