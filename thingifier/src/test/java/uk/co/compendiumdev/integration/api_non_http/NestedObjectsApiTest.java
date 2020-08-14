package uk.co.compendiumdev.integration.api_non_http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;


public class NestedObjectsApiTest {

    Thingifier thingifier;
    ThingDefinition defn;
    ThingInstance instance;
    ThingifierHttpApi api;
    Thing thing;

    @BeforeEach
    public void createThingWithNestedObjectField(){

        thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);

        thing = thingifier.createThing("thing", "things");
        defn = thing.definition();
        defn.addField(Field.is("person", FieldType.OBJECT)
                .withField(
                        Field.is("firstname").
                                withExample("Bob")).
                        withField(
                                Field.is("surname").withExample("D'obbs")
                        ));

    }

    @Test
    void canAmendConnie(){

        instance = ThingInstance.create(defn);
        instance.setValue("person.firstname", "Connie");
        instance.setValue("person.surname", "Dobbs");

        thing.addInstance(instance);

        api = new ThingifierHttpApi(thingifier,
                null, null);


        final HttpApiRequest amendConnieRequest = new HttpApiRequest("/things/" + instance.getGUID());
        amendConnieRequest.setVerb(HttpApiRequest.VERB.POST);
        //{"person" : {"firstname": "bob"}}
        amendConnieRequest.setBody("{\"person\" : {\"firstname\": \"bob\"}}");

        final HttpApiResponse response = api.post(amendConnieRequest);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("bob",
                instance.getFieldValue("person").asObject().getFieldValue("firstname").asString());
    }

    @Test
    void canCreateBob(){

        api = new ThingifierHttpApi(thingifier,
                null, null);

        Assertions.assertEquals(0,thing.countInstances());

        final HttpApiRequest createBobRequest = new HttpApiRequest("/things");
        createBobRequest.setVerb(HttpApiRequest.VERB.POST);
        //{"person" : {"firstname": "bob", "surname" : "dobbs"}}
        createBobRequest.setBody("{\"person\" : {\"firstname\": \"bob\", \"surname\" : \"dobbs\"}}");

        final HttpApiResponse response = api.post(createBobRequest);

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1,thing.countInstances());

        for(ThingInstance bob : thing.getInstances()){
            Assertions.assertEquals("bob", bob.getFieldValue("person").asObject().
                    getFieldValue("firstname").asString());
        }
    }

    @Test
    public void failValidationAtObjectFieldLevel() {

        defn.getField("person").
                getObjectDefinition().
                getField("surname").makeMandatory();

        api = new ThingifierHttpApi(thingifier,
                null, null);

        Assertions.assertEquals(0,thing.countInstances());

        final HttpApiRequest failToCreateBobRequest = new HttpApiRequest("/things");
        failToCreateBobRequest.setVerb(HttpApiRequest.VERB.POST);
        //{"person" : {"firstname": "bob"}}
        failToCreateBobRequest.setBody("{\"person\" : {\"firstname\": \"bob\"}}");

        final HttpApiResponse response = api.post(failToCreateBobRequest);

        Assertions.assertEquals(400, response.getStatusCode());
        Assertions.assertEquals(0,thing.countInstances());

        Assertions.assertTrue(
            response.apiResponse().getErrorMessages().contains("surname : field is mandatory"));
    }
}
