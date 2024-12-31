package uk.co.compendiumdev;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;


public class UsageTest {



    // done: create thingifier  (things Map<thingName, Thing>
    // done: define relationships between things
    // done: create relationships between things
    // done: named directional relationships
    // done: delete thing instances
    // done: show relationships in the instances e.g. for a to do show "relationships" : [ "task-of" : [{"guid":"xxx"}]]
    // done: find things based on field values e.g. ?status=true

    // todo: create test coverage for creating relationships through post 'in' the instance
    // todo: cardinality of relationships
    // todo: test for optional fields (they are optional by default)
    // todo: randomly generate data against regex
    // todo: delete definitions - and all things





    @Test
    public void thingifierCanManageThings(){

        Thingifier things = new Thingifier();

        things.defineThing("URL", "URLs").
                    addFields(Field.is("url", STRING),Field.is("name",STRING)
                    );

        EntityInstanceCollection urls = things.getThingInstancesNamed("URL", EntityRelModel.DEFAULT_DATABASE_NAME);

        Assertions.assertTrue(urls.definition().hasFieldNameDefined("url"));
        Assertions.assertTrue(urls.definition().hasFieldNameDefined("name"));

        urls.addInstance(new EntityInstance(urls.definition())).
                setValue("name","EvilTester.com").
                setValue("url","http://eviltester.com");

        EntityDefinition user = things.defineThing("USER", "users");

        user.addFields(Field.is("name", STRING));

        EntityInstanceCollection entityInstanceCollection = things.getThingInstancesNamed("USER", EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance alan = entityInstanceCollection.addInstance(new EntityInstance(entityInstanceCollection.definition())).
                setValue("name","alan");


        // TODO fix relationshps so that they have values
        //RelationshipDefinition relationship = things.defineRelationshipBetween("USER", "URL", AndCall.it("visited"));
        //relationship.representedAsThing("visit").definition().addFields(Field.is("dateOfVisit", DATE));

        // TODO: would prefer FieldValue.is("dateOfVisit", "2015 10 04 15:45")
        //things.createRelationship(alan, "visited", evilTester_dot_com, "dateOfVisit:2015 10 04 15:45");

        System.out.println(things);

    }





}


