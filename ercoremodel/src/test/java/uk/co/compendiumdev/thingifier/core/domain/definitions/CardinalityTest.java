package uk.co.compendiumdev.thingifier.core.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

class CardinalityTest {

    @Test
    void canGetLeftAndRightValues() {
        Assertions.assertEquals("1", Cardinality.ONE_TO_MANY().left());
        Assertions.assertEquals("*", Cardinality.ONE_TO_MANY().right());
    }

    @Test
    void cannotAddRelationshipExceedCardinality() {

        final EntityRelModel model = new EntityRelModel();

        final EntityDefinition thing1 = model.createEntityDefinition("thing1", "thing1");
        final EntityDefinition thing2 = model.createEntityDefinition("thing2", "thing2");

        model.createRelationshipDefinition(thing1, thing2, "bob", new Cardinality(0, 2));
        ThingRepository repository = model.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

        final EntityInstance instance1 =
                repository.createInstance(EntityInstanceDraft.forEntity(thing1));
        final EntityInstance instance2 =
                repository.createInstance(EntityInstanceDraft.forEntity(thing2));
        final EntityInstance instance3 =
                repository.createInstance(EntityInstanceDraft.forEntity(thing2));
        final EntityInstance instance4 =
                repository.createInstance(EntityInstanceDraft.forEntity(thing2));

        repository.connectRelationship(instance1, "bob", instance2);
        repository.connectRelationship(instance1, "bob", instance3);

        Assertions.assertEquals(true, instance1.validate().isValid());

        // this should fail
        boolean failed = false;
        try {
            repository.connectRelationship(instance1, "bob", instance4);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            failed = true;
        }

        Assertions.assertTrue(failed);
    }
}
