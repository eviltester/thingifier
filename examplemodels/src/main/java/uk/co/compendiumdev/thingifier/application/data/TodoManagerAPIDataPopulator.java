package uk.co.compendiumdev.thingifier.application.data;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.data.ThingifierDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

public class TodoManagerAPIDataPopulator implements ThingifierDataPopulator {
    @Override
    public void populate(final Thingifier thingifier) {

        Thing todo = thingifier.getThingNamed("todo");
        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);

        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        Thing category = thingifier.getThingNamed("category");

        ThingInstance officeCategory = category.createInstance().setValue("title", "Office");
        category.addInstance(officeCategory);

        ThingInstance homeCategory = category.createInstance().setValue("title", "Home");
        category.addInstance(homeCategory);

        Thing project = thingifier.getThingNamed("project");

        ThingInstance officeWork = project.createInstance().setValue("title", "Office Work");
        project.addInstance(officeWork);

        officeWork.getRelationships().connect("tasks", paperwork);
        officeWork.getRelationships().connect("tasks", filework);

        paperwork.getRelationships().connect("categories", officeCategory);

    }
}
