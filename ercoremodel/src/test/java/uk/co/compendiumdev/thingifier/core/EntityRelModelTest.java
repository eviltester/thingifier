package uk.co.compendiumdev.thingifier.core;

import java.util.ArrayList;
import java.util.List;
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
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStoreProvider;

public class EntityRelModelTest {

    @Test
    public void canCreateAnEntityRelModel() {
        EntityRelModel erm = new EntityRelModel();

        Assertions.assertFalse(erm.hasEntityNamed("bob"));
        Assertions.assertNull(
                erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByGuid("bob"));
        Assertions.assertFalse(erm.hasEntityWithPluralNamed("bob"));
        Assertions.assertNull(erm.getSchema().getEntityDefinitionWithPluralNamed("bob"));
        Assertions.assertNull(erm.getSchema().getDefinitionWithSingularOrPluralNamed("bob"));
        Assertions.assertEquals(0, erm.getEntityNames().size());
    }

    @Test
    public void nothingHappensWhenTryToDeleteThingThatDoesNotExist() {
        EntityRelModel erm = new EntityRelModel();

        EntityInstance missing =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(
                                EntityInstanceDraft.forEntity(new EntityDefinition("no", "nos")));

        Assertions.assertDoesNotThrow(
                () ->
                        erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                                .entities()
                                .delete(missing));
    }

    @Test
    public void canCreateAThingInAModel() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing = erm.createEntityDefinition("thing", "things");

