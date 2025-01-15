package uk.co.compendiumdev.thingifier.swaggerizer;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

public class TodoListThingifierTestModel {

    /*
            A Very simple list of to do items
     */
    public Thingifier get() {



        Thingifier todoList = new Thingifier();

        StringBuilder para = new StringBuilder();

        para.append("A Simple todo list");


        todoList.setDocumentation("Simple Todo List", para.toString());

        EntityDefinition todo = todoList.defineThing("todo", "todos");

        todo.addAsPrimaryKeyField(Field.is("id",FieldType.AUTO_INCREMENT));
        todo
                .addFields(
                        Field.is("title", STRING).
                                makeMandatory().
                                withValidation(
                                        VRule.notEmpty()),
                        Field.is("doneStatus", FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("description", STRING)
                        );


        // TODO: have a data generator per profile
        todoList.setDataGenerator(new TodoAPITestDataPopulator());
        todoList.generateData(EntityRelModel.DEFAULT_DATABASE_NAME);

        // API Config Profiles

        ThingifierApiConfigProfile profilev0 = todoList.apiConfigProfiles().create(
                "0",
                "v0 prototype");
        ThingifierApiConfig v0 = profilev0.apiConfig();
        v0.setUrlToShowSingleInstancesAsPlural(false);
        v0.setApiToShowPrimaryKeyHeaderInResponse(false);
        v0.jsonOutput().setConvertFieldsToDefinedTypes(false);
        v0.setApiToEnforceDeclaredTypesInInput(false);

        ThingifierApiConfigProfile profilev1 = todoList.apiConfigProfiles().create(
                                                "1",
                                            "v1 single as plural");

        ThingifierApiConfig v1 = profilev1.apiConfig();
        v1.setUrlToShowSingleInstancesAsPlural(true);
        v0.setApiToShowPrimaryKeyHeaderInResponse(false);
        v1.jsonOutput().setConvertFieldsToDefinedTypes(false);
        v1.setApiToEnforceDeclaredTypesInInput(false);

        ThingifierApiConfigProfile profilev2 = todoList.apiConfigProfiles().create(
                "2",
                "v2 no id header, improved output validation");

        ThingifierApiConfig v2 = profilev2.apiConfig();
        v2.setUrlToShowSingleInstancesAsPlural(true);
        v0.setApiToShowPrimaryKeyHeaderInResponse(false);
        v2.jsonOutput().setConvertFieldsToDefinedTypes(true);
        v2.setApiToEnforceDeclaredTypesInInput(false);

        ThingifierApiConfigProfile profilev3 = todoList.apiConfigProfiles().create(
                "3",
                "v3 use ids in headers and validation");

        ThingifierApiConfig v3 = profilev3.apiConfig();
        v3.setUrlToShowSingleInstancesAsPlural(true);
        v3.setApiToShowPrimaryKeyHeaderInResponse(true);
        v3.jsonOutput().setConvertFieldsToDefinedTypes(true);
        v3.setApiToEnforceDeclaredTypesInInput(true);
        return todoList;
    }
}
