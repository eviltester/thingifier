package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.ThingifierDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

public class TodoManagerAPIDataPopulator implements ThingifierDataPopulator {
    @Override
    public void populate(final Thingifier thingifier) {

        Thing todo = thingifier.getThingNamed("todo");
        ThingInstance paperwork = todo.createManagedInstance().
                setValue("title", "scan paperwork");

        ThingInstance filework = todo.createManagedInstance().
                setValue("title", "file paperwork");

        Thing category = thingifier.getThingNamed("category");

        ThingInstance officeCategory = category.createManagedInstance().
                setValue("title", "Office");


        ThingInstance homeCategory = category.createManagedInstance().
                setValue("title", "Home");

        Thing project = thingifier.getThingNamed("project");

        ThingInstance officeWork = project.createManagedInstance().
                setValue("title", "Office Work");

        officeWork.getRelationships().connect("tasks", paperwork);
        officeWork.getRelationships().connect("tasks", filework);

        paperwork.getRelationships().connect("categories", officeCategory);

    }
}
