package uk.co.compendiumdev.thingifier.application.examples;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

import static uk.co.compendiumdev.thingifier.domain.FieldType.STRING;

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
                        Field.is("doneStatus", FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("description", STRING)
                        );


        // TODO: create a 'sample data' definition
        ThingInstance paperwork = todo.createInstance().setValue("title", "scan paperwork");
        todo.addInstance(paperwork);

        ThingInstance filework = todo.createInstance().setValue("title", "file paperwork");
        todo.addInstance(filework);

        // API Config Profiles

        ThingifierApiConfigProfile profilev0 = todoList.apiConfigProfiles().create(
                "0",
                "v0 prototype");
        ThingifierApiConfig v0 = profilev0.apiConfig();
        v0.allowShowIdsInUrlsIfAvailable(false);
        v0.allowShowIdsInResponsesIfAvailable(false);
        v0.showSingleInstancesAsPlural(false);
        v0.allowShowGuidsInResponses(true);
        v0.jsonOutput().convertFieldsToDefinedTypes(false);
        v0.shouldEnforceDeclaredTypesInInput(false);

        ThingifierApiConfigProfile profilev1 = todoList.apiConfigProfiles().create(
                                                "1",
                                            "v1 harder to use, based on guids");

        ThingifierApiConfig v1 = profilev1.apiConfig();
        v1.allowShowIdsInUrlsIfAvailable(false);
        v1.allowShowIdsInResponsesIfAvailable(false);
        v1.showSingleInstancesAsPlural(true);
        v1.allowShowGuidsInResponses(true);
        v1.jsonOutput().convertFieldsToDefinedTypes(false);
        v1.shouldEnforceDeclaredTypesInInput(false);

        ThingifierApiConfigProfile profilev2 = todoList.apiConfigProfiles().create(
                "2",
                "v2 still uses guids but prefers ids, improved output validation");

        ThingifierApiConfig v2 = profilev2.apiConfig();
        v2.allowShowIdsInUrlsIfAvailable(true);
        v2.allowShowIdsInResponsesIfAvailable(true);
        v2.showSingleInstancesAsPlural(true);
        v2.allowShowGuidsInResponses(true);
        v2.jsonOutput().convertFieldsToDefinedTypes(true);
        v2.shouldEnforceDeclaredTypesInInput(false);

        ThingifierApiConfigProfile profilev3 = todoList.apiConfigProfiles().create(
                "3",
                "v3 use ids");

        ThingifierApiConfig v3 = profilev3.apiConfig();
        v3.allowShowIdsInUrlsIfAvailable(true);
        v3.allowShowIdsInResponsesIfAvailable(true);
        v3.showSingleInstancesAsPlural(true);
        v3.allowShowGuidsInResponses(false);
        v3.jsonOutput().convertFieldsToDefinedTypes(true);
        v3.shouldEnforceDeclaredTypesInInput(false);
        return todoList;
    }
}
