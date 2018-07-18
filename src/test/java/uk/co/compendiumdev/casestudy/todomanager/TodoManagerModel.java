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

    // explore the Thingifier via a todo manager case study

    /*
        Entities:
            TODO:
                - title, description, doneStatus(TRUE,FALSE)
            project:
                - title, description, completed, active

        Relationships:
            Project -> has many -> todos
            todo -> can be part of many -> project

    */



    /*

Thinking through API

GET todo/_GUID_   - single todo with all fields   {todo : guid = _GUID_}
GET todo          - all todos with all fields     {todo}

// would have to specify - couldn't get for free
GET todo/done     - all done todos                {todo : doneStatus = TRUE}

GET project       - all projects                  {project}
GET project/_GUID_/todos    - all todos for the project    {project : guid = _GUID_} -> "tasks.todo"
GET project/_GUID_/categories    - all categories for the project  {project : guid = _GUID_} -> "categories.category"

// would have to specify couldn't get for free
GET project/completed - all completed projects      {project : completed = TRUE}
GET project/active    - all active projects         {project : active = TRUE}

GET category      - all categories                      {category}
GET category/_GUID_/todos - all todos for that category   {category : guid = _GUID_} -> "todos.todo"
GET category/_GUID_/projects - all projects for that category {category : guid = _GUID_} -> "projects.project"

// only PUT should allow adding/amending a GUID

relationships woudld be

POST todo/_GUID_/categories
{category : [{"guid":"12345"}, {"guid":"45678"}]}
[{"guid":"12345"}, {"guid":"45678"}]
{"guid":"12345"}

PUT todo/_GUID_/categories   - would make sure only thes listed categories were associated as relationship

POST to update any field
POST todo/_GUID_  {title : "new title", etc.}

DELETE project/_GUID_   - delete a project

?title="test*" match titles - can combine with DELETE and POST to update many

Should this come for 'free' or do we need to specify it

e.g. THING/_GUID_/RELATIONSHIP/THING

could implement a Thingifier URL query matcher to return instances based on query to get much of this for free


 */

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
