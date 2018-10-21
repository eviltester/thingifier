package uk.co.compendiumdev.casestudy.todomanager;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.AndCall;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.Between;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.WithCardinality;

import static uk.co.compendiumdev.thingifier.generic.FieldType.INTEGER;
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

        StringBuilder para = new StringBuilder();

        para.append("A Simple todo manager<br/>\n\n");
        para.append("Will accept json by default.<br/>\n");
        para.append("Set Content-Type header to application/xml if you want to send in XML.<br/>\n");
        para.append("XML body would look something like:<br/>\n");
        // <project><title>My posted todo on the project</title></project>
        // would become {"project":{"title":"My posted todo on the project"}}
        // when we want {"title":"My posted todo on the project"}
        para.append("&#x3C;project&#x3E;&#x3C;title&#x3E;My posted todo on the project&#x3C;/title&#x3E;&#x3C;/project&#x3E;<br/>\n");
        para.append("JSON body would look something like:<br/>\n");
        para.append("{\"title\":\"My posted todo on the project\"}<br/>\n");

        todoManager.setDocumentation("Todo Manager", para.toString());

        Thing todo = todoManager.createThing("todo", "todos");

        todo.definition()
                .addFields( Field.is("title", STRING).
                                mandatory().
                                withValidation(
                                        VRule.notEmpty(),
                                        VRule.matchesType()),
                        Field.is("description",STRING),
                        Field.is("doneStatus",FieldType.BOOLEAN).
                                withDefaultValue("FALSE").
                                withValidation(
                                        VRule.matchesType()))
        ;


        Thing project = todoManager.createThing("project", "projects");

        project.definition()
                .addFields(
                        Field.is("title", STRING),
                        Field.is("description",STRING),
                        Field.is("completed",FieldType.BOOLEAN).
                                withDefaultValue("FALSE").
                                withValidation(VRule.matchesType()),
                        Field.is("active",FieldType.BOOLEAN).
                                withDefaultValue("TRUE").
                                withValidation(VRule.matchesType()));


        Thing category = todoManager.createThing("category", "categories");

        category.definition()
                .addFields(
                        Field.is("title", STRING).
                                mandatory().
                                withValidation(VRule.notEmpty()),
                        Field.is("description",STRING));

        todoManager.defineRelationship(Between.things(project, todo), AndCall.it("tasks"), WithCardinality.of("1", "*")).
                whenReversed(WithCardinality.of("1","*"),AndCall.it("task-of"));

        todoManager.defineRelationship(Between.things(project, category), AndCall.it("categories"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, todo), AndCall.it("todos"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, project), AndCall.it("projects"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(todo, category), AndCall.it("categories"), WithCardinality.of("1", "*"));


        // TODO create mandatory relationships = at the moment all entities can exist without relationship
        // e.g. create an estimate for a todo - the estimate must have a todo

        Thing estimate = todoManager.createThing("estimate", "estimates");
        estimate.definition()
                .addFields(
                        Field.is("duration", INTEGER).
                                mandatory().
                                withValidation(VRule.notEmpty()),
                        Field.is("description", STRING));

        // a todo can only have one estimate, and an estimate is for one todo
        final RelationshipDefinition estimated = todoManager.defineRelationship(
                Between.things(estimate, todo),
                AndCall.it("estimate"),
                WithCardinality.of("1", "1"));
        // an estimate must have a todo, a todo does not need to have an estimate
        estimated.hasOptionality("M", "O");

        // TODO there is a special case of Mandatory : Mandatory which we need to be able to 'create' entities at same time as relationships

        return todoManager;
    }
}
