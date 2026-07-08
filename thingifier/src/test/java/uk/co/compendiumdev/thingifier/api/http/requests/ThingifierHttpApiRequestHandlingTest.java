package uk.co.compendiumdev.thingifier.api.http.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.testsupport.ThingifierRepositoryTestSupport;

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
        final EntityDefinition thing = ThingifierRepositoryTestSupport.entity(thingifier, "other_things", "thing");
        final EntityInstance existingInstance = ThingifierRepositoryTestSupport.repository(thingifier, "other_things").addInstance(new EntityInstance(thing)).setValue("title", "My Title" + System.nanoTime());

        final HttpApiResponse response2 = api.get(new HttpApiRequest("/things/" + existingInstance.getPrimaryKeyValue()).setHeaders(headers));

        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void aRequestWillCreateNewSessionWithDatabaseUsingDefaultData(){

        Thingifier thingifier = getTestThingifier();

        RepositoryDataPopulator dataPopulator = (schema, repository) -> {
            String [] titles= {"thing1", "thing2", "thing3"};
            EntityDefinition thing = schema.getEntityDefinitionNamed("thing");

            for(String thingTitle : titles){
                repository.addInstance(new EntityInstance(thing).setValue("title", thingTitle));
            }
        };
        thingifier.setDataGenerator(dataPopulator);

        // populate default database
        dataPopulator.populate(
                thingifier.getERmodel().getSchema(),
                thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME));

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String,String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.get(new HttpApiRequest("/things").setHeaders(headers));

        Assertions.assertEquals(200, response.getStatusCode());

        EntityDefinition thingInstances =
                ThingifierRepositoryTestSupport.entity(thingifier, "other_things", "thing");

        Assertions.assertEquals(3, ThingifierRepositoryTestSupport.repository(thingifier, "other_things").countInstances(thingInstances));
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

        EntityDefinition thingInstances =
                ThingifierRepositoryTestSupport.entity(thingifier, "other_things", "thing");

        Assertions.assertEquals(1, ThingifierRepositoryTestSupport.repository(thingifier, "other_things").countInstances(thingInstances));

        Assertions.assertEquals(0,
                ThingifierRepositoryTestSupport.repository(thingifier).countInstances(
                        ThingifierRepositoryTestSupport.entity(thingifier, "thing")));
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

        EntityDefinition thingInstances =
                ThingifierRepositoryTestSupport.entity(thingifier, "other_things", "thing");

        EntityInstance anInstance = ThingifierRepositoryTestSupport.repository(thingifier, "other_things").addInstance(new EntityInstance(thingInstances));

        Assertions.assertEquals(1, ThingifierRepositoryTestSupport.repository(thingifier, "other_things").countInstances(thingInstances));

        final HttpApiResponse actualDeleteResponse = api.delete(new HttpApiRequest("/things/" + anInstance.getPrimaryKeyValue())
                .setHeaders(headers)
        );

        Assertions.assertEquals(0, ThingifierRepositoryTestSupport.repository(thingifier, "other_things").countInstances(thingInstances));

        Assertions.assertEquals(0,
                ThingifierRepositoryTestSupport.repository(thingifier).countInstances(
                        ThingifierRepositoryTestSupport.entity(thingifier, "thing")));
    }

    // OPTIONS
    // etc.
}
