package uk.co.compendiumdev.challenge.apimodel;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class TodoAPIDataPopulator implements RepositoryDataPopulator {

    @Override
    public void populate(final ERSchema schema, final ThingStore store) {
        String[] todos = {
            "scan paperwork",
            "file paperwork",
            "process payments",
            "escalate late payments",
            "pay invoices",
            "process payroll",
            "train staff",
            "schedule meeting",
            "tidy meeting room",
            "install webcam"
        };

        EntityDefinition todo = schema.getEntityDefinitionNamed("todo");

        for (String todoItem : todos) {
            store.entities()
                    .create(EntityInstanceDraft.forEntity(todo).withField("title", todoItem));
        }
    }
}
