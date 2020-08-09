package uk.co.compendiumdev.challenge.apimodel;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.validation.VRule;

import static uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.FieldType.STRING;

public class ChallengeApiModel {
    public Thingifier get() {
        Thingifier todoList = new Thingifier();

        StringBuilder para = new StringBuilder();

        para.append("A Simple todo list");


        todoList.setDocumentation("Simple Todo List", para.toString());

        Thing todo = todoList.createThing("todo", "todos");

        todo.definition()
                .addFields(
                        Field.is("id", FieldType.ID),
                        Field.is("title", STRING).
                                makeMandatory().
                                withValidation(
                                        VRule.notEmpty()),
                        Field.is("doneStatus", FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("description", STRING)
                );


        todoList.setDataGenerator(new TodoAPIDataPopulator());
        todoList.generateData();

        todoList.apiConfig().setResponsesToShowGuids(false);

        return todoList;
    }
}
