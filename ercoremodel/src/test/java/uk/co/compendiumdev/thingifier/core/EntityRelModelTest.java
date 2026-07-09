package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

import java.util.ArrayList;
import java.util.List;

public class EntityRelModelTest {

    @Test
    public void canCreateAnEntityRelModel() {
        EntityRelModel erm = new EntityRelModel();

        Assertions.assertFalse(erm.hasEntityNamed("bob"));
        Assertions.assertNull(erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).
                findEntityInstanceByGUID("bob"));
        Assertions.assertFalse(erm.hasEntityWithPluralNamed("bob"));
        Assertions.assertNull(erm.getSchema().getEntityDefinitionWithPluralNamed("bob"));
        Assertions.assertNull(erm.getSchema().getDefinitionWithSingularOrPluralNamed("bob"));
        Assertions.assertEquals(0, erm.getEntityNames().size());
    }

    @Test
    public void nothingHappensWhenTryToDeleteThingThatDoesNotExist() {
        EntityRelModel erm = new EntityRelModel();

        EntityInstance missing = EntityInstance.fromDraft(EntityInstanceDraft.forEntity(
                new EntityDefinition("no", "nos")));

        Assertions.assertDoesNotThrow(() ->
                erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).deleteEntityInstance(missing));
    }

    @Test
    public void canCreateAThingInAModel() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing = erm.createEntityDefinition("thing", "things");

        Assertions.assertTrue(erm.hasEntityNamed("thing"));
        Assertions.assertTrue(erm.hasEntityWithPluralNamed("things"));
        Assertions.assertEquals(thing, erm.getSchema().getEntityDefinitionWithPluralNamed("things"));
        Assertions.assertEquals(thing, erm.getSchema().getDefinitionWithSingularOrPluralNamed("thing"));
        Assertions.assertEquals(thing, erm.getSchema().getDefinitionWithSingularOrPluralNamed("things"));
        Assertions.assertEquals(1, erm.getEntityNames().size());
        Assertions.assertTrue(erm.getEntityNames().contains("thing"));
        Assertions.assertEquals(0,
                erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).countInstances(thing));
    }

    @Test
    public void repositoryCanAddExplicitAutoIncrementValueWhenFieldAddedAfterCollectionCreated() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition defn = erm.createEntityDefinition("challenge", "challenges");
        defn.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));

        ThingRepository repository = erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance explicitId = repository.createInstance(
                EntityInstanceDraft.forEntity(defn).withProtectedField("id", "12"));

        EntityInstance nextId = repository.createInstance(EntityInstanceDraft.forEntity(defn));

        Assertions.assertEquals("12", explicitId.getPrimaryKeyValue());
        Assertions.assertEquals("13", nextId.getPrimaryKeyValue());
    }

    @Test
    public void canFindAThingInAModel() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition defn = erm.createEntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        ThingRepository repository = erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance instance = repository.createInstance(EntityInstanceDraft.forEntity(defn));

        String thingGuid = instance.getPrimaryKeyValue();

        Assertions.assertEquals(instance, repository.findEntityInstanceByGUID(thingGuid));
    }

    @Test
    public void canDeleteAThingInAModel() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition defn = erm.createEntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        ThingRepository repository = erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance instance = repository.createInstance(EntityInstanceDraft.forEntity(defn));
        String thingGuid = instance.getPrimaryKeyValue();

        repository.deleteEntityInstance(instance);

        Assertions.assertNull(repository.findEntityInstanceByGUID(thingGuid));
        Assertions.assertEquals(0, repository.countInstances(defn));
    }

    @Test
    public void canClearAllDataInAModel() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing = erm.createEntityDefinition("thing", "things");
        EntityDefinition thing2 = erm.createEntityDefinition("thing2", "thing2");
        ThingRepository repository = erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

        repository.createInstance(EntityInstanceDraft.forEntity(thing));
        repository.createInstance(EntityInstanceDraft.forEntity(thing));
        repository.createInstance(EntityInstanceDraft.forEntity(thing2));
        repository.createInstance(EntityInstanceDraft.forEntity(thing2));
        repository.createInstance(EntityInstanceDraft.forEntity(thing2));

        Assertions.assertEquals(2, repository.countInstances(thing));
        Assertions.assertEquals(3, repository.countInstances(thing2));

        repository.clearAllData();

        Assertions.assertEquals(0, repository.countInstances(thing));
        Assertions.assertEquals(0, repository.countInstances(thing2));
    }

    @Test
    public void canCreateWithNoRelationships() {
        EntityRelModel erm = new EntityRelModel();

        Assertions.assertNotNull(erm.getRelationshipDefinitions());
        Assertions.assertEquals(0, erm.getRelationshipDefinitions().size());
        Assertions.assertFalse(erm.hasRelationshipNamed("bob"));
    }

    @Test
    public void canCreateRelationships() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing1 = erm.createEntityDefinition("thing1", "thing1");
        EntityDefinition thing2 = erm.createEntityDefinition("thing2", "thing2");

        erm.createRelationshipDefinition(thing1, thing2, "things", Cardinality.ONE_TO_MANY());

        Assertions.assertEquals(1, erm.getRelationshipDefinitions().size());
        Assertions.assertTrue(erm.hasRelationshipNamed("things"));
    }

    @Test
    public void canFindAReversedRelationship() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing = erm.createEntityDefinition("thing", "things");
        EntityDefinition dependant = erm.createEntityDefinition("dependantthing", "dthings");

        erm.createRelationshipDefinition(thing, dependant, "things", Cardinality.ONE_TO_MANY()).
                whenReversed(Cardinality.ONE_TO_ONE(), "idiewithoutyou").
                getReversedRelationship().
                setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        Assertions.assertTrue(erm.hasRelationshipNamed("idiewithoutyou"));
    }

    @Test
    public void canDeleteAThingWithRelationships() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing = erm.createEntityDefinition("thing", "things");
        thing.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        EntityDefinition dependant = erm.createEntityDefinition("dependantthing", "dthings");

        erm.createRelationshipDefinition(thing, dependant, "things", Cardinality.ONE_TO_MANY()).
                whenReversed(Cardinality.ONE_TO_ONE(), "idiewithoutyou").
                getReversedRelationship().
                setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        ThingRepository repository = erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance mainThing = repository.createInstance(EntityInstanceDraft.forEntity(thing));
        EntityInstance first = repository.createInstance(EntityInstanceDraft.forEntity(dependant));
        EntityInstance second = repository.createInstance(EntityInstanceDraft.forEntity(dependant));
        EntityInstance third = repository.createInstance(EntityInstanceDraft.forEntity(dependant));

        repository.connectRelationship(mainThing, "things", first);
        repository.connectRelationship(mainThing, "things", second);
        repository.connectRelationship(mainThing, "things", third);

        Assertions.assertEquals(3, repository.getConnectedItems(mainThing, "things").size());
        Assertions.assertEquals(3, repository.countInstances(dependant));

        String thingGuid = mainThing.getPrimaryKeyValue();
        repository.deleteEntityInstance(mainThing);

        Assertions.assertNull(repository.findEntityInstanceByGUID(thingGuid));
        Assertions.assertEquals(0, repository.countInstances(thing));
        Assertions.assertEquals(0, repository.countInstances(dependant));
    }

    @Test
    public void canFunctionWithoutADataGenerator() {
        EntityRelModel erm = new EntityRelModel();

        Assertions.assertEquals(0, erm.getEntityNames().size());
    }

    @Test
    public void canSetAndUseARepositoryDataGenerator() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing = erm.createEntityDefinition("thing", "things");

        RepositoryDataPopulator dataPopulator = (ERSchema schema, ThingRepository repository) ->
                repository.createInstance(
                        EntityInstanceDraft.forEntity(schema.getEntityDefinitionNamed("thing")));

        erm.setDataGenerator(dataPopulator);

        Assertions.assertTrue(erm.populateDatabase(EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertEquals(1,
                erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).countInstances(thing));
    }

    @Test
    public void populateDatabaseUsesRepositoryDataPopulator() {
        try (EntityRelModel erm = new EntityRelModel(SqliteThingRepositoryProvider.inMemory())) {
            EntityDefinition thing = erm.createEntityDefinition("thing", "things");
            thing.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            thing.addField(Field.is("name", FieldType.STRING));

            RepositoryDataPopulator dataPopulator = (ERSchema schema, ThingRepository repository) ->
                    repository.createInstance(
                            EntityInstanceDraft.forEntity(schema.getEntityDefinitionNamed("thing")).
                                    withField("name", "repository"));

            erm.setDataGenerator(dataPopulator);

            Assertions.assertTrue(erm.populateDatabase(EntityRelModel.DEFAULT_DATABASE_NAME));

            ThingRepository repository = erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            List<EntityInstance> instances = new ArrayList<>(repository.listInstances(thing));

            Assertions.assertEquals(1, instances.size());
            Assertions.assertEquals("repository", instances.get(0).getFieldValue("name").asString());
        }
    }
}
