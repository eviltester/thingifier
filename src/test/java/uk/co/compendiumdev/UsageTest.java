package uk.co.compendiumdev;


import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

import static uk.co.compendiumdev.thingifier.generic.FieldType.INTEGER;
import static uk.co.compendiumdev.thingifier.generic.FieldType.STRING;


public class UsageTest {



    // done: create thingifier  (things Map<thingName, Thing>
    // done: define relationships between things
    // done: create relationships between things
    // done: named directional relationships
    // done: delete thing instances

    // todo: show relationships in the instances e.g. for a todo show "relationships" : [ "task-of" : [{"guid":"xxx"}]]
    // todo: find things based on field values
    // todo: cardinality of relationships
    // todo: test for optional fields (they are optional by default)
    // todo: default field values
    // todo: regex for field validation
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

        Assert.assertEquals(2, person.countInstances());
        Assert.assertEquals("Bob", bob.getValue("name"));
        Assert.assertEquals("56", bob.getValue("age"));
        Assert.assertEquals("1000", person.findInstanceByField(FieldValue.is("name","Eris")).getValue("age"));

    }

    @Test
    public void moreUsageExamples(){

        Thing url = Thing.create("URL", "URLs");

        url.definition().addFields(Field.is("url"),
                                 Field.is("visited", INTEGER), Field.is("name",STRING));

        Assert.assertTrue(url.definition().hasFieldNameDefined("url"));
        Assert.assertTrue(url.definition().hasFieldNameDefined("name"));
        Assert.assertTrue(url.definition().hasFieldNameDefined("visited"));


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
            System.out.println(String.format("%s\t%s", aURL.getValue("name"), aURL.getValue("url")));
        }

        Assert.assertEquals(2, instances.size());

    }



    @Test
    public void thingifierCanManageThings(){

        Thingifier things = new Thingifier();

        things.createThing("URL", "URLs").definition().
                    addFields(Field.is("url"),Field.is("name",STRING)
                    );

        Thing urls = things.getThingNamed("URL");

        Assert.assertTrue(urls.definition().hasFieldNameDefined("url"));
        Assert.assertTrue(urls.definition().hasFieldNameDefined("name"));

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

        // Start simple with a todo manager model e.g. todo, context, project (can also be a sub-project), task group

        Thing todo = Thing.create("ToDo", "ToDos");

        todo.definition()
                .addFields(Field.is("title", STRING), Field.is("description",STRING),
                        Field.is("doneStatus",FieldType.BOOLEAN).withDefaultValue("FALSE"));

        Assert.assertTrue(todo.definition().hasFieldNameDefined("title"));
        Assert.assertTrue(todo.definition().hasFieldNameDefined("description"));
        Assert.assertTrue(todo.definition().hasFieldNameDefined("doneStatus"));

        Assert.assertEquals("FALSE", todo.definition().getField("doneStatus").getDefaultValue());


    }


}


