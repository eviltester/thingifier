package uk.co.compendiumdev.thingifier.application.examples;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.application.data.TodoManagerAPIDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

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

        EntityDefinition todo = todoManager.defineThing("todo", "todos");
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        // todo: show fields in the order we add them, not the hashmap key order
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

        EntityDefinition project = todoManager.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project
                .addFields(
                        Field.is("title", STRING),
                        Field.is("completed", FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("active", FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("description", STRING));


        EntityDefinition category = todoManager.defineThing("category", "categories");
        category.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        category
                .addFields(
                        Field.is("title", STRING).
                                makeMandatory().
                                withValidation(VRule.notEmpty()),
                        Field.is("description", STRING));

        todoManager.defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY()).
                whenReversed(Cardinality.ONE_TO_MANY(), "tasksof");

        todoManager.defineRelationship(project, category, "categories", Cardinality.ONE_TO_MANY());
        todoManager.defineRelationship(category, todo, "todos", Cardinality.ONE_TO_MANY());
        todoManager.defineRelationship(category, project, "projects", Cardinality.ONE_TO_MANY());
        todoManager.defineRelationship(todo, category, "categories", Cardinality.ONE_TO_MANY());

        // Some hard coded test data for experimenting with
        // TODO: allow importing from a JSON to create data in bulk
        todoManager.setDataGenerator(new TodoManagerAPIDataPopulator());
        todoManager.generateData(EntityRelModel.DEFAULT_DATABASE_NAME);
        todoManager.apiConfig().setReturnSingleGetItemsAsCollection(true);

        // PROFILES
        // can have different -version params which configure the TodoManagerThingifier in different ways
        // e.g. v1 non compressed relationships with guids
        // e.g. v2 compressed relationships with guids
        // e.g. v3 compressed relationships with ids
        // default the app to v3 to make it easier for people

        ThingifierApiConfigProfile profile0 = todoManager.apiConfigProfiles().
                create("v0", "prototype");
        ThingifierApiConfig config0 = profile0.apiConfig();


        config0.setUrlToShowSingleInstancesAsPlural(false);
        config0.jsonOutput().setCompressRelationships(false);
        config0.jsonOutput().setConvertFieldsToDefinedTypes(false);
        config0.setApiToEnforceDeclaredTypesInInput(false);
        config0.setReturnSingleGetItemsAsCollection(true);

        ThingifierApiConfigProfile profile = todoManager.apiConfigProfiles().
                create("v1", "non compressed relationships with guids");
        ThingifierApiConfig config = profile.apiConfig();


        config.setUrlToShowSingleInstancesAsPlural(true);
        config.setApiToShowPrimaryKeyHeaderInResponse(true);
        config.jsonOutput().setCompressRelationships(false);
        config.jsonOutput().setConvertFieldsToDefinedTypes(false);
        config.setApiToEnforceDeclaredTypesInInput(false);
        config.setReturnSingleGetItemsAsCollection(true);

        ThingifierApiConfigProfile profile2 = todoManager.apiConfigProfiles().
                create("v2", "compressed relationships with guids");
        ThingifierApiConfig config2 = profile2.apiConfig();
        config2.setUrlToShowSingleInstancesAsPlural(true);
        config2.setApiToShowPrimaryKeyHeaderInResponse(true);
        config2.jsonOutput().setCompressRelationships(true);
        config2.jsonOutput().setConvertFieldsToDefinedTypes(false);
        config2.setApiToEnforceDeclaredTypesInInput(false);
        config2.setReturnSingleGetItemsAsCollection(true);

        ThingifierApiConfigProfile profile3 = todoManager.apiConfigProfiles().
                create("v3", "compressed relationships with ids");
        ThingifierApiConfig config3 = profile3.apiConfig();
        config3.setUrlToShowSingleInstancesAsPlural(true);
        config3.setApiToShowPrimaryKeyHeaderInResponse(true);
        config3.jsonOutput().setCompressRelationships(true);
        config3.jsonOutput().setConvertFieldsToDefinedTypes(false);
        config3.setApiToEnforceDeclaredTypesInInput(true);
        config3.setReturnSingleGetItemsAsCollection(true);

        return todoManager;
    }
}
