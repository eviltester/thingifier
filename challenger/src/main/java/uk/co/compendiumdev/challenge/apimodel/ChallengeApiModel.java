package uk.co.compendiumdev.challenge.apimodel;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

public class ChallengeApiModel {
    public Thingifier get() {
        Thingifier todoList = new Thingifier();

        StringBuilder para = new StringBuilder();

        para.append("A Simple todo list");


        todoList.setDocumentation("Simple Todo List", para.toString());

        // can create a maximum of 20 todos in the challenge todos list
        EntityDefinition todo = todoList.defineThing("todo", "todos", 20);
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addFields(
            Field.is("title", STRING).
                    withExample("A title").
                    makeMandatory().
                    withValidation(
                            VRule.notEmpty(),
                            VRule.maximumLength(50)),
            Field.is("doneStatus", FieldType.BOOLEAN).
                    withDefaultValue("false"),
            Field.is("description", STRING).
                    withExample("my description").
                    withValidation(VRule.maximumLength(200))
        );


        todoList.setDataGenerator(new TodoAPIDataPopulator());
        todoList.generateData(EntityRelModel.DEFAULT_DATABASE_NAME);

        todoList.apiConfig().setApiToShowPrimaryKeyHeaderInResponse(false);
        todoList.apiConfig().statusCodes().setMaxRequestBodyLengthBytes(5000);
        todoList.apiConfig().setReturnSingleGetItemsAsCollection(true);

        return todoList;
    }
}
