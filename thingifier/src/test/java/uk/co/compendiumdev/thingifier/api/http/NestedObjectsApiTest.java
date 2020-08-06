package uk.co.compendiumdev.thingifier.api.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.InstanceFields;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;


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

        instance = new ThingInstance(defn);
        final InstanceFields person = instance.getObjectValue("person");
        person.addValue("firstname", "Connie");
        person.addValue("surname", "Dobbs");

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
                instance.getObjectValue("person").getValue("firstname").asString());
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
            Assertions.assertEquals("bob", bob.getObjectValue("person").
                                    getValue("firstname").asString());
        }
    }
}
