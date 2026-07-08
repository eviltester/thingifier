package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class TodoAPIDataPopulator implements RepositoryDataPopulator {

    private static final String[] TODOS = {
            "scan paperwork",
            "file paperwork",
            "process payments",
            "escalate late payments",
            "pay invoices",
            "process payroll",
            "train staff",
            "schedule meeting"};

    @Override
    public void populate(final ERSchema schema, final ThingRepository repository) {
        EntityDefinition todo = schema.getEntityDefinitionNamed("todo");

        for (String todoItem : TODOS) {
            repository.addInstance(new EntityInstance(todo).
                    setValue("title", todoItem));
        }
    }
}
