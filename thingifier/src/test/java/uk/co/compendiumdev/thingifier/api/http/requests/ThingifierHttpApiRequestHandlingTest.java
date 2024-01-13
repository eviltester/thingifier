package uk.co.compendiumdev.thingifier.api.http.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

import java.util.HashMap;
import java.util.Map;

public class ThingifierHttpApiRequestHandlingTest {

    @Test
    public void aGetRequestWillCreateNewSessionWithDatabase(){

        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        EntityDefinition defn = thingifier.getERmodel().createEntityDefinition("thing", "things");
        defn.addField(Field.is("title", FieldType.STRING));

        final ThingifierHttpApi api =
                new ThingifierHttpApi(thingifier, null, null);

        final Map<String,String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.get(new HttpApiRequest("/things").setHeaders(headers));

        // add a thing
        final EntityInstanceCollection thing = thingifier.getERmodel().getInstanceData("other_things").getInstanceCollectionForEntityNamed("thing");
        final EntityInstance existingInstance = thing.createManagedInstance().setValue("title", "My Title" + System.nanoTime());

        final HttpApiResponse response2 = api.get(new HttpApiRequest("/things/" + existingInstance.getGUID()).setHeaders(headers));

        Assertions.assertEquals(200, response.getStatusCode());
    }
}
