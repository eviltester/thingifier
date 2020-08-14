package uk.co.compendiumdev;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.Collection;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.INTEGER;
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

        things.createThing("URL", "URLs").definition().
                    addFields(Field.is("url"),Field.is("name",STRING)
                    );

        Thing urls = things.getThingNamed("URL");

        Assertions.assertTrue(urls.definition().hasFieldNameDefined("url"));
        Assertions.assertTrue(urls.definition().hasFieldNameDefined("name"));

        urls.createManagedInstance().
                setValue("name","EvilTester.com").
                setValue("url","http://eviltester.com");

        Thing user = things.createThing("USER", "users");

        user.definition().addFields(Field.is("name"));

        ThingInstance alan = user.createManagedInstance().
                setValue("name","alan");


        // TODO fix relationshps so that they have values
        //RelationshipDefinition relationship = things.defineRelationshipBetween("USER", "URL", AndCall.it("visited"));
        //relationship.representedAsThing("visit").definition().addFields(Field.is("dateOfVisit", DATE));

        // TODO: would prefer FieldValue.is("dateOfVisit", "2015 10 04 15:45")
        //things.createRelationship(alan, "visited", evilTester_dot_com, "dateOfVisit:2015 10 04 15:45");

        System.out.println(things);

    }





}


