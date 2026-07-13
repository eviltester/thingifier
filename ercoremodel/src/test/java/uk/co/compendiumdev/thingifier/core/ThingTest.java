package uk.co.compendiumdev.thingifier.core;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.INTEGER;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStore;

public class ThingTest {

    @Test
    public void thingUsageExample() {

        ERSchema schema = new ERSchema();
        EntityDefinition person = schema.defineEntity("person", "people", -1);
        person.addFields(Field.is("name", STRING), Field.is("age", INTEGER));
        ThingStore repository = new InMemoryThingStore("example");
        repository.administration().initializeFrom(schema);

        EntityInstance bob =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(person)
                                        .withField("name", "Bob")
                                        .withField("age", "56"));

        repository
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(person)
                                .withField("name", "Eris")
                                .withField("age", "1000"));

        Assertions.assertEquals(2, repository.entityQueries().count(person));
        Assertions.assertEquals("Bob", bob.getFieldValue("name").asString());
        Assertions.assertEquals("56", bob.getFieldValue("age").asString());
        Assertions.assertEquals(
                "1000",
                repository
                        .entityQueries()
                        .findByField(person, "name", "Eris")
                        .getFieldValue("age")
                        .asString());
    }

    @Test
    public void moreThingUsageExamples() {

        ERSchema schema = new ERSchema();
        EntityDefinition url = schema.defineEntity("URL", "URLs", -1);
        url.addFields(
                Field.is("url", STRING), Field.is("visited", INTEGER), Field.is("name", STRING));
        ThingStore repository = new InMemoryThingStore("example");
        repository.administration().initializeFrom(schema);

        Assertions.assertTrue(url.hasFieldNameDefined("url"));
        Assertions.assertTrue(url.hasFieldNameDefined("name"));
        Assertions.assertTrue(url.hasFieldNameDefined("visited"));

        repository
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(url)
                                .withField("name", "EvilTester.com")
                                .withField("url", "http://eviltester.com"));

        repository
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(url)
                                .withField("name", "JavaForTesters.com")
                                .withField("url", "http://javaForTesters.com"));

        Collection<EntityInstance> instances = repository.entityQueries().list(url);

        System.out.println("NAME\tURL");
        System.out.println("==========");

        for (EntityInstance aURL : instances) {
            System.out.println(
                    String.format(
                            "%s\t%s",
                            aURL.getFieldValue("name").asString(),
                            aURL.getFieldValue("url").asString()));
        }

        Assertions.assertEquals(2, instances.size());
    }

    @Test
    public void todoModelUsageExamples() {

        // Start simple with a to do manager model e.g. to do items, context, project (can also be a
        // sub-project), task group

        EntityDefinition todo = new EntityDefinition("ToDo", "ToDos");

        todo.addFields(
                Field.is("title", STRING),
                Field.is("description", STRING),
                Field.is("doneStatus", FieldType.BOOLEAN).withDefaultValue("FALSE"));

        Assertions.assertTrue(todo.hasFieldNameDefined("title"));
        Assertions.assertTrue(todo.hasFieldNameDefined("description"));
        Assertions.assertTrue(todo.hasFieldNameDefined("doneStatus"));

        Assertions.assertEquals("FALSE", todo.getField("doneStatus").getDefaultValue().asString());
    }
}
