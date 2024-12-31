package uk.co.compendiumdev.thingifier.swaggerizer;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;

public class TodoAPITestDataPopulator implements DataPopulator {

    @Override
    public void populate(final ERSchema schema, final ERInstanceData database) {

        String [] todos={
                        "scan paperwork",
                        "file paperwork",
                        "process payments",
                        "escalate late payments",
                        "pay invoices",
                        "process payroll",
                        "train staff",
                        "schedule meeting"};

        EntityInstanceCollection todo = database.getInstanceCollectionForEntityNamed("todo");

        for(String todoItem : todos){
            todo.addInstance(new EntityInstance(todo.definition())).
                                    setValue("title", todoItem);
        }
    }
}
