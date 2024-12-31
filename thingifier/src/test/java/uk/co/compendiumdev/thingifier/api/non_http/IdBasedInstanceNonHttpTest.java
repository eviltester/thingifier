package uk.co.compendiumdev.thingifier.api.non_http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

public class IdBasedInstanceNonHttpTest {

    public Thingifier getThingifier() {
        Thingifier thingifier = new Thingifier();
        thingifier.setDocumentation("Model", "test model");

        EntityDefinition thing = thingifier.defineThing("thing", "things");
        thing.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        thing
                .addFields(Field.is("title", STRING),
                        Field.is("id", FieldType.AUTO_INCREMENT)
                )
        ;

        thingifier.apiConfig().setReturnSingleGetItemsAsCollection(false);

        return thingifier;
    }


    @Test
    public void canGetAThingUsingGuidFromDefaultSession(){

        Thingifier model = getThingifier();

        final EntityInstanceCollection thing = model.getThingInstancesNamed("thing", EntityRelModel.DEFAULT_DATABASE_NAME);
        final EntityInstance existingInstance = thing.addInstance(new EntityInstance(thing.definition())).setValue("title",
                "My Title" + System.nanoTime());

        // no session header so use default session
        final ApiResponse apiResponse = model.api().get("/thing/" + existingInstance.getPrimaryKeyValue(), new QueryFilterParams(), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiResponse.getStatusCode());
        Assertions.assertEquals(existingInstance, apiResponse.getReturnedInstance());
    }

    @Test
    public void canGetAThingUsingIdFromDefaultSession(){

        Thingifier model = getThingifier();

        final EntityInstanceCollection thing = model.getThingInstancesNamed("thing", EntityRelModel.DEFAULT_DATABASE_NAME);
        final EntityInstance existingInstance = thing.addInstance(new EntityInstance(thing.definition())).setValue("title",
                "My Title" + System.nanoTime());

        // no session header so use default session
        final ApiResponse idApiResponse = model.api().get("/thing/" + existingInstance.getFieldValue("id").asString(), new QueryFilterParams(), new HttpHeadersBlock());
        Assertions.assertEquals(200, idApiResponse.getStatusCode());
        Assertions.assertEquals(existingInstance, idApiResponse.getReturnedInstance());

    }

    @Test
    public void canGetAThingUsingGuidFromCustomSession(){

        Thingifier model = getThingifier();

        final HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        // we are bypassing the HTTP api so need to create the database
        model.getERmodel().createInstanceDatabase("other_things");

        final ApiResponse idApiResponse = model.api().get("/thing/200", new QueryFilterParams(), headers);
        Assertions.assertEquals(404, idApiResponse.getStatusCode());

        // add instance to custom session
        final EntityInstanceCollection thing = model.getERmodel().getInstanceData("other_things").getInstanceCollectionForEntityNamed("thing");
        final EntityInstance existingInstance = thing.addInstance(new EntityInstance(thing.definition())).setValue("title", "My Title" + System.nanoTime());

        final ApiResponse idApiResponse2 = model.api().get("/thing/" + existingInstance.getPrimaryKeyValue(), new QueryFilterParams(), headers);
        Assertions.assertEquals(200, idApiResponse2.getStatusCode());
        Assertions.assertEquals(existingInstance, idApiResponse2.getReturnedInstance());
    }

    @Test
    public void canGetAThingUsingIdFromCustomSession(){

        Thingifier model = getThingifier();

        final HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        // we are bypassing the HTTP api so need to create the database
        model.getERmodel().createInstanceDatabase("other_things");

        final ApiResponse idApiResponse = model.api().get("/thing/200", new QueryFilterParams(), headers);
        Assertions.assertEquals(404, idApiResponse.getStatusCode());

        // add instance to custom session
        final EntityInstanceCollection thing = model.getERmodel().getInstanceData("other_things").getInstanceCollectionForEntityNamed("thing");
        final EntityInstance existingInstance = thing.addInstance(new EntityInstance(thing.definition())).setValue("title", "My Title" + System.nanoTime());

        final ApiResponse idApiResponse2 = model.api().get("/thing/" + existingInstance.getFieldValue("id").asString(), new QueryFilterParams(), headers);
        Assertions.assertEquals(200, idApiResponse2.getStatusCode());
        Assertions.assertEquals(existingInstance, idApiResponse2.getReturnedInstance());
    }
}