        Assertions.assertTrue(erm.hasEntityNamed("thing"));
        Assertions.assertTrue(erm.hasEntityWithPluralNamed("things"));
        Assertions.assertEquals(
                thing, erm.getSchema().getEntityDefinitionWithPluralNamed("things"));
        Assertions.assertEquals(
                thing, erm.getSchema().getDefinitionWithSingularOrPluralNamed("thing"));
        Assertions.assertEquals(
                thing, erm.getSchema().getDefinitionWithSingularOrPluralNamed("things"));
        Assertions.assertEquals(1, erm.getEntityNames().size());
        Assertions.assertTrue(erm.getEntityNames().contains("thing"));
        Assertions.assertEquals(
                0, erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME).entityQueries().count(thing));
    }

    @Test
    public void repositoryCanAddExplicitAutoIncrementValueWhenFieldAddedAfterCollectionCreated() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition defn = erm.createEntityDefinition("challenge", "challenges");
        defn.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));

        ThingStore repository = erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance explicitId =
                repository
                        .entities()
                        .create(EntityInstanceDraft.forEntity(defn).withProtectedField("id", "12"));

        EntityInstance nextId = repository.entities().create(EntityInstanceDraft.forEntity(defn));

        Assertions.assertEquals("12", explicitId.getPrimaryKeyValue());
        Assertions.assertEquals("13", nextId.getPrimaryKeyValue());
    }

    @Test
    public void canFindAThingInAModel() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition defn = erm.createEntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        ThingStore repository = erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance instance = repository.entities().create(EntityInstanceDraft.forEntity(defn));

        String thingGuid = instance.getPrimaryKeyValue();

        Assertions.assertEquals(instance, repository.entityQueries().findByGuid(thingGuid));
    }

    @Test
    public void canDeleteAThingInAModel() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition defn = erm.createEntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        ThingStore repository = erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance instance = repository.entities().create(EntityInstanceDraft.forEntity(defn));
        String thingGuid = instance.getPrimaryKeyValue();

        repository.entities().delete(instance);

        Assertions.assertNull(repository.entityQueries().findByGuid(thingGuid));
        Assertions.assertEquals(0, repository.entityQueries().count(defn));
    }

    @Test
    public void canClearAllDataInAModel() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing = erm.createEntityDefinition("thing", "things");
        EntityDefinition thing2 = erm.createEntityDefinition("thing2", "thing2");
        ThingStore repository = erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);

        repository.entities().create(EntityInstanceDraft.forEntity(thing));
        repository.entities().create(EntityInstanceDraft.forEntity(thing));
        repository.entities().create(EntityInstanceDraft.forEntity(thing2));
        repository.entities().create(EntityInstanceDraft.forEntity(thing2));
        repository.entities().create(EntityInstanceDraft.forEntity(thing2));

        Assertions.assertEquals(2, repository.entityQueries().count(thing));
        Assertions.assertEquals(3, repository.entityQueries().count(thing2));

        repository.administration().clearAllData();

        Assertions.assertEquals(0, repository.entityQueries().count(thing));
        Assertions.assertEquals(0, repository.entityQueries().count(thing2));
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
    public void canCreateRelationshipsWithTheSameNameFromDifferentSources() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition robotModel = erm.createEntityDefinition("robotmodel", "robotmodels");
        EntityDefinition zone = erm.createEntityDefinition("zone", "zones");
        EntityDefinition robot = erm.createEntityDefinition("robot", "robots");

        erm.createRelationshipDefinition(robotModel, robot, "robots", Cardinality.ONE_TO_MANY());
        erm.createRelationshipDefinition(zone, robot, "robots", new Cardinality(1, 24));

        Assertions.assertEquals(2, erm.getRelationshipDefinitions().size());
        Assertions.assertTrue(erm.hasRelationshipNamed("robots"));
        Assertions.assertEquals(
                1,
                erm.getRelationshipDefinitions().stream()
                        .filter(
                                relationship ->
                                        relationship.getFromRelationship().getFrom() == robotModel)
                        .count());
        Assertions.assertEquals(
                1,
                erm.getRelationshipDefinitions().stream()
                        .filter(
                                relationship ->
                                        relationship.getFromRelationship().getFrom() == zone)
                        .count());
    }

    @Test
    public void canFindAReversedRelationship() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing = erm.createEntityDefinition("thing", "things");
        EntityDefinition dependant = erm.createEntityDefinition("dependantthing", "dthings");

        erm.createRelationshipDefinition(thing, dependant, "things", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "idiewithoutyou")
                .getReversedRelationship()
                .setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        Assertions.assertTrue(erm.hasRelationshipNamed("idiewithoutyou"));
    }

    @Test
    public void canDeleteAThingWithRelationships() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thing = erm.createEntityDefinition("thing", "things");
        thing.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        EntityDefinition dependant = erm.createEntityDefinition("dependantthing", "dthings");

        erm.createRelationshipDefinition(thing, dependant, "things", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "idiewithoutyou")
                .getReversedRelationship()
                .setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        ThingStore repository = erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance mainThing =
                repository.entities().create(EntityInstanceDraft.forEntity(thing));
        EntityInstance first =
                repository.entities().create(EntityInstanceDraft.forEntity(dependant));
        EntityInstance second =
                repository.entities().create(EntityInstanceDraft.forEntity(dependant));
        EntityInstance third =
                repository.entities().create(EntityInstanceDraft.forEntity(dependant));

        repository.relationships().connect(mainThing, "things", first);
        repository.relationships().connect(mainThing, "things", second);
        repository.relationships().connect(mainThing, "things", third);

        Assertions.assertEquals(
                3, repository.relationships().listRelated(mainThing, "things").size());
        Assertions.assertEquals(3, repository.entityQueries().count(dependant));

        String thingGuid = mainThing.getPrimaryKeyValue();
        repository.entities().delete(mainThing);

        Assertions.assertNull(repository.entityQueries().findByGuid(thingGuid));
        Assertions.assertEquals(0, repository.entityQueries().count(thing));
        Assertions.assertEquals(0, repository.entityQueries().count(dependant));
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

        RepositoryDataPopulator dataPopulator =
                (ERSchema schema, ThingStore repository) ->
                        repository
                                .entities()
                                .create(
                                        EntityInstanceDraft.forEntity(
                                                schema.getEntityDefinitionNamed("thing")));

        erm.setDataGenerator(dataPopulator);

        Assertions.assertTrue(erm.populateDatabase(EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertEquals(
                1, erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME).entityQueries().count(thing));
    }

    @Test
    public void populateDatabaseUsesRepositoryDataPopulator() {
        try (EntityRelModel erm = new EntityRelModel(SqliteThingStoreProvider.inMemory())) {
            EntityDefinition thing = erm.createEntityDefinition("thing", "things");
            thing.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            thing.addField(Field.is("name", FieldType.STRING));

            RepositoryDataPopulator dataPopulator =
                    (ERSchema schema, ThingStore repository) ->
                            repository
                                    .entities()
                                    .create(
                                            EntityInstanceDraft.forEntity(
                                                            schema.getEntityDefinitionNamed(
                                                                    "thing"))
                                                    .withField("name", "repository"));

            erm.setDataGenerator(dataPopulator);

            Assertions.assertTrue(erm.populateDatabase(EntityRelModel.DEFAULT_DATABASE_NAME));

            ThingStore repository = erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            List<EntityInstance> instances =
                    new ArrayList<>(repository.entityQueries().list(thing));

            Assertions.assertEquals(1, instances.size());
            Assertions.assertEquals(
                    "repository", instances.get(0).getFieldValue("name").asString());
        }
    }
}
