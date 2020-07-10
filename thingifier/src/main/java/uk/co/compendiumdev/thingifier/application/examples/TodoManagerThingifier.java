package uk.co.compendiumdev.thingifier.application.examples;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.AndCall;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.Between;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.WithCardinality;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import static uk.co.compendiumdev.thingifier.generic.FieldType.INTEGER;
import static uk.co.compendiumdev.thingifier.generic.FieldType.STRING;

public class TodoManagerThingifier {

    /*
            REMEMBER - THIS IS THE PRODUCTION DEFINITION
     */
    public Thingifier get() {

        // this is basically an Entity Relationship diagram as source
        // TODO:  should expand functionality based on E-R diagrams
        // TODO: import thingifier from JSON to allow dynamic configuration
        // TODO: allow configuration from -defn FILENAME when starting at the command line

        // TODO: validate against field type DATE
        // TODO: create SET field type
        // TODO: validate against set type
        // TODO: create validation rule MatchesFieldType to allow configuration of field validation against type
        // TODO: ValidationRule Interface - implemented by rules and ValidtionRuleGroup to allow processing in a list - main method validates()
        // TODO: create a MinimumLength validation rule
        // TODO: create a MaximumLength validation rule
        // TODO: create a IsGreaterThan validation rule
        // TODO: create an IsLessThan validation rule
        // TODO create a ValidationRuleGroup.and()  where all the validation rules in the group must pass for validation to pass
        // TODO create a ValidationRuleGroup.or()  where any of the validation rules in the group must pass for validation to pass - stops on first 'pass'
        // TODO : create a MatchesRegex validation rule for fields - this will be the simplest way of creating complex validation quickly
        // e.g. (.|\s)*\S(.|\s)*    non empty


        Thingifier todoManager = new Thingifier();

        StringBuilder para = new StringBuilder();

        para.append("A Simple todo manager with categories and projects.");

        todoManager.setDocumentation("Todo Manager", para.toString());

        Thing todo = todoManager.createThing("todo", "todos");

        // todo: show fields in the order we add them, not the hashmap key order
        todo.definition()
                .addFields(Field.is("id", FieldType.ID),
                        Field.is("title", STRING).
                                mandatory().
                                withValidation(
                                        VRule.notEmpty()),
                        Field.is("description", STRING),
                        Field.is("doneStatus", FieldType.BOOLEAN).
                                withDefaultValue("false"));

        Thing project = todoManager.createThing("project", "projects");

        project.definition()
                .addFields(
                        Field.is("id", FieldType.ID),
                        Field.is("title", STRING),
                        Field.is("description", STRING),
                        Field.is("completed", FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("active", FieldType.BOOLEAN).
                                withDefaultValue("false"));


        Thing category = todoManager.createThing("category", "categories");

        category.definition()
                .addFields(
                        Field.is("id", FieldType.ID),
                        Field.is("title", STRING).
                                mandatory().
                                withValidation(VRule.notEmpty()),
                        Field.is("description", STRING));

        todoManager.defineRelationship(Between.things(project, todo), AndCall.it("tasks"), WithCardinality.of("1", "*")).
                whenReversed(WithCardinality.of("1", "*"), AndCall.it("task-of"));

        todoManager.defineRelationship(Between.things(project, category), AndCall.it("categories"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, todo), AndCall.it("todos"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, project), AndCall.it("projects"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(todo, category), AndCall.it("categories"), WithCardinality.of("1", "*"));

        // Some hard coded test data for experimenting with
        // TODO: allow importing from a JSON to create data in bulk
        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);

        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        ThingInstance officeCategory = category.createInstance().setValue("title", "Office");
        category.addInstance(officeCategory);

        ThingInstance homeCategory = category.createInstance().setValue("title", "Home");
        category.addInstance(homeCategory);

        ThingInstance officeWork = project.createInstance().setValue("title", "Office Work");
        project.addInstance(officeWork);

        officeWork.connects("tasks", paperwork);
        officeWork.connects("tasks", filework);

        paperwork.connects("categories", officeCategory);


        return todoManager;
    }
}
