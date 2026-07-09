package uk.co.compendiumdev.thingifier.api.http.requests;

import java.util.HashMap;
import java.util.Map;
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
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class ThingifierHttpApiRequestHandlingTest {

    private Thingifier getTestThingifier() {

        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        EntityDefinition defn = thingifier.getERmodel().createEntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        defn.addField(Field.is("title", FieldType.STRING));

        return thingifier;
    }

    @Test
    public void aGetRequestWillCreateNewSessionWithDatabase() {

        Thingifier thingifier = getTestThingifier();

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String, String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.get(new HttpApiRequest("/things").setHeaders(headers));

        // add a thing
        final EntityDefinition thing =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("thing");
        final EntityInstance existingInstance =
                thingifier
                        .getRepository("other_things")
                        .createInstance(
                                EntityInstanceDraft.forEntity(thing)
                                        .withField("title", "My Title" + System.nanoTime()));

        final HttpApiResponse response2 =
                api.get(
                        new HttpApiRequest("/things/" + existingInstance.getPrimaryKeyValue())
                                .setHeaders(headers));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(200, response2.getStatusCode());
    }

    @Test
    public void aRequestWillCreateNewSessionWithDatabaseUsingDefaultData() {

        Thingifier thingifier = getTestThingifier();

        RepositoryDataPopulator dataPopulator =
                (schema, repository) -> {
                    String[] titles = {"thing1", "thing2", "thing3"};
                    EntityDefinition thing = schema.getEntityDefinitionNamed("thing");

                    for (String thingTitle : titles) {
                        repository.createInstance(
                                EntityInstanceDraft.forEntity(thing)
                                        .withField("title", thingTitle));
                    }
                };
        thingifier.setDataGenerator(dataPopulator);

        // populate default database
        dataPopulator.populate(
                thingifier.getERmodel().getSchema(),
                thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME));

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String, String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.get(new HttpApiRequest("/things").setHeaders(headers));

        Assertions.assertEquals(200, response.getStatusCode());

        EntityDefinition thingInstances =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("thing");

        Assertions.assertEquals(
                3, thingifier.getRepository("other_things").countInstances(thingInstances));
    }

    @Test
    public void aPostRequestWillCreateNewSessionWithDatabase() {

        Thingifier thingifier = getTestThingifier();

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String, String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");
        headers.put("content-type", "application/json");

        final HttpApiResponse response =
                api.post(
                        new HttpApiRequest("/things")
                                .setHeaders(headers)
                                .setBody("{\"title\":\"thing1\"}"));

        // check added a thing to db
        Assertions.assertEquals(201, response.getStatusCode());

        EntityDefinition thingInstances =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("thing");

        Assertions.assertEquals(
                1, thingifier.getRepository("other_things").countInstances(thingInstances));

        Assertions.assertEquals(
                0,
                thingifier
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(
                                thingifier
                                        .getERmodel()
                                        .getSchema()
                                        .getDefinitionWithSingularOrPluralNamed("thing")));
    }

    @Test
    public void aDeleteRequestWillCreateNewSessionWithDatabase() {

        Thingifier thingifier = getTestThingifier();

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String, String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response =
                api.delete(new HttpApiRequest("/things/1").setHeaders(headers));

        // check added a thing to db
        Assertions.assertEquals(404, response.getStatusCode());

        EntityDefinition thingInstances =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("thing");

        EntityInstance anInstance =
                thingifier
                        .getRepository("other_things")
                        .createInstance(EntityInstanceDraft.forEntity(thingInstances));

        Assertions.assertEquals(
                1, thingifier.getRepository("other_things").countInstances(thingInstances));

        final HttpApiResponse actualDeleteResponse =
                api.delete(
                        new HttpApiRequest("/things/" + anInstance.getPrimaryKeyValue())
                                .setHeaders(headers));
        Assertions.assertEquals(200, actualDeleteResponse.getStatusCode());

        Assertions.assertEquals(
                0, thingifier.getRepository("other_things").countInstances(thingInstances));

        Assertions.assertEquals(
                0,
                thingifier
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(
                                thingifier
                                        .getERmodel()
                                        .getSchema()
                                        .getDefinitionWithSingularOrPluralNamed("thing")));
    }

    // OPTIONS
    // etc.
}
