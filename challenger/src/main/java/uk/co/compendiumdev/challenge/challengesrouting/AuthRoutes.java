package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.*;

import java.util.List;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteHandler;
import uk.co.compendiumdev.thingifier.adapter.httpserver.SimpleHttpRouteCreator;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.HttpServerRequestToInternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.InternalHttpResponseToHttpServer;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.HttpApiResponseToInternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.InternalHttpRequestToHttpApiRequest;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.*;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BasicAuthHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BearerAuthHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.domain.instances.validation.EntityInstanceStateValidator;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

// TODO: This should be using a Thingifier to do the work of XML JSON etc... like the simulation
public class AuthRoutes {
    private Thingifier secretNoteStore;
    private EntityDefinition secretNote;
    private ThingifierHttpApi httpApi;
    private JsonThing jsonThing;
    private final EntityInstanceStateValidator stateValidator = new EntityInstanceStateValidator();

    public void configure(
            final Challengers challengers, final ThingifierApiDocumentationDefn apiDefn) {
        // authentication and authorisation
        // - create a 'secret' note which can be stored against session using an auth token

        this.secretNoteStore = new Thingifier();

        this.secretNote = this.secretNoteStore.defineThing("secretnote", "secretnotes");

        this.secretNote.addFields(
                Field.is("note", FieldType.STRING)
                        .makeMandatory()
                        .withValidation(new MaximumLengthValidationRule(100))
                        .withDefaultValue(""));

        this.httpApi = new ThingifierHttpApi(this.secretNoteStore);
        this.jsonThing = new JsonThing(this.secretNoteStore.apiConfig().jsonOutput());

        SimpleHttpRouteCreator.addHandler(
                "/secret/token",
                "options",
                (request, result) -> {
                    result.status(204);
                    // disallow POST, DELETE, PATCH, TRACE
                    result.header("Allow", "POST, OPTIONS");
                    return "";
                });

        // TODO: this still feels tightly coupled to HTTP routing; route handling should delegate
        // to an internal auth use case.

        // POST /secret/token with basic auth to get a secret/token to use as X-AUTH-TOKEN header
        // todo: or {username, password} payload
        post(
                "/secret/token",
                (request, result) -> {
                    BasicAuthHeaderParser basicAuth =
                            new BasicAuthHeaderParser(request.header("Authorization"));

                    // admin/password as default username:password
                    if (!basicAuth.matches("admin", "password")) {
                        result.header("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");
                        result.status(401);
                        return "";
                    }

                    ChallengerAuthData challenger =
                            challengers.getChallenger(request.header("X-CHALLENGER"));

                    if (challenger == null) {
                        result.status(401);
                        XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
                        return "";
                    }

                    // if no header X-AUTH-TOKEN then grant one
                    result.header("X-AUTH-TOKEN", challenger.getXAuthToken());
                    result.status(201);
                    return "";
                });

        SimpleHttpRouteCreator.routeStatusWhenNot(405, "/secret/token", List.of("post", "options"));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST,
                                "/secret/token",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "POST /secret/token with basic auth to get a secret/token to use as X-AUTH-TOKEN header, to allow access to the /secret/note end points.")
                        .addPossibleStatuses(201, 401)
                        .secureWithBasicAuth());

        // todo: GET /secret/token returns the secret token or 401 if not authenticated

        // POST /secret/note GET /secret/note - limit note to 100 chars
        // no auth token will receive a 403
        // auth token which does not match the session will receive a 401
        // header X-AUTH-TOKEN: token given - if token not found (then) 401

        SimpleHttpRouteCreator.addHandler(
                "/secret/note",
                "options",
                (request, result) -> {
                    result.status(204);
                    // disallow POST, DELETE, PATCH, TRACE
                    result.header("Allow", "GET, HEAD, POST, OPTIONS");
                    return "";
                });

        HttpRouteHandler getSecretNote =
                (request, result) -> {
                    String authToken = request.header("X-AUTH-TOKEN");
                    final String authorization = request.header("Authorization");

                    result.header("Content-Type", "application/json");

                    ChallengerAuthData challenger =
                            challengers.getChallenger(request.header("X-CHALLENGER"));

                    if (challenger == null) {
                        result.status(401);
                        XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
                        return "";
                    }

                    // authorization bearer token will take precedence over X-AUTH-HEADER
                    if (authorization != null && !authorization.isEmpty()) {
                        final BearerAuthHeaderParser bearerToken =
                                new BearerAuthHeaderParser(authorization);
                        if (bearerToken.isBearerToken() && bearerToken.isValid()) {
                            authToken = bearerToken.getToken();
                        }
                    }

                    if (authToken == null || authToken.isEmpty()) {
                        result.status(401);
                        return "";
                    }

                    if (!authToken.contentEquals(challenger.getXAuthToken())) {
                        result.status(403); // given token is not allowed to access anything
                        return "";
                    }

                    AcceptHeaderParser acceptHeaderParser =
                            new AcceptHeaderParser(request.header("ACCEPT"));
                    if (!acceptHeaderParser.missingAcceptHeader()
                            && !acceptHeaderParser.isSupportedHeader()) {
                        result.status(406);
                        return "";
                    }

                    final InternalHttpRequest internalRequest =
                            HttpServerRequestToInternalHttpRequest.convert(request);
                    final HttpApiRequest myRequest =
                            InternalHttpRequestToHttpApiRequest.convert(internalRequest);

                    final ApiResponse response =
                            ApiResponse.success()
                                    .returnSingleDraft(
                                            EntityInstanceDraft.forEntity(secretNote)
                                                    .withField("note", challenger.getNote()));

                    final HttpApiResponse httpApiResponse =
                            new HttpApiResponse(
                                    myRequest.getHeaders(),
                                    response,
                                    jsonThing,
                                    this.secretNoteStore.apiConfig());

                    return InternalHttpResponseToHttpServer.convert(
                            HttpApiResponseToInternalHttpResponse.convert(httpApiResponse), result);

                    // return resultBasedOnAcceptHeader(result, request.header("ACCEPT"),
                    // challenger.getNote());
                };

        get(
                "/secret/note",
                (request, result) -> {
                    return getSecretNote.handle(request, result);
                });

        head(
                "/secret/note",
                (request, result) -> {
                    getSecretNote.handle(request, result);
                    return "";
                });

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET,
                                "/secret/note",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "GET /secret/note with X-AUTH-TOKEN to return the secret note for the user.")
                        .addPossibleStatuses(200, 401, 403)
                        .addCustomHeader("X-AUTH-TOKEN", "string"));

        post(
                "/secret/note",
                (request, result) -> {
                    final String authorization = request.header("Authorization");
                    String authToken = request.header("X-AUTH-TOKEN");

                    AcceptHeaderParser acceptHeaderParser =
                            new AcceptHeaderParser(request.header("ACCEPT"));
                    if (!acceptHeaderParser.missingAcceptHeader()
                            && !acceptHeaderParser.isSupportedHeader()) {
                        result.status(406);
                        return "";
                    }

                    ContentTypeHeaderParser contentTypeParser =
                            new ContentTypeHeaderParser(request.header("CONTENT-TYPE"));
                    if (!contentTypeParser.isJSON() && !contentTypeParser.isXML()) {
                        result.status(415);
                        return "";
                    }

                    // todo: if no X-CHALLENGER provided then, search memory for authToken and use
                    // associated
                    //       challenger
                    ChallengerAuthData challenger =
                            challengers.getChallenger(request.header("X-CHALLENGER"));

                    if (challenger == null) {
                        result.status(401);
                        XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
                        return "";
                    }

                    result.header("X-CHALLENGER", challenger.getXChallenger());
                    // set content-type header for error responses
                    if (acceptHeaderParser.hasAPreferenceForXml()) {
                        result.header("Content-Type", "application/xml");
                    } else {
                        result.header("Content-Type", "application/json");
                    }

                    // authorization bearer token will take precedence over X-AUTH-HEADER
                    if (authorization != null && !authorization.isEmpty()) {
                        final BearerAuthHeaderParser bearerToken =
                                new BearerAuthHeaderParser(authorization);
                        if (bearerToken.isBearerToken() && bearerToken.isValid()) {
                            authToken = bearerToken.getToken();
                        }
                    }

                    if (authToken == null || authToken.isEmpty()) {
                        result.status(401);
                        return "";
                    }

                    if (!authToken.contentEquals(challenger.getXAuthToken())) {
                        result.status(403); // given token is not allowed to access anything
                        return "";
                    }

                    if (!acceptHeaderParser.missingAcceptHeader()
                            && !acceptHeaderParser.isSupportedHeader()) {
                        result.status(406);
                        return "";
                    }

                    final InternalHttpRequest internalRequest =
                            HttpServerRequestToInternalHttpRequest.convert(request);
                    final HttpApiRequest myRequest =
                            InternalHttpRequestToHttpApiRequest.convert(internalRequest);
                    HttpApiResponse httpApiResponse =
                            this.httpApi.validateRequestSyntax(
                                    myRequest, ThingifierHttpApi.HttpVerb.POST);

                    // TODO: this should be simpler to use by apps building on thingifier
                    if (httpApiResponse == null) {

                        ApiResponse response =
                                this.secretNoteStore
                                        .api()
                                        .post(
                                                "secretnote",
                                                new BodyParser(
                                                        myRequest,
                                                        this.secretNoteStore.getThingNames()),
                                                myRequest.getHeaders());
                        if (!response.isErrorResponse()) {

                            EntityInstance returnedInstance = response.getReturnedInstance();
                            final List<String> protectedFieldNames =
                                    returnedInstance
                                            .getEntity()
                                            .getFieldNamesOfType(
                                                    FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
                            ValidationReport validity =
                                    stateValidator.validateFields(
                                            returnedInstance, protectedFieldNames, false);
                            validity.combine(
                                    secretNoteStore
                                            .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                                            .relationships()
                                            .validate(returnedInstance));

                            this.secretNoteStore.deleteThing(
                                    response.getReturnedInstance(),
                                    EntityRelModel.DEFAULT_DATABASE_NAME);

                            if (!validity.isValid()) {
                                response =
                                        ApiResponse.error(400, validity.getCombinedErrorMessages());
                            } else {
                                final EntityInstance postedThing = response.getReturnedInstance();
                                response = ApiResponse.success().returnSingleInstance(postedThing);
                                challenger.setNote(
                                        response.getReturnedInstance()
                                                .getFieldValue("note")
                                                .asString());
                            }
                        }

                        httpApiResponse =
                                new HttpApiResponse(
                                        myRequest.getHeaders(),
                                        response,
                                        jsonThing,
                                        this.secretNoteStore.apiConfig());
                    }

                    return InternalHttpResponseToHttpServer.convert(
                            HttpApiResponseToInternalHttpResponse.convert(httpApiResponse), result);
                });

        SimpleHttpRouteCreator.routeStatusWhenNot(
                405, "/secret/note", List.of("get", "post", "head", "options"));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST,
                                "/secret/note",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "POST /secret/note with X-AUTH-TOKEN, and a payload of `{'note':'contents of note'}` to amend the contents of the secret note.")
                        .addPossibleStatuses(200, 400, 401, 403)
                        .addCustomHeader("X-AUTH-TOKEN", "string"));
    }
}
