package uk.co.compendiumdev.thingifier.application.examples;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.AndCall;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.Between;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.WithCardinality;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import static uk.co.compendiumdev.thingifier.generic.FieldType.STRING;

public class TodoListThingifier {

    /*
            A Very simple list of todo items
     */
    public Thingifier get() {



        Thingifier todoList = new Thingifier();

        StringBuilder para = new StringBuilder();

        para.append("A Simple todo list<br/><br/>\n\n");
        para.append("Will accept json by default.<br/><br/>\n");
        para.append("<i>Content-Type: application/json</i><br/><br/>\n");
        para.append("Set Content-Type header to application/xml if you want to send in XML.<br/><br/>\n");
        para.append("<i>Content-Type: application/xml</i><br/><br/>\n");
        para.append("XML body would look something like:<br/><br/>\n");
        para.append("<i>&#x3C;todo&#x3E;&#x3C;title&#x3E;My posted todo &#x3C;/title&#x3E;&#x3C;/todo&#x3E;</i><br/><br/>\n");
        para.append("JSON body would look something like:<br/><br/>\n");
        para.append("<i>{\"title\":\"My posted todo\"}</i><br/><br/>\n");
        para.append("You can control the returned data format by setting the Accept header<br/><br/>\n");
        para.append("i.e. for XML use<br/><br/>\n");
        para.append("<i>Accept: application/xml</i><br/><br/>\n");
        para.append("You get JSON by default but can also request this i.e.<br/><br/>\n");
        para.append("<i>Accept: application/json</i><br/><br/>\n");

        para.append("<br/>All data lives in memory and is not persisted so the application is cleared everytime you start it. It does have some test data in here when you start<br/>\n");


        todoList.setDocumentation("Simple Todo List", para.toString());

        Thing todo = todoList.createThing("todo", "todos");

        todo.definition()
                .addFields(Field.is("title", STRING).
                                mandatory().
                                withValidation(
                                        VRule.notEmpty(),
                                        VRule.matchesType()),
                        Field.is("description", STRING),
                        Field.is("doneStatus", FieldType.BOOLEAN).
                                withDefaultValue("FALSE").
                                withValidation(
                                        VRule.matchesType()));


        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);

        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        return todoList;
    }
}
