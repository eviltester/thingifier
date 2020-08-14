package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

public class TodoManagerAPIDataPopulator implements DataPopulator {
    @Override
    public void populate(final EntityRelModel erm) {

        Thing todo = erm.getThingNamed("todo");
        ThingInstance paperwork = todo.createManagedInstance().
                setValue("title", "scan paperwork");

        ThingInstance filework = todo.createManagedInstance().
                setValue("title", "file paperwork");

        Thing category = erm.getThingNamed("category");

        ThingInstance officeCategory = category.createManagedInstance().
                setValue("title", "Office");


        ThingInstance homeCategory = category.createManagedInstance().
                setValue("title", "Home");

        Thing project = erm.getThingNamed("project");

        ThingInstance officeWork = project.createManagedInstance().
                setValue("title", "Office Work");

        officeWork.getRelationships().connect("tasks", paperwork);
        officeWork.getRelationships().connect("tasks", filework);

        paperwork.getRelationships().connect("categories", officeCategory);

    }
}
