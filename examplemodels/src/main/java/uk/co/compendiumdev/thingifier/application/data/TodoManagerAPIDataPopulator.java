package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class TodoManagerAPIDataPopulator implements DataPopulator {
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
}
