package uk.co.compendiumdev.casestudy.todomanager;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.AndCall;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.Between;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.WithCardinality;

import static uk.co.compendiumdev.thingifier.generic.FieldType.STRING;

public class TodoManagerModel {
    public static Thingifier definedAsThingifier() {
        Thingifier todoManager = new Thingifier();

        todoManager.setDocumentation("Todo Manager", "A Simple todo manager");

        Thing todo = todoManager.createThing("todo", "todos");

        todo.definition()
                .addFields( Field.is("title", STRING).
                                mandatory().
                                withValidation(
                                        VRule.NotEmpty(),
                                        VRule.MatchesType()),
                        Field.is("description",STRING),
                        Field.is("doneStatus",FieldType.BOOLEAN).
                                withDefaultValue("FALSE").
                                withValidation(
                                        VRule.MatchesType()))
        ;


        Thing project = todoManager.createThing("project", "projects");

        project.definition()
                .addFields(
                        Field.is("title", STRING),
                        Field.is("description",STRING),
                        Field.is("completed",FieldType.BOOLEAN).
                                withDefaultValue("FALSE").
                                withValidation(VRule.MatchesType()),
                        Field.is("active",FieldType.BOOLEAN).
                                withDefaultValue("TRUE").
                                withValidation(VRule.MatchesType()));


        Thing category = todoManager.createThing("category", "categories");

        category.definition()
                .addFields(
                        Field.is("title", STRING).
                                mandatory().
                                withValidation(VRule.NotEmpty()),
                        Field.is("description",STRING));

        todoManager.defineRelationship(Between.things(project, todo), AndCall.it("tasks"), WithCardinality.of("1", "*")).
                whenReversed(WithCardinality.of("1","*"),AndCall.it("task-of"));

        todoManager.defineRelationship(Between.things(project, category), AndCall.it("categories"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, todo), AndCall.it("todos"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, project), AndCall.it("projects"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(todo, category), AndCall.it("categories"), WithCardinality.of("1", "*"));
        
        return todoManager;
    }
}
