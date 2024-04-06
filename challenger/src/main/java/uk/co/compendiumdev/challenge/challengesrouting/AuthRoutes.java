package uk.co.compendiumdev.challenge.challengesrouting;

import spark.Route;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BasicAuthHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BearerAuthHeaderParser;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.*;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.ThingCreation;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.HttpApiResponseToSpark;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.SparkToHttpApiRequest;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.spark.SimpleSparkRouteCreator;

import java.util.List;

import static spark.Spark.*;

// TODO: This should be using a Thingifier to do the work of XML JSON etc... like the simulation
public class AuthRoutes {
    private Thingifier secretNoteStore;
    private EntityDefinition secretNote;
    private ThingifierHttpApi httpApi;
    private JsonThing jsonThing;

    public void configure(final Challengers challengers,
                          final ThingifierApiDocumentationDefn apiDefn) {
        // authentication and authorisation
        // - create a 'secret' note which can be stored against session using an auth token

        this.secretNoteStore = new Thingifier();

        this.secretNote = this.secretNoteStore.defineThing("secretnote", "secretnotes");

        this.secretNote.addFields(
                Field.is("note", FieldType.STRING).
                        makeMandatory().
                        withValidation(new MaximumLengthValidationRule(100)).
                        withDefaultValue("")
        );

        this.httpApi = new ThingifierHttpApi(this.secretNoteStore);
        this.jsonThing = new JsonThing(this.secretNoteStore.apiConfig().jsonOutput());


        SimpleSparkRouteCreator.addHandler("/secret/token", "options", (request, result) ->{
            result.status(204);
            // disallow POST, DELETE, PATCH, TRACE
            result.header("Allow", "POST, OPTIONS");
            return "";
        });


        // TODO: this all feels too tightly coupled to SparkJava we should have our own routing internally that spark delegates too

        // POST /secret/token with basic auth to get a secret/token to use as X-AUTH-TOKEN header
        // todo: or {username, password} payload
        post("/secret/token", (request, result) -> {

            BasicAuthHeaderParser basicAuth = new BasicAuthHeaderParser(request.headers("Authorization"));

            // admin/password as default username:password
            if(!basicAuth.matches("admin","password")){
                result.raw().setHeader("WWW-Authenticate","Basic realm=\"User Visible Realm\"");
                result.status(401);
                return "";
            }

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
                return "";
            }

            // if no header X-AUTH-TOKEN then grant one
            result.raw().setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
            result.status(201);
            return "";
        });

