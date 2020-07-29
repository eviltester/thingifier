package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.domain.data.ThingifierDataPopulator;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

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
                        "schedule meeting"};

        Thing todo = thingifier.getThingNamed("todo");

        for(String todoItem : todos){
            ThingInstance instance = todo.createInstance().
                                    setValue("title", todoItem);
            todo.addInstance(instance);
        }
    }
}
