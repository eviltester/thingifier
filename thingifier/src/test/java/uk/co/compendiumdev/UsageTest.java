package uk.co.compendiumdev;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class UsageTest {

    // done: create thingifier  (things Map<thingName, Thing>
    // done: define relationships between things
    // done: create relationships between things
    // done: named directional relationships
    // done: delete thing instances
    // done: show relationships in the instances e.g. for a to do show "relationships" : [ "task-of"
    // : [{"guid":"xxx"}]]
    // done: find things based on field values e.g. ?status=true

    // todo: create test coverage for creating relationships through post 'in' the instance
    // todo: cardinality of relationships
    // todo: test for optional fields (they are optional by default)
    // todo: randomly generate data against regex
    // todo: delete definitions - and all things

    @Test
    public void thingifierCanManageThings() {

        Thingifier things = new Thingifier();

        things.defineThing("URL", "URLs")
                .addFields(Field.is("url", STRING), Field.is("name", STRING));

        EntityDefinition urls =
                things.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("URL");
        ThingRepository repository = things.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

        Assertions.assertTrue(urls.hasFieldNameDefined("url"));
        Assertions.assertTrue(urls.hasFieldNameDefined("name"));

        repository.createInstance(
                EntityInstanceDraft.forEntity(urls)
                        .withField("name", "EvilTester.com")
                        .withField("url", "http://eviltester.com"));

        EntityDefinition user = things.defineThing("USER", "users");

        user.addFields(Field.is("name", STRING));

        EntityDefinition users =
                things.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("USER");
        EntityInstance alan =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(users).withField("name", "alan"));
        Assertions.assertEquals("alan", alan.getFieldValue("name").asString());

        // TODO fix relationshps so that they have values
        // RelationshipDefinition relationship = things.defineRelationshipBetween("USER", "URL",
        // AndCall.it("visited"));
        // relationship.representedAsThing("visit").definition().addFields(Field.is("dateOfVisit",
        // DATE));

        // TODO: would prefer FieldValue.is("dateOfVisit", "2015 10 04 15:45")
        // things.createRelationship(alan, "visited", evilTester_dot_com, "dateOfVisit:2015 10 04
        // 15:45");

        System.out.println(things);
    }
}