        SimpleSparkRouteCreator.routeStatusWhenNot(405, "/secret/token", List.of("post", "options"));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                RoutingVerb.POST,
                "/secret/token",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("POST /secret/token with basic auth to get a secret/token to use as X-AUTH-TOKEN header, to allow access to the /secret/note end points.").
                    addPossibleStatuses(201,401));










        // todo: GET /secret/token returns the secret token or 401 if not authenticated


        // POST /secret/note GET /secret/note - limit note to 100 chars
        // no auth token will receive a 403
        // auth token which does not match the session will receive a 401
        // header X-AUTH-TOKEN: token given - if token not found (then) 401

        SimpleSparkRouteCreator.addHandler("/secret/note", "options", (request, result) ->{
            result.status(204);
            // disallow POST, DELETE, PATCH, TRACE
            result.header("Allow", "GET, HEAD, POST, OPTIONS");
            return "";
        });


        Route getSecretNote = (request, result) -> {
            String authToken = request.headers("X-AUTH-TOKEN");
            final String authorization = request.headers("Authorization");

            result.header("Content-Type", "application/json");

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                XChallengerHeader.setResultHeaderBasedOnChallenger(result,challenger);
                return "";
            }

            // authorization bearer token will take precedence over X-AUTH-HEADER
            if(authorization!=null && !authorization.isEmpty()){
                final BearerAuthHeaderParser bearerToken = new BearerAuthHeaderParser(authorization);
                if(bearerToken.isBearerToken() && bearerToken.isValid()){
                    authToken = bearerToken.getToken();
                }
            }

            if(authToken==null || authToken.isEmpty()){
                result.status(401);
                return "";
            }

            if(!authToken.contentEquals(challenger.getXAuthToken())){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            AcceptHeaderParser acceptHeaderParser = new AcceptHeaderParser(request.headers("ACCEPT"));
            if(!acceptHeaderParser.missingAcceptHeader() && !acceptHeaderParser.isSupportedHeader()){
                result.status(406);
                return "";
            }

            final HttpApiRequest myRequest = SparkToHttpApiRequest.convert(request);

            EntityInstance note = new EntityInstance(secretNote).setValue("note", challenger.getNote());
            final ApiResponse response = ApiResponse.success().returnSingleInstance(note);

            final HttpApiResponse httpApiResponse = new HttpApiResponse(myRequest.getHeaders(), response,
                    jsonThing, this.secretNoteStore.apiConfig());

            return HttpApiResponseToSpark.convert(httpApiResponse, result);

            //return resultBasedOnAcceptHeader(result, request.headers("ACCEPT"), challenger.getNote());
        };

        get("/secret/note", (request, result) -> {
            return getSecretNote.handle(request,result);
        });

        head("/secret/note", (request, result) -> {
            getSecretNote.handle(request,result);
            return "";
        });

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                RoutingVerb.GET,
                "/secret/note",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation(
                        "GET /secret/note with X-AUTH-TOKEN to return the secret note for the user.").
                        addPossibleStatuses(200,401,403));


        post("/secret/note", (request, result) -> {

            final String authorization = request.headers("Authorization");
            String authToken = request.headers("X-AUTH-TOKEN");

            AcceptHeaderParser acceptHeaderParser = new AcceptHeaderParser(request.headers("ACCEPT"));
            if(!acceptHeaderParser.missingAcceptHeader() && !acceptHeaderParser.isSupportedHeader()){
                result.status(406);
                return "";
            }

            ContentTypeHeaderParser contentTypeParser = new ContentTypeHeaderParser(request.headers("CONTENT-TYPE"));
            if(!contentTypeParser.isJSON() && !contentTypeParser.isXML()){
                result.status(415);
                return "";
            }

            // todo: if no X-CHALLENGER provided then, search memory for authToken and use associated
            //       challenger
            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                XChallengerHeader.setResultHeaderBasedOnChallenger(result,challenger);
                return "";
            }

            result.raw().setHeader("X-CHALLENGER", challenger.getXChallenger());
            // set content-type header for error responses
            if(acceptHeaderParser.hasAPreferenceForXml()){
                result.header("Content-Type", "application/xml");
            }else{
                result.header("Content-Type", "application/json");
            }

            // authorization bearer token will take precedence over X-AUTH-HEADER
            if(authorization!=null && !authorization.isEmpty()){
                final BearerAuthHeaderParser bearerToken = new BearerAuthHeaderParser(authorization);
                if(bearerToken.isBearerToken() && bearerToken.isValid()){
                    authToken = bearerToken.getToken();
                }
            }

            if(authToken==null || authToken.isEmpty()){
                result.status(401);
                return "";
            }

            if(!authToken.contentEquals(challenger.getXAuthToken())){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            if(!acceptHeaderParser.missingAcceptHeader() && !acceptHeaderParser.isSupportedHeader()){
                result.status(406);
                return "";
            }

            final HttpApiRequest myRequest = SparkToHttpApiRequest.convert(request);
            HttpApiResponse httpApiResponse = this.httpApi.validateRequestSyntax(myRequest,
                    ThingifierHttpApi.HttpVerb.POST);

            // TODO: this should be simpler to use by apps building on thingifier
            if(httpApiResponse==null) {

                ApiResponse response=null;
                response = new ThingCreation(this.secretNoteStore).with(
                        new BodyParser(myRequest, List.of("secretnote")),
                        this.secretNoteStore.getThingInstancesNamed("secretnote", EntityRelModel.DEFAULT_DATABASE_NAME), EntityRelModel.DEFAULT_DATABASE_NAME);
                if (!response.isErrorResponse()) {

                    EntityInstance returnedInstance = response.getReturnedInstance();
                    final List<String> protectedFieldNames = returnedInstance.getEntity().getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
                    ValidationReport validity = returnedInstance.validateFieldValues(protectedFieldNames, false);
                    validity.combine(returnedInstance.validateRelationships());

                    this.secretNoteStore.deleteThing(response.getReturnedInstance(), EntityRelModel.DEFAULT_DATABASE_NAME);

                    if (!validity.isValid()) {
                        response = ApiResponse.error(400, validity.getCombinedErrorMessages());
                    }else{
                        final EntityInstance postedThing = response.getReturnedInstance();
                        response = ApiResponse.success().returnSingleInstance(postedThing);
                        challenger.setNote(response.getReturnedInstance().
                                            getFieldValue("note").asString());
                    }
                }

                httpApiResponse = new HttpApiResponse(myRequest.getHeaders(), response,
                        jsonThing, this.secretNoteStore.apiConfig());
            }


            return HttpApiResponseToSpark.convert(httpApiResponse, result);

        });

        SimpleSparkRouteCreator.routeStatusWhenNot(405, "/secret/note", List.of("get", "post", "head", "options"));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                RoutingVerb.POST,
                "/secret/note",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("POST /secret/note with X-AUTH-TOKEN, and a payload of `{'note':'contents of note'}` to amend the contents of the secret note.").
                addPossibleStatuses(200,400,401,403));
    }


}
