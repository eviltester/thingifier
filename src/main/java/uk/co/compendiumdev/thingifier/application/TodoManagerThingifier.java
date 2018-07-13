package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.AndCall;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.Between;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.WithCardinality;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import static uk.co.compendiumdev.thingifier.generic.FieldType.STRING;

public class TodoManagerThingifier {

    public Thingifier get(){

        // this is basically an Entity Relationship diagram as source
        //TODO:  should expand functionality based on E-R diagrams

        Thingifier todoManager = new Thingifier();

        Thing todo = todoManager.createThing("todo", "todos");

        todo.definition()
                .addFields(Field.is("title", STRING), Field.is("description",STRING),
                        Field.is("doneStatus",FieldType.BOOLEAN).withDefaultValue("FALSE"));


        Thing project = todoManager.createThing("project", "projects");

        project.definition()
                .addFields(Field.is("title", STRING), Field.is("description",STRING),
                        Field.is("completed",FieldType.BOOLEAN).withDefaultValue("FALSE"),
                        Field.is("active",FieldType.BOOLEAN).withDefaultValue("TRUE"));


        Thing category = todoManager.createThing("category", "categories");

        category.definition()
                .addFields(Field.is("title", STRING), Field.is("description",STRING));

        todoManager.defineRelationship(Between.things(project, todo), AndCall.it("tasks"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(project, category), AndCall.it("categories"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, todo), AndCall.it("todos"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, project), AndCall.it("projects"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(todo, category), AndCall.it("categories"), WithCardinality.of("1", "*"));


        // Some hard coded test data for experimenting with
        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);

        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        ThingInstance officeCategory = category.createInstance().setValue("title", "Office");

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
