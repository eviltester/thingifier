package uk.co.compendiumdev.challenge.practicemodes.simpleapi;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class AddMoreItemsIfNecessary implements HttpApiRequestHook {

    private final EntityRelModel erModel;

    public AddMoreItemsIfNecessary(EntityRelModel eRmodel) {
        this.erModel = eRmodel;
    }

    @Override
    public HttpApiResponse run(HttpApiRequest request, ThingifierApiConfig config) {

        EntityDefinition item = erModel.getSchema().getEntityDefinitionNamed("item");
        ThingRepository repository = erModel.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        if (repository != null && item != null && repository.countInstances(item) < 5) {
            erModel.populateDatabase(EntityRelModel.DEFAULT_DATABASE_NAME);
        }
        return null;
    }
}
