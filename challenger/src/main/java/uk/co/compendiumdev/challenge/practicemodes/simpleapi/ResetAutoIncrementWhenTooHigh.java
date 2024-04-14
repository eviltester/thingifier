package uk.co.compendiumdev.challenge.practicemodes.simpleapi;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

public class ResetAutoIncrementWhenTooHigh implements HttpApiRequestHook {

    private final EntityRelModel erModel;

    public ResetAutoIncrementWhenTooHigh(EntityRelModel eRmodel){
        this.erModel = eRmodel;
    }

    @Override
    public HttpApiResponse run(HttpApiRequest request, ThingifierApiConfig config) {

        ERInstanceData instanceData = erModel.getInstanceData(EntityRelModel.DEFAULT_DATABASE_NAME);
        if(instanceData!=null){
            EntityInstanceCollection collection = instanceData.
                    getInstanceCollectionForEntityNamed("item");
            AutoIncrement idCounter = collection.getCounters().get("id");
            //if(idCounter.getCurrentValue()>2140000000){
            if(idCounter.getCurrentValue()>99999){
                // reset it
                idCounter.incrementToNextAbove(0);
            }
            if(collection != null && collection.countInstances()<5) {
                erModel.populateDatabase(EntityRelModel.DEFAULT_DATABASE_NAME);
            }
        }
        return null;
    }
}
