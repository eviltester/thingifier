package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class TodoManagerAPIDataPopulator implements RepositoryDataPopulator {
    @Override
    public void populate(final ERSchema schema, final ERInstanceData database) {

        EntityInstanceCollection todo = database.getInstanceCollectionForEntityNamed("todo");
        EntityInstance paperwork = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "scan paperwork");

        EntityInstance filework = todo.addInstance(new EntityInstance(todo.definition())).
                setValue("title", "file paperwork");

        EntityInstanceCollection category = database.getInstanceCollectionForEntityNamed("category");

        EntityInstance officeCategory = category.addInstance(new EntityInstance(category.definition())).
                setValue("title", "Office");


        EntityInstance homeCategory = category.addInstance(new EntityInstance(category.definition())).
                setValue("title", "Home");

        EntityInstanceCollection project = database.getInstanceCollectionForEntityNamed("project");

        EntityInstance officeWork = project.addInstance(new EntityInstance(project.definition())).
                setValue("title", "Office Work");

        officeWork.getRelationships().connect("tasks", paperwork);
        officeWork.getRelationships().connect("tasks", filework);

        paperwork.getRelationships().connect("categories", officeCategory);

    }

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
