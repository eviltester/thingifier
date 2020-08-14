package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.Collection;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.INTEGER;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

public class ThingTest {

    @Test
    public void thingUsageExample(){


        Thing person = Thing.
                create("person", "people");

        person.definition().
                addFields(Field.is("name"), Field.is("age", INTEGER));

        ThingInstance bob = person.createManagedInstance().
                setValue("name","Bob");

        bob.setValue("age", "56");

        ThingInstance eris = person.createManagedInstance().
                setValue("name","Eris").setValue("age", "1000");

        Assertions.assertEquals(2, person.countInstances());
        Assertions.assertEquals("Bob", bob.getFieldValue("name").asString());
        Assertions.assertEquals("56", bob.getFieldValue("age").asString());
        Assertions.assertEquals("1000", person.findInstanceByField(FieldValue.is("name", "Eris")).getFieldValue("age").asString());

    }

    @Test
    public void moreThingUsageExamples(){

        Thing url = Thing.create("URL", "URLs");

        url.definition().addFields(Field.is("url"),
                Field.is("visited", INTEGER), Field.is("name",STRING));

        Assertions.assertTrue(url.definition().hasFieldNameDefined("url"));
        Assertions.assertTrue(url.definition().hasFieldNameDefined("name"));
        Assertions.assertTrue(url.definition().hasFieldNameDefined("visited"));


        url.createManagedInstance().
           setValue("name","EvilTester.com").
            setValue("url", "http://eviltester.com");


        url.createManagedInstance().
            setValue("name","JavaForTesters.com").
            setValue("url", "http://javaForTesters.com");


        Collection<ThingInstance> instances = url.getInstances();

        System.out.println("NAME\tURL");
        System.out.println("==========");

        for(ThingInstance aURL : instances){
            System.out.println(String.format("%s\t%s", aURL.getFieldValue("name").asString(), aURL.getFieldValue("url").asString()));
        }

        Assertions.assertEquals(2, instances.size());

    }

    @Test
    public void todoModelUsageExamples(){

        // Start simple with a to do manager model e.g. to do items, context, project (can also be a sub-project), task group

        Thing todo = Thing.create("ToDo", "ToDos");

        todo.definition()
                .addFields(Field.is("title", STRING), Field.is("description",STRING),
                        Field.is("doneStatus", FieldType.BOOLEAN).withDefaultValue("FALSE"));

        Assertions.assertTrue(todo.definition().hasFieldNameDefined("title"));
        Assertions.assertTrue(todo.definition().hasFieldNameDefined("description"));
        Assertions.assertTrue(todo.definition().hasFieldNameDefined("doneStatus"));

        Assertions.assertEquals("FALSE", todo.definition().
                getField("doneStatus").
                getDefaultValue().asString());


    }
}
