package uk.co.compendiumdev.challenge.apimodel;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.data.ThingifierDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

public class TodoAPIDataPopulator implements ThingifierDataPopulator {

    @Override
    public void populate(final Thingifier thingifier) {

        String [] todos={
                        "scan paperwork",
                        "file paperwork",
                        "process payments",
                        "escalate late payments",
                        "pay invoices",
                        "process payroll",
                        "train staff",
                        "schedule meeting",
                        "tidy meeting room",
                        "install webcam"};

        Thing todo = thingifier.getThingNamed("todo");

        for(String todoItem : todos){
            ThingInstance instance = todo.createInstance().
                                    setValue("title", todoItem);
            todo.addInstance(instance);
        }
    }
}
