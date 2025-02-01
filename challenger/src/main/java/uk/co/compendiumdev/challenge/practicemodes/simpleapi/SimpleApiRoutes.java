package uk.co.compendiumdev.challenge.practicemodes.simpleapi;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengesrouting.XChallengerHeader;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BasicAuthHeaderParser;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httprouting.ThingifierAutoDocGenRouting;
import uk.co.compendiumdev.thingifier.application.httprouting.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MatchesRegexValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.routing.DefaultGuiRoutings;
import uk.co.compendiumdev.thingifier.spark.SimpleSparkRouteCreator;

import java.util.List;

import static spark.Spark.*;

/*
    The simple API is a no-auth API where anyone can amend, create, delete items.

    To make this Safe all the fields will be primitives and no Strings.

 */
public class SimpleApiRoutes {

    private final DefaultGUIHTML guiTemplates;
    public Thingifier simplethings;
    public EntityDefinition entityDefn;
    private ThingifierApiDocumentationDefn apiDocDefn;
    private ThingifierAutoDocGenRouting simpleApiDocsRouting;
    private ThingifierHttpApiRoutings simpleApiHttpRouting;
    private DefaultGuiRoutings simpleApiGuiRouting;

    public SimpleApiRoutes(DefaultGUIHTML guiTemplates){
        // fake the data storage
        this.simplethings = new Thingifier();

        simplethings.setDocumentation(
                "Simple API Mode",
            "A simple API, no auth protection so you can add and delete what you want in a multi-user mode.");
        this.entityDefn = simplethings.defineThing("item", "items", 100);

        // TODO: add descriptions on a field level to explain what they are and show this in documentation
        this.entityDefn.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        this.entityDefn.addFields(
                Field.is("type", FieldType.ENUM).
                        makeMandatory().
                        withExample("book").
                        withExample("blu-ray").
                        withExample("cd").
                        withExample("dvd"),
                Field.is("isbn13", FieldType.STRING).
                        makeMandatory().
                        withValidation(new MatchesRegexValidationRule("[0-9]{3}[-]?[0-9]{1}[-]?[0-9]{2}[-]?[0-9]{6}[-]?[0-9]{1}")).
                        withValidation(new MaximumLengthValidationRule(17)).
                        setMustBeUnique(true).
                        setUniqueAfterTransform((s) -> s.replace("-","")).
                        withExample("123-4-56-789012-3"),
                Field.is("price",FieldType.FLOAT).
                        makeMandatory().
                        withExample("97.99").
                        withMinMaxValues(0f, 50000.0f),
                Field.is("numberinstock", FieldType.INTEGER).
                        withDefaultValue("0").
                        withMinMaxValues(0,100)
        );

        simplethings.setDataGenerator(new SimpleAPITestDataPopulator());

        simplethings.apiConfig().setFrom(new ThingifierApiConfig("/simpleapi"));
        // do  not convert floats to int
        simplethings.apiConfig().setApiToEnforceDeclaredTypesInInput(false);
        // single items should be single items
        simplethings.apiConfig().setReturnSingleGetItemsAsCollection(false);

        // TODO: should probably have a support multiple databases config somewhere
        simplethings.getERmodel().populateDatabase(EntityRelModel.DEFAULT_DATABASE_NAME);

        this.guiTemplates = guiTemplates;

    }

    public void configure() {

        DefaultGUIHTML gui = guiTemplates;

        simpleApiGuiRouting = new DefaultGuiRoutings(simplethings, gui).
                configureRoutes("/simpleapi/gui");

        apiDocDefn = new ThingifierApiDocumentationDefn();
        apiDocDefn.addServer("https://apichallenges.eviltester.com", "cloud hosted version");
        apiDocDefn.addServer("http://localhost:4567", "local execution");
        apiDocDefn.setVersion("1.0.0");
        apiDocDefn.setThingifier(simplethings);
        apiDocDefn.setPathPrefix("/simpleapi"); // where can the API endpoints be found




        new SimpleSparkRouteCreator("/simpleapi/items").status(501, List.of("patch", "trace"));


        get("/simpleapi/randomisbn", (request, result) -> {
            result.status(200);
            result.header("content-type", "text/plain");
            return RandomIsbnGenerator.generate();
        });

        options("/simpleapi/randomisbn", (request, result) -> {
            result.status(200);
            result.header("content-type", "text/plain");
            result.header("Allow", "GET, HEAD, OPTIONS");
            return "";
        });

        head("/simpleapi/randomisbn", (request, result) -> {
            result.status(200);
            result.header("content-type", "text/plain");
            return "";
        });


        apiDocDefn.addRouteToDocumentation(
            new RoutingDefinition(
                    RoutingVerb.GET,
                    "/simpleapi/randomisbn",
                    RoutingStatus.returnedFromCall(),
                    null).addDocumentation("return a random ISBN that can be used for testing purposes with the Simple API.").
                    addPossibleStatuses(200)
        );


        new SimpleSparkRouteCreator("/simpleapi/randomisbn").status(405, List.of("put", "post", "delete", "patch", "trace"));





        simpleApiDocsRouting = new ThingifierAutoDocGenRouting(
                simplethings,
                apiDocDefn,
                gui);

        simpleApiHttpRouting = new ThingifierHttpApiRoutings(simplethings, apiDocDefn);

        simpleApiHttpRouting.registerHttpApiRequestHook(new AddMoreItemsIfNecessary(simplethings.getERmodel()));
        simpleApiHttpRouting.registerHttpApiRequestHook(new ResetAutoIncrementWhenTooHigh(simplethings.getERmodel()));
        simpleApiHttpRouting.registerInternalHttpResponseHook(new SimpleApiCorsHeadersResponseHook());
    }
}
