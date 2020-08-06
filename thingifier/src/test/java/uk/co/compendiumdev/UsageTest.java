package uk.co.compendiumdev;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

import java.util.Collection;

import static uk.co.compendiumdev.thingifier.domain.FieldType.INTEGER;
import static uk.co.compendiumdev.thingifier.domain.FieldType.STRING;


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
    public void usageExample(){


        Thing person = Thing.
                        create("person", "people");

        person.definition().
                        addFields(Field.is("name"), Field.is("age", INTEGER));

        ThingInstance bob = person.createInstance().
                                setValue("name","Bob");
        person.addInstance(bob);

        bob.setValue("age", "56");

        ThingInstance eris = person.createInstance().
                setValue("name","Eris").setValue("age", "1000");

        person.addInstance(eris);

        Assertions.assertEquals(2, person.countInstances());
        Assertions.assertEquals("Bob", bob.getFieldValue("name").asString());
        Assertions.assertEquals("56", bob.getFieldValue("age").asString());
        Assertions.assertEquals("1000", person.findInstanceByField(FieldValue.is("name", "Eris")).getFieldValue("age").asString());

    }

    @Test
    public void moreUsageExamples(){

        Thing url = Thing.create("URL", "URLs");

        url.definition().addFields(Field.is("url"),
                                 Field.is("visited", INTEGER), Field.is("name",STRING));

        Assertions.assertTrue(url.definition().hasFieldNameDefined("url"));
        Assertions.assertTrue(url.definition().hasFieldNameDefined("name"));
        Assertions.assertTrue(url.definition().hasFieldNameDefined("visited"));


        url.addInstance(
                url.createInstance().
                setValue("name","EvilTester.com").setValue("url", "http://eviltester.com")
        );

        url.addInstance(
                url.createInstance().
                setValue("name","JavaForTesters.com").setValue("url", "http://javaForTesters.com")
        );

        Collection<ThingInstance> instances = url.getInstances();

        System.out.println("NAME\tURL");
        System.out.println("==========");

        for(ThingInstance aURL : instances){
            System.out.println(String.format("%s\t%s", aURL.getFieldValue("name").asString(), aURL.getFieldValue("url").asString()));
        }

        Assertions.assertEquals(2, instances.size());

    }



    @Test
    public void thingifierCanManageThings(){

        Thingifier things = new Thingifier();

        things.createThing("URL", "URLs").definition().
                    addFields(Field.is("url"),Field.is("name",STRING)
                    );

        Thing urls = things.getThingNamed("URL");

        Assertions.assertTrue(urls.definition().hasFieldNameDefined("url"));
        Assertions.assertTrue(urls.definition().hasFieldNameDefined("name"));

        ThingInstance evilTester_dot_com = urls.createInstance().
                setValue("name","EvilTester.com").setValue("url","http://eviltester.com");

        urls.addInstance(evilTester_dot_com);

        Thing user = things.createThing("USER", "users");

        user.definition().addFields(Field.is("name"));

        ThingInstance alan = user.createInstance().
                setValue("name","alan");

        user.addInstance(alan);


        // TODO fix relationshps so that they have values
        //RelationshipDefinition relationship = things.defineRelationshipBetween("USER", "URL", AndCall.it("visited"));
        //relationship.representedAsThing("visit").definition().addFields(Field.is("dateOfVisit", DATE));

        // TODO: would prefer FieldValue.is("dateOfVisit", "2015 10 04 15:45")
        //things.createRelationship(alan, "visited", evilTester_dot_com, "dateOfVisit:2015 10 04 15:45");

        System.out.println(things);

    }


    @Test
    public void todoModelUsageExamples(){

        // Start simple with a to do manager model e.g. to do items, context, project (can also be a sub-project), task group

        Thing todo = Thing.create("ToDo", "ToDos");

        todo.definition()
                .addFields(Field.is("title", STRING), Field.is("description",STRING),
                        Field.is("doneStatus",FieldType.BOOLEAN).withDefaultValue("FALSE"));

        Assertions.assertTrue(todo.definition().hasFieldNameDefined("title"));
        Assertions.assertTrue(todo.definition().hasFieldNameDefined("description"));
        Assertions.assertTrue(todo.definition().hasFieldNameDefined("doneStatus"));

        Assertions.assertEquals("FALSE", todo.definition().
                                                    getField("doneStatus").
                                                    getDefaultValue().asString());


    }


}


