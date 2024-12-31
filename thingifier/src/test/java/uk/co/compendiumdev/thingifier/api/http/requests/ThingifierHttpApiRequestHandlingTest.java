package uk.co.compendiumdev.thingifier.api.http.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

import java.util.HashMap;
import java.util.Map;

public class ThingifierHttpApiRequestHandlingTest {

    private Thingifier getTestThingifier(){

        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        EntityDefinition defn = thingifier.getERmodel().createEntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        defn.addField(Field.is("title", FieldType.STRING));

        return thingifier;
    }

    @Test
    public void aGetRequestWillCreateNewSessionWithDatabase(){

        Thingifier thingifier = getTestThingifier();

        final ThingifierHttpApi api =
                new ThingifierHttpApi(thingifier, null, null);

        final Map<String,String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.get(new HttpApiRequest("/things").setHeaders(headers));

        // add a thing
        final EntityInstanceCollection thing = thingifier.getERmodel().getInstanceData("other_things").getInstanceCollectionForEntityNamed("thing");
        final EntityInstance existingInstance = thing.addInstance(new EntityInstance(thing.definition())).setValue("title", "My Title" + System.nanoTime());

        final HttpApiResponse response2 = api.get(new HttpApiRequest("/things/" + existingInstance.getPrimaryKeyValue()).setHeaders(headers));

        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void aRequestWillCreateNewSessionWithDatabaseUsingDefaultData(){

        Thingifier thingifier = getTestThingifier();

        DataPopulator dataPopulator = new DataPopulator() {
            @Override
            public void populate(final ERSchema schema, final ERInstanceData database) {

                String [] titles= {"thing1", "thing2", "thing3"};
                EntityInstanceCollection things = database.getInstanceCollectionForEntityNamed("thing");

                for(String thingTitle : titles){
                    things.addInstance(new EntityInstance(things.definition())).setValue("title", thingTitle);
                }
            }
        };
        thingifier.setDataGenerator(dataPopulator);

        // populate default database
        dataPopulator.populate(thingifier.getERmodel().getSchema(), thingifier.getERmodel().getInstanceData());

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String,String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.get(new HttpApiRequest("/things").setHeaders(headers));

        Assertions.assertEquals(200, response.getStatusCode());

        EntityInstanceCollection thingInstances = thingifier.getERmodel().
                                                        getInstanceData("other_things").
                                                        getInstanceCollectionForEntityNamed("thing");

        Assertions.assertEquals(3, thingInstances.countInstances());
    }

    @Test
    public void aPostRequestWillCreateNewSessionWithDatabase(){

        Thingifier thingifier = getTestThingifier();

        final ThingifierHttpApi api =
                new ThingifierHttpApi(thingifier, null, null);

        final Map<String,String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");
        headers.put("content-type", "application/json");

        final HttpApiResponse response = api.post(new HttpApiRequest("/things")
                                            .setHeaders(headers)
                                            .setBody("{\"title\":\"thing1\"}")
                                            );

        // check added a thing to db
        Assertions.assertEquals(201, response.getStatusCode());

        EntityInstanceCollection thingInstances = thingifier.getERmodel().
                getInstanceData("other_things").
                getInstanceCollectionForEntityNamed("thing");

        Assertions.assertEquals(1, thingInstances.countInstances());

        Assertions.assertEquals(0, thingifier.getERmodel().
                getInstanceData(EntityRelModel.DEFAULT_DATABASE_NAME).
                getInstanceCollectionForEntityNamed("thing").countInstances());
    }

    @Test
    public void aDeleteRequestWillCreateNewSessionWithDatabase(){

        Thingifier thingifier = getTestThingifier();

        final ThingifierHttpApi api =
                new ThingifierHttpApi(thingifier, null, null);

        final Map<String,String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.delete(new HttpApiRequest("/things/1")
                .setHeaders(headers)
        );

        // check added a thing to db
        Assertions.assertEquals(404, response.getStatusCode());

        EntityInstanceCollection thingInstances = thingifier.getERmodel().
                getInstanceData("other_things").
                getInstanceCollectionForEntityNamed("thing");

        EntityInstance anInstance = thingInstances.addInstance(new EntityInstance(thingInstances.definition()));

        Assertions.assertEquals(1, thingInstances.countInstances());

        final HttpApiResponse actualDeleteResponse = api.delete(new HttpApiRequest("/things/" + anInstance.getPrimaryKeyValue())
                .setHeaders(headers)
        );

        Assertions.assertEquals(0, thingInstances.countInstances());

        Assertions.assertEquals(0, thingifier.getERmodel().
                getInstanceData(EntityRelModel.DEFAULT_DATABASE_NAME).
                getInstanceCollectionForEntityNamed("thing").countInstances());
    }

    // OPTIONS
    // etc.
}
