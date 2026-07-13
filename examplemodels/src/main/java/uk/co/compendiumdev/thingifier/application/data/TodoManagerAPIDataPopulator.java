package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class TodoManagerAPIDataPopulator implements RepositoryDataPopulator {
    @Override
    public void populate(final ERSchema schema, final ThingStore store) {
        EntityDefinition todo = schema.getEntityDefinitionNamed("todo");
        EntityDefinition category = schema.getEntityDefinitionNamed("category");
        EntityDefinition project = schema.getEntityDefinitionNamed("project");

        EntityInstance paperwork =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "scan paperwork"));

        EntityInstance filework =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "file paperwork"));

        EntityInstance officeCategory =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(category)
                                        .withField("title", "Office"));

        store.entities().create(EntityInstanceDraft.forEntity(category).withField("title", "Home"));

        EntityInstance officeWork =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Office Work"));

        store.relationships().connect(officeWork, "tasks", paperwork);
        store.relationships().connect(officeWork, "tasks", filework);

        store.relationships().connect(paperwork, "categories", officeCategory);
    }
}
