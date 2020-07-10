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
            A Very simple list of to do items
     */
    public Thingifier get() {



        Thingifier todoList = new Thingifier();

        StringBuilder para = new StringBuilder();

        para.append("A Simple todo list");


        todoList.setDocumentation("Simple Todo List", para.toString());

        Thing todo = todoList.createThing("todo", "todos");

        todo.definition()
                .addFields(
                        Field.is("id",FieldType.ID),
                        Field.is("title", STRING).
                                mandatory().
                                withValidation(
                                        VRule.notEmpty()),
                        Field.is("description", STRING),
                        Field.is("doneStatus", FieldType.BOOLEAN).
                                withDefaultValue("false"));


        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);

        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        return todoList;
    }
}
