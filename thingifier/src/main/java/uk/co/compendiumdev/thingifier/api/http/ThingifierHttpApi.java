package uk.co.compendiumdev.thingifier.api.http;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

public final class ThingifierHttpApi {

    // TODO: each 'session' could have its own thingifier to support multiple users
    // TODO: would need the ability to create and delete sessions
    public static final String HTTP_SESSION_HEADER_NAME = "X-THING-HTTP-SESSION-GUID";
    public static final String ACCEPT_QUERY_HEADER = "Accept-Query";
    public static final String QUERY_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final Thingifier thingifier;
    private final JsonThing jsonThing;
    private List<HttpApiRequestHook> apiRequestHooks;
    private List<HttpApiResponseHook> apiResponseHooks;

    public enum HttpVerb {
        GET,
        DELETE,
        POST,
        PUT,
        HEAD,
        QUERY,
        // NOT Handled
        OPTIONS,
        PATCH,
        TRACE
    };

    public ThingifierHttpApi(final Thingifier aThingifier) {
        this(aThingifier, null, null);
    }

    public ThingifierHttpApi(
            final Thingifier aThingifier,
            List<HttpApiRequestHook> apiRequestHooks,
            List<HttpApiResponseHook> apiResponseHooks) {
        this.thingifier = aThingifier;

        // request hooks are used to do initial processing and possibly prevent processing
        if (apiRequestHooks == null) {
            this.apiRequestHooks = new ArrayList<>();
        } else {
            this.apiRequestHooks = apiRequestHooks;
        }

        // response hooks are used after the main API processing and possibly override values
        if (apiResponseHooks == null) {
            this.apiResponseHooks = new ArrayList<>();
        } else {
            this.apiResponseHooks = apiResponseHooks;
        }

        jsonThing = new JsonThing(thingifier.apiConfig().jsonOutput());
    }

    private HttpApiResponse handleRequest(final HttpApiRequest request, HttpVerb verb) {

        // if the request.url has the 'prefix' then remove the prefix and process the request
        // if(request.getPath())

        String prefix = thingifier.apiConfig().getApiEndPointPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            if (prefix.startsWith("/")) {
                prefix = prefix.substring(1);
            }
            request.removePrefixFromPath(prefix);
        }

        // any pre-request override processing
        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);
        final HttpVerb effectiveVerb = MethodOverrideParser.getEffectiveVerb(request, verb);

        // TODO: consider 'validation' hooks which can be used to override/augment validation

        // validate request syntax
        if (httpResponse == null) {
            httpResponse = validateRequestSyntax(request, effectiveVerb);
        }

        // TODO: consider 'processing' hooks which can be used to override the generic processing

        // no httpResponse generated after validation so it is not in error
        if (httpResponse == null) {
            ApiResponse apiResponse = routeAndProcessRequest(request, effectiveVerb);

            httpResponse =
                    new HttpApiResponse(
                            request.getHeaders(), apiResponse, jsonThing, thingifier.apiConfig());

            if (effectiveVerb == HttpVerb.HEAD) {
                final int bodyLength =
                        httpResponse.getBody().getBytes(StandardCharsets.UTF_8).length;
                apiResponse.clearBody();
                apiResponse.setHeader("Content-Length", Integer.toString(bodyLength));
                httpResponse =
                        new HttpApiResponse(
                                request.getHeaders(),
                                apiResponse,
                                jsonThing,
                                thingifier.apiConfig());
            }
        }

        // run any post processing response hooks
        return runTheHttpApiResponseHooksOn(request, httpResponse);
    }

    /** return an error response if the request is invalid, null if valid */
    public HttpApiResponse validateRequestSyntax(
            final HttpApiRequest request, final HttpVerb verb) {

        final HttpApiRequestValidator requestValidator =
                new HttpApiRequestValidator(thingifier.apiConfig());

        HttpApiResponse httpResponse = null;

        if (!requestValidator.validateSyntax(request, verb)) {

            httpResponse =
                    new HttpApiResponse(
                            request.getHeaders(),
                            requestValidator.getErrorApiResponse(),
                            jsonThing,
                            thingifier.apiConfig());
        }

        return httpResponse;
    }

    public ApiResponse routeAndProcessRequest(final HttpApiRequest request, HttpVerb verb) {

        ApiResponse apiResponse = null;
        ApiRequestEnvelope envelope =
                ApiRequestEnvelope.from(request, verb, thingifier.getThingNames());

        switch (verb) {
            case GET:
                apiResponse = thingifier.api().get(envelope);
                break;
            case HEAD:
                apiResponse = thingifier.api().get(envelope);
                break;
            case QUERY:
                apiResponse = thingifier.api().query(envelope);
                break;
            case DELETE:
                apiResponse = thingifier.api().delete(envelope);
                break;
            case POST:
                apiResponse = thingifier.api().post(envelope);
                break;
            case PUT:
                apiResponse = thingifier.api().put(envelope);
                break;
            default:
                break;
        }

        return apiResponse;
    }

    public HttpApiResponse get(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.GET);
    }

    public HttpApiResponse head(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.HEAD);
    }

    public HttpApiResponse delete(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.DELETE);
    }

    public HttpApiResponse post(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.POST);
    }

    public HttpApiResponse put(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.PUT);
    }

    public HttpApiResponse queryRequest(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.QUERY);
    }

    public HttpApiResponse query(final HttpApiRequest request, final String query) {

        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        if (httpResponse == null) {
            ApiResponse apiResponse =
                    thingifier
                            .api()
                            .get(query, request.getFilterableQueryParams(), request.getHeaders());
            httpResponse =
                    new HttpApiResponse(
                            request.getHeaders(), apiResponse, jsonThing, thingifier.apiConfig());
        }

        return runTheHttpApiResponseHooksOn(request, httpResponse);
    }

    private HttpApiResponse runTheHttpApiResponseHooksOn(
            final HttpApiRequest request, final HttpApiResponse response) {
        for (HttpApiResponseHook hook : apiResponseHooks) {
            HttpApiResponse returnImmediately = hook.run(request, response, thingifier.apiConfig());
            if (returnImmediately != null) {
                return returnImmediately;
            }
        }
        return response;
    }

    private HttpApiResponse runTheHttpApiRequestHooksOn(final HttpApiRequest request) {
        for (HttpApiRequestHook hook : apiRequestHooks) {
            HttpApiResponse response = hook.run(request, thingifier.apiConfig());
            if (response != null) {
                return response;
            }
        }
        return null;
    }
}
