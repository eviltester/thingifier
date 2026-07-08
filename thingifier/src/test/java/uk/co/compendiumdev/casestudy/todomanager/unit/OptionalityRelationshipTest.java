package uk.co.compendiumdev.casestudy.todomanager.unit;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.testsupport.ThingifierRepositoryTestSupport;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.Collection;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
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


        EntityDefinition projects = ThingifierRepositoryTestSupport.entity(todoManager, "project");

        EntityInstance aProject = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(projects)).setValue("title", "myproject");

        Assertions.assertTrue(aProject.validate().isValid());


        EntityDefinition todos = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        EntityInstance tidy = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todos)).
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        Assertions.assertTrue(tidy.validateFieldValues(new ArrayList<>(), true).isValid());


    }

    @Test
    public void anEstimateWithoutATodoIsInvalid(){

        EntityDefinition todos = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        EntityInstance tidy = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todos)).
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        EntityDefinition estimates = ThingifierRepositoryTestSupport.entity(todoManager, "estimate");

        EntityInstance tidyRoomEstimate = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(estimates)).
                                        setValue("duration", "1");

        // it should be invalid because the estimate does not have a relationship with a to do
        Assertions.assertFalse(tidyRoomEstimate.validate().isValid());

    }

    @Test
    public void anEstimateMustHaveATodoToBeValid(){

        EntityDefinition todos = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        EntityInstance tidy = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todos)).
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");

        EntityDefinition estimates = ThingifierRepositoryTestSupport.entity(todoManager, "estimate");

        EntityInstance tidyRoomEstimate = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(estimates)).
                setValue("duration", "1");

        tidyRoomEstimate.getRelationships().connect("estimate", tidy);

        // it should be valid because the estimate has a relationship with a to do
        Assertions.assertTrue(tidyRoomEstimate.validate().isValid());

        final Collection<EntityInstance> relatedEstimates = tidy.getRelationships().getConnectedItems("estimate");
        Assertions.assertEquals(1, relatedEstimates.size());

    }



    // deleting a thing which is related to another thing where the relationship is mandatory should delete the other thing
    @Test
    public void deleteAlsoCoversMandatoryOptionalityRelationships(){

        EntityDefinition todos = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        EntityInstance tidy = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todos)).
                setValue("title", "Tidy up my room").
                setValue("description", "I need to tidy up my room because it is a mess");


        EntityDefinition estimates = ThingifierRepositoryTestSupport.entity(todoManager, "estimate");

        EntityInstance tidyRoomEstimate = ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(estimates)).
                setValue("duration", "1");

        tidyRoomEstimate.getRelationships().connect("estimate", tidy);

        // it should be valid because the estimate has a relationship with a to do
        Assertions.assertTrue(tidyRoomEstimate.validate().isValid());

        final Collection<EntityInstance> relatedEstimates = tidy.getRelationships().getConnectedItems("estimates");
        Assertions.assertEquals(1, relatedEstimates.size());
        Assertions.assertEquals(1, ThingifierRepositoryTestSupport.repository(todoManager).listInstances(estimates).size());
        Assertions.assertEquals(1, ThingifierRepositoryTestSupport.repository(todoManager).listInstances(todos).size());

        // now delete the to do, and the estimate should also be deleted

        todoManager.deleteThing(tidy, EntityRelModel.DEFAULT_DATABASE_NAME);

        // the thingifier.deleteThing should be used instead of the Thing.deleteInstance because
        // things only know about themselves and their instances, but the thingifier knows about
        // all things and so can delete related items as well

        Assertions.assertEquals(0, ThingifierRepositoryTestSupport.repository(todoManager).listInstances(todos).size());
        Assertions.assertEquals(0, ThingifierRepositoryTestSupport.repository(todoManager).listInstances(estimates).size());
    }




}
