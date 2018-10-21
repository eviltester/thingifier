package uk.co.compendiumdev.casestudy.todomanager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

public class OptionalityRelationshipTest {

    // relationships can be Optional:Optional
    // or Mandatory:Optional
    // we need to enforce this during creation, updates and deletion

    private Thingifier todoManager;

    @Before
    public void createDefinitions(){

        todoManager = TodoManagerModel.definedAsThingifier();

    }

    // by default relationships are optional optional
    @Test
    public void byDefaultRelationshipsAreOptional(){


        Thing projects = todoManager.getThingNamed("project");

        ThingInstance aProject = projects.createInstance().setValue("title", "myproject");

        Assert.assertTrue(aProject.validate().isValid());


        Thing todos = todoManager.getThingNamed("todo");

        ThingInstance tidy = todos.createInstance().
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        Assert.assertTrue(tidy.validate().isValid());


    }

    @Test
    public void anEstimateWithoutATodoIsInvalid(){

        Thing todos = todoManager.getThingNamed("todo");

        ThingInstance tidy = todos.createInstance().
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        Thing estimates = todoManager.getThingNamed("estimate");

        ThingInstance tidyRoomEstimate = estimates.createInstance().
                                        setValue("duration", "1");

        // it should be invalid because the estimate does not have a relationship with a todo
        Assert.assertFalse(tidyRoomEstimate.validate().isValid());

    }

    @Test
    public void anEstimateMustHaveATodoToBeValid(){

        Thing todos = todoManager.getThingNamed("todo");

        ThingInstance tidy = todos.createInstance().
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        Thing estimates = todoManager.getThingNamed("estimate");

        ThingInstance tidyRoomEstimate = estimates.createInstance().
                setValue("duration", "1");

        tidyRoomEstimate.connects("estimate", tidy);

        // it should be valid because the estimate has a relationship with a todo
        Assert.assertTrue(tidyRoomEstimate.validate().isValid());

    }

    // TODO deleting a thing which is related to another thing where the relationship is mandatory should delete the other thing
    // TODO rest api needs to enforce optionality during creation

}
