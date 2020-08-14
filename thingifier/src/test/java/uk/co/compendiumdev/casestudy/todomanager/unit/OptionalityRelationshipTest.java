package uk.co.compendiumdev.casestudy.todomanager.unit;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.Collection;

public class OptionalityRelationshipTest {

    // relationships can be Optional:Optional
    // or Mandatory:Optional
    // we need to enforce this during creation, updates and deletion

    private Thingifier todoManager;

    @BeforeEach
    public void createDefinitions(){

        todoManager = TodoManagerModel.definedAsThingifier();

    }

    // by default relationships are optional optional
    @Test
    public void byDefaultRelationshipsAreOptional(){


        Thing projects = todoManager.getThingNamed("project");

        ThingInstance aProject = projects.createManagedInstance().setValue("title", "myproject");

        Assertions.assertTrue(aProject.validate().isValid());


        Thing todos = todoManager.getThingNamed("todo");

        ThingInstance tidy = todos.createManagedInstance().
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        Assertions.assertTrue(tidy.validate().isValid());


    }

    @Test
    public void anEstimateWithoutATodoIsInvalid(){

        Thing todos = todoManager.getThingNamed("todo");

        ThingInstance tidy = todos.createManagedInstance().
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        Thing estimates = todoManager.getThingNamed("estimate");

        ThingInstance tidyRoomEstimate = estimates.createManagedInstance().
                                        setValue("duration", "1");

        // it should be invalid because the estimate does not have a relationship with a to do
        Assertions.assertFalse(tidyRoomEstimate.validate().isValid());

    }

    @Test
    public void anEstimateMustHaveATodoToBeValid(){

        Thing todos = todoManager.getThingNamed("todo");

        ThingInstance tidy = todos.createManagedInstance().
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        Thing estimates = todoManager.getThingNamed("estimate");

        ThingInstance tidyRoomEstimate = estimates.createManagedInstance().
                setValue("duration", "1");

        tidyRoomEstimate.getRelationships().connect("estimate", tidy);

        // it should be valid because the estimate has a relationship with a to do
        Assertions.assertTrue(tidyRoomEstimate.validate().isValid());

        final Collection<ThingInstance> relatedEstimates = tidy.getRelationships().getConnectedItems("estimate");
        Assertions.assertEquals(1, relatedEstimates.size());

    }



    // deleting a thing which is related to another thing where the relationship is mandatory should delete the other thing
    @Test
    public void deleteAlsoCoversMandatoryOptionalityRelationships(){

        Thing todos = todoManager.getThingNamed("todo");

        ThingInstance tidy = todos.createManagedInstance().
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");


        Thing estimates = todoManager.getThingNamed("estimate");

        ThingInstance tidyRoomEstimate = estimates.createManagedInstance().
                setValue("duration", "1");

        tidyRoomEstimate.getRelationships().connect("estimate", tidy);

        // it should be valid because the estimate has a relationship with a to do
        Assertions.assertTrue(tidyRoomEstimate.validate().isValid());

        final Collection<ThingInstance> relatedEstimates = tidy.getRelationships().getConnectedItems("estimates");
        Assertions.assertEquals(1, relatedEstimates.size());
        Assertions.assertEquals(1, estimates.getInstances().size());
        Assertions.assertEquals(1, todos.getInstances().size());

        // now delete the to do, and the estimate should also be deleted

        todoManager.deleteThing(tidy);

        // the thingifier.deleteThing should be used instead of the Thing.deleteInstance because
        // things only know about themselves and their instances, but the thingifier knows about
        // all things and so can delete related items as well

        Assertions.assertEquals(0, todos.getInstances().size());
        Assertions.assertEquals(0, estimates.getInstances().size());
    }




}
