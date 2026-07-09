package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class TodoManagerAPIDataPopulator implements RepositoryDataPopulator {
    @Override
    public void populate(final ERSchema schema, final ThingRepository repository) {
        EntityDefinition todo = schema.getEntityDefinitionNamed("todo");
        EntityDefinition category = schema.getEntityDefinitionNamed("category");
        EntityDefinition project = schema.getEntityDefinitionNamed("project");

        EntityInstance paperwork =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todo).withField("title", "scan paperwork"));

        EntityInstance filework =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todo).withField("title", "file paperwork"));

        EntityInstance officeCategory =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(category).withField("title", "Office"));

        repository.createInstance(
                EntityInstanceDraft.forEntity(category).withField("title", "Home"));

        EntityInstance officeWork =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(project).withField("title", "Office Work"));

        repository.connectRelationship(officeWork, "tasks", paperwork);
        repository.connectRelationship(officeWork, "tasks", filework);

        repository.connectRelationship(paperwork, "categories", officeCategory);
    }
}
