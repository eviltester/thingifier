package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;

public class TodoAPIDataPopulator implements DataPopulator {

    @Override
    public void populate(final EntityRelModel erm) {

        String [] todos={
                        "scan paperwork",
                        "file paperwork",
                        "process payments",
                        "escalate late payments",
                        "pay invoices",
                        "process payroll",
                        "train staff",
                        "schedule meeting"};

        Thing todo = erm.getThingNamed("todo");

        for(String todoItem : todos){
            todo.createManagedInstance().
                    setValue("title", todoItem);
        }
    }
}
