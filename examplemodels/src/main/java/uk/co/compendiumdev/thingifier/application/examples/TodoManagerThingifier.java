package uk.co.compendiumdev.thingifier.application.examples;

import uk.co.compendiumdev.thingifier.core.Thing;
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

        Thing todo = todoManager.createThing("todo", "todos");

        // todo: show fields in the order we add them, not the hashmap key order
        todo.definition()
                .addFields(Field.is("id", FieldType.ID),
                        Field.is("title", STRING).
                                makeMandatory().
                                withValidation(
                                        VRule.notEmpty()),
                        Field.is("doneStatus", FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("description", STRING)
                        );

        Thing project = todoManager.createThing("project", "projects");

        project.definition()
                .addFields(
                        Field.is("id", FieldType.ID),
                        Field.is("title", STRING),
                        Field.is("completed", FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("active", FieldType.BOOLEAN).
                                withDefaultValue("false"),
                        Field.is("description", STRING));


        Thing category = todoManager.createThing("category", "categories");

        category.definition()
                .addFields(
                        Field.is("id", FieldType.ID),
                        Field.is("title", STRING).
                                makeMandatory().
                                withValidation(VRule.notEmpty()),
                        Field.is("description", STRING));

        todoManager.defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY).
                whenReversed(Cardinality.ONE_TO_MANY, "task-of");

        todoManager.defineRelationship(project, category, "categories", Cardinality.ONE_TO_MANY);
        todoManager.defineRelationship(category, todo, "todos", Cardinality.ONE_TO_MANY);
        todoManager.defineRelationship(category, project, "projects", Cardinality.ONE_TO_MANY);
        todoManager.defineRelationship(todo, category, "categories", Cardinality.ONE_TO_MANY);

        // Some hard coded test data for experimenting with
        // TODO: allow importing from a JSON to create data in bulk
        todoManager.setDataGenerator(new TodoManagerAPIDataPopulator());
        todoManager.generateData();

        // PROFILES
        // can have different -version params which configure the TodoManagerThingifier in different ways
        // e.g. v1 non compressed relationships with guids
        // e.g. v2 compressed relationships with guids
        // e.g. v3 compressed relationships with ids
        // default the app to v3 to make it easier for people

        ThingifierApiConfigProfile profile0 = todoManager.apiConfigProfiles().
                create("v0", "prototype");
        ThingifierApiConfig config0 = profile0.apiConfig();

        config0.setUrlToShowIdsInUrlsIfAvailable(false);
        config0.setResponsesToShowIdsIfAvailable(false);
        config0.setUrlToShowSingleInstancesAsPlural(false);
        config0.setResponsesToShowGuids(true);
        config0.jsonOutput().setCompressRelationships(false);
        config0.jsonOutput().setRelationshipsUseIdsIfAvailable(false);
        config0.jsonOutput().setConvertFieldsToDefinedTypes(false);
        config0.setApiToEnforceDeclaredTypesInInput(false);

        ThingifierApiConfigProfile profile = todoManager.apiConfigProfiles().
                create("v1", "non compressed relationships with guids");
        ThingifierApiConfig config = profile.apiConfig();

        config.setUrlToShowIdsInUrlsIfAvailable(false);
        config.setResponsesToShowIdsIfAvailable(false);
        config.setUrlToShowSingleInstancesAsPlural(true);
        config.setResponsesToShowGuids(true);
        config.jsonOutput().setCompressRelationships(false);
        config.jsonOutput().setRelationshipsUseIdsIfAvailable(false);
        config.jsonOutput().setConvertFieldsToDefinedTypes(false);
        config.setApiToEnforceDeclaredTypesInInput(false);

        ThingifierApiConfigProfile profile2 = todoManager.apiConfigProfiles().
                create("v2", "compressed relationships with guids");
        ThingifierApiConfig config2 = profile2.apiConfig();
        config2.setUrlToShowIdsInUrlsIfAvailable(false);
        config2.setResponsesToShowIdsIfAvailable(false);
        config2.setUrlToShowSingleInstancesAsPlural(true);
        config2.setResponsesToShowGuids(true);
        config2.jsonOutput().setCompressRelationships(true);
        config2.jsonOutput().setRelationshipsUseIdsIfAvailable(false);
        config2.jsonOutput().setConvertFieldsToDefinedTypes(false);
        config2.setApiToEnforceDeclaredTypesInInput(false);

        ThingifierApiConfigProfile profile3 = todoManager.apiConfigProfiles().
                create("v3", "compressed relationships with ids");
        ThingifierApiConfig config3 = profile3.apiConfig();
        config3.setUrlToShowIdsInUrlsIfAvailable(true);
        config3.setResponsesToShowIdsIfAvailable(true);
        config3.setUrlToShowSingleInstancesAsPlural(true);
        config3.setResponsesToShowGuids(false);
        config3.jsonOutput().setCompressRelationships(true);
        config3.jsonOutput().setRelationshipsUseIdsIfAvailable(true);
        config3.jsonOutput().setConvertFieldsToDefinedTypes(false);
        config3.setApiToEnforceDeclaredTypesInInput(true);

        return todoManager;
    }
}
