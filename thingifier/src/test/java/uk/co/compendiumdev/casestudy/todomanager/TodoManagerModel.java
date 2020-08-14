package uk.co.compendiumdev.casestudy.todomanager;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.INTEGER;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

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

    // TODO: validate model
    // TODO: do not allow fields to have same names are relationships
    // TODO: more types of fields SET(predefined set of values), NUMBER (floating point)
    // TODO: transformations for fields e.g. allow Number to Integer (or validate against this)
    // TODO: transformations for SET (match case, or any case)
    // TODO: validation for dates - specific date formats
    // TODO: allow creation of a key/unique field to use instead of guid e.g. .keyIs("id")
    //     this means that /todos/_id_ would match the entity, rather than guid
    // TODO: allow definition of unique scheme e.g. id starting at 0 auto increment (GUID is global, ID is per entity)


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
                                makeMandatory().
                                withValidation(
                                        VRule.notEmpty()),
                        Field.is("description",STRING),
                        Field.is("doneStatus",FieldType.BOOLEAN).
                                withDefaultValue("false")
                )
        ;


        Thing project = todoManager.createThing("project", "projects");

        project.definition()
                .addFields(
                        Field.is("title", STRING),
                        Field.is("description",STRING),
                        Field.is("completed",FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("active",FieldType.BOOLEAN).
                                withDefaultValue("true"));


        Thing category = todoManager.createThing("category", "categories");

        category.definition()
                .addFields(
                        Field.is("title", STRING).
                                makeMandatory().
                                withValidation(VRule.notEmpty()),
                        Field.is("description",STRING));

        todoManager.defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY).
                whenReversed(Cardinality.ONE_TO_MANY,"task-of");

        todoManager.defineRelationship(project, category, "categories", Cardinality.ONE_TO_MANY);
        todoManager.defineRelationship(category, todo, "todos", Cardinality.ONE_TO_MANY);
        todoManager.defineRelationship(category, project, "projects", Cardinality.ONE_TO_MANY);
        todoManager.defineRelationship(todo, category, "categories", Cardinality.ONE_TO_MANY);


        // TODO create mandatory relationships = at the moment all entities can exist without relationship
        // e.g. create an estimate for a todo - the estimate must have a todo

        Thing estimate = todoManager.createThing("estimate", "estimates");
        estimate.definition()
                .addFields(
                        Field.is("duration", INTEGER).
                                makeMandatory().
                                withValidation(VRule.notEmpty()),
                        Field.is("description", STRING));

        // a todo can only have one estimate, and an estimate is for one todo
        final RelationshipDefinition estimated = todoManager.defineRelationship(
                estimate, todo,
                "estimate",
                Cardinality.ONE_TO_MANY).
                whenReversed(Cardinality.ONE_TO_MANY, "estimates");

        // an estimate must have a todo, a todo does not need to have an estimate
        estimated.getFromRelationship().setOptionality(Optionality.MANDATORY_RELATIONSHIP);


        // TODO there is a special case of Mandatory : Mandatory which we need to be able to 'create' entities at same time as relationships

        todoManager.apiConfig().setApiToEnforceDeclaredTypesInInput(false);
        todoManager.apiConfig().jsonOutput().setConvertFieldsToDefinedTypes(false);
        todoManager.apiConfig().setApiToEnforceContentTypeForRequests(false);

        return todoManager;
    }
}
