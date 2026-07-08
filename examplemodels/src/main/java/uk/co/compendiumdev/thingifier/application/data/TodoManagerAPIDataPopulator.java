package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class TodoManagerAPIDataPopulator implements RepositoryDataPopulator {
    @Override
    public void populate(final ERSchema schema, final ThingRepository repository) {
        EntityDefinition todo = schema.getEntityDefinitionNamed("todo");
        EntityDefinition category = schema.getEntityDefinitionNamed("category");
        EntityDefinition project = schema.getEntityDefinitionNamed("project");

        EntityInstance paperwork = repository.addInstance(new EntityInstance(todo).
                setValue("title", "scan paperwork"));

        EntityInstance filework = repository.addInstance(new EntityInstance(todo).
                setValue("title", "file paperwork"));

        EntityInstance officeCategory = repository.addInstance(new EntityInstance(category).
                setValue("title", "Office"));

        repository.addInstance(new EntityInstance(category).
                setValue("title", "Home"));

        EntityInstance officeWork = repository.addInstance(new EntityInstance(project).
                setValue("title", "Office Work"));

        repository.connectRelationship(officeWork, "tasks", paperwork);
        repository.connectRelationship(officeWork, "tasks", filework);

        repository.connectRelationship(paperwork, "categories", officeCategory);
    }
}
