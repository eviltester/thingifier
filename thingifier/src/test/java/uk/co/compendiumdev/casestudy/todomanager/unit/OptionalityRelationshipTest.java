package uk.co.compendiumdev.casestudy.todomanager.unit;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class OptionalityRelationshipTest {

    // relationships can be Optional:Optional
    // or Mandatory:Optional
    // we need to enforce this during creation, updates and deletion

    private Thingifier todoManager;
    private ThingStore repository;

    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();
        repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    // by default relationships are optional optional
    @Test
    public void byDefaultRelationshipsAreOptional() {

        EntityDefinition projects =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("project");

        EntityInstance aProject =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(projects)
                                        .withField("title", "myproject"));

        Assertions.assertTrue(aProject.validate().isValid());

        EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        EntityInstance tidy =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todos)
                                        .withField("title", "Tidy up my room")
                                        .withField(
                                                "description",
                                                "I need to tidy up my room because it is a mess"));
        Assertions.assertEquals("Tidy up my room", tidy.getFieldValue("title").asString());

        Assertions.assertTrue(tidy.validateFieldValues(new ArrayList<>(), true).isValid());
    }

    @Test
    public void anEstimateWithoutATodoIsInvalid() {

        EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        repository
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(todos)
                                .withField("title", "Tidy up my room")
                                .withField(
                                        "description",
                                        "I need to tidy up my room because it is a mess"));

        EntityDefinition estimates =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("estimate");

        EntityInstance tidyRoomEstimate =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(estimates)
                                        .withField("duration", "1"));

        // it should be invalid because the estimate does not have a relationship with a to do
        Assertions.assertFalse(repository.relationships().validate(tidyRoomEstimate).isValid());
    }

    @Test
    public void anEstimateMustHaveATodoToBeValid() {

        EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        EntityInstance tidy =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todos)
                                        .withField("title", "Tidy up my room")
                                        .withField(
                                                "description",
                                                "I need to tidy up my room because it is a mess"));

        EntityDefinition estimates =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("estimate");

        EntityInstance tidyRoomEstimate =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(estimates)
                                        .withField("duration", "1"));

        repository.relationships().connect(tidyRoomEstimate, "estimate", tidy);

        // it should be valid because the estimate has a relationship with a to do
        Assertions.assertTrue(repository.relationships().validate(tidyRoomEstimate).isValid());

        final Collection<EntityInstance> relatedEstimates =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(tidy, "estimate");
        Assertions.assertEquals(1, relatedEstimates.size());
    }

    // deleting a thing which is related to another thing where the relationship is mandatory should
    // delete the other thing
    @Test
    public void deleteAlsoCoversMandatoryOptionalityRelationships() {

        EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        EntityInstance tidy =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todos)
                                        .withField("title", "Tidy up my room")
                                        .withField(
                                                "description",
                                                "I need to tidy up my room because it is a mess"));

        EntityDefinition estimates =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("estimate");

        EntityInstance tidyRoomEstimate =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(estimates)
                                        .withField("duration", "1"));

        repository.relationships().connect(tidyRoomEstimate, "estimate", tidy);

        // it should be valid because the estimate has a relationship with a to do
        Assertions.assertTrue(repository.relationships().validate(tidyRoomEstimate).isValid());

        final Collection<EntityInstance> relatedEstimates =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .relationships()
                        .listRelated(tidy, "estimates");
        Assertions.assertEquals(1, relatedEstimates.size());
        Assertions.assertEquals(1, repository.entityQueries().list(estimates).size());
        Assertions.assertEquals(1, repository.entityQueries().list(todos).size());

        // now delete the to do, and the estimate should also be deleted

        todoManager.deleteThing(tidy, EntityRelModel.DEFAULT_DATABASE_NAME);

        // the thingifier.deleteThing should be used instead of the Thing.deleteInstance because
        // things only know about themselves and their instances, but the thingifier knows about
        // all things and so can delete related items as well

        Assertions.assertEquals(0, repository.entityQueries().list(todos).size());
        Assertions.assertEquals(0, repository.entityQueries().list(estimates).size());
    }
}
