package uk.co.compendiumdev.thingifier.api.http;

import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

/**
 * Parsed request data passed from HTTP handling into direct Thingifier API handlers.
 *
 * <p>The envelope preserves both raw request data and parsed body/query data so lifecycle hooks can
 * intentionally replace either form before the mapped read or write operation runs.
 */
public final class ApiRequestEnvelope {

    /** Identifies how a QUERY request body should be interpreted. */
    public enum QueryBodyFormat {
        /** Query parameters encoded as a form-style key/value body. */
        URL_ENCODED,
        /** JSONPath expression used to filter the mapped query result. */
        JSONPATH,
        /** Thingifier structured query JSON document. */
        STRUCTURED_JSON
    }

    private final ThingifierHttpApi.HttpVerb verb;
    private final String path;
    private final QueryFilterParams queryParams;
    private final HttpHeadersBlock headers;
    private final ApiBodyFields bodyFields;
    private final String body;
    private final QueryBodyFormat queryBodyFormat;

    private ApiRequestEnvelope(
            final ThingifierHttpApi.HttpVerb verb,
            final String path,
            final QueryFilterParams queryParams,
            final HttpHeadersBlock headers,
            final ApiBodyFields bodyFields,
            final String body,
            final QueryBodyFormat queryBodyFormat) {
        this.verb = verb;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.bodyFields = bodyFields;
        this.body = body == null ? "" : body;
        this.queryBodyFormat = queryBodyFormat;
    }

    /**
     * Parses an HTTP API request into a handler envelope.
     *
     * <p>POST and PUT bodies are parsed to field values immediately because write handlers consume
     * fields. QUERY requests keep enough raw data to support URL-encoded, JSONPath, and structured
     * JSON query bodies.
     *
     * @param request HTTP API request
     * @param verb effective HTTP API verb
     * @param thingNames XML entity names used by body parsing
     * @return parsed request envelope
     */
    public static ApiRequestEnvelope from(
            final HttpApiRequest request,
            final ThingifierHttpApi.HttpVerb verb,
            final List<String> thingNames) {
        ApiBodyFields bodyFields = ApiBodyFields.empty();
        if (verb == ThingifierHttpApi.HttpVerb.POST || verb == ThingifierHttpApi.HttpVerb.PUT) {
            bodyFields = new BodyParser(request, thingNames).bodyFields();
        }
        QueryFilterParams queryParams = request.getFilterableQueryParams();
        QueryBodyFormat queryBodyFormat = QueryBodyFormat.URL_ENCODED;
        if (verb == ThingifierHttpApi.HttpVerb.QUERY) {
            queryBodyFormat = queryBodyFormatFor(request);
            if (queryBodyFormat == QueryBodyFormat.URL_ENCODED) {
                queryParams = queryContentAndUriQueryParams(request);
            }
        }
        return new ApiRequestEnvelope(
                verb,
                request.getPath(),
                queryParams,
                request.getHeaders(),
                bodyFields,
                request.getBody(),
                queryBodyFormat);
    }

    /**
     * Creates an envelope from already parsed request parts.
     *
     * <p>Lifecycle hooks use this when a previous phase has replaced query parameters, body fields,
     * raw body text, or query body format.
     *
     * @param verb effective HTTP API verb
     * @param path generated API path
     * @param queryParams query filters to expose to handlers
     * @param headers request headers
     * @param bodyFields parsed request body fields
     * @param body raw request body
     * @param queryBodyFormat QUERY body interpretation
     * @return request envelope with null-safe defaults
     */
    public static ApiRequestEnvelope fromParsed(
            final ThingifierHttpApi.HttpVerb verb,
            final String path,
            final QueryFilterParams queryParams,
            final HttpHeadersBlock headers,
            final ApiBodyFields bodyFields,
            final String body,
            final QueryBodyFormat queryBodyFormat) {
        return new ApiRequestEnvelope(
                verb,
                path,
                queryParams == null ? new QueryFilterParams() : queryParams,
                headers == null ? new HttpHeadersBlock() : headers,
                bodyFields == null ? ApiBodyFields.empty() : bodyFields,
                body,
                queryBodyFormat == null ? QueryBodyFormat.URL_ENCODED : queryBodyFormat);
    }

    /**
     * Chooses the QUERY body format from the request Content-Type.
     *
     * @param request HTTP API request
     * @return query body format used by query handling
     */
    private static QueryBodyFormat queryBodyFormatFor(final HttpApiRequest request) {
        final ContentTypeHeaderParser contentType =
                new ContentTypeHeaderParser(request.getContentTypeHeader());
        if (contentType.isJsonPath()) {
            return QueryBodyFormat.JSONPATH;
        }
        if (contentType.isStructuredQueryJson()) {
            return QueryBodyFormat.STRUCTURED_JSON;
        }
        return QueryBodyFormat.URL_ENCODED;
    }

    /**
     * Combines URI query parameters with URL-encoded QUERY body parameters.
     *
     * @param request HTTP API request
     * @return merged query filter parameters
     */
    private static QueryFilterParams queryContentAndUriQueryParams(final HttpApiRequest request) {
        QueryFilterParams params = new QueryFilterParams();
        params.addAll(request.getFilterableQueryParams());
        params.addAll(new UrlQueryParamParser().parseStrict(request.getBody()));
        return params;
    }

    /**
     * Returns the effective verb for the request.
     *
     * @return HTTP API verb used by handlers
     */
    public ThingifierHttpApi.HttpVerb verb() {
        return verb;
    }

    /**
     * Returns the normalized generated API path.
     *
     * @return request path
     */
    public String path() {
        return path;
    }

    /**
     * Returns query filter parameters visible to handlers.
     *
     * @return query filters
     */
    public QueryFilterParams queryParams() {
        return queryParams;
    }

    /**
     * Returns request headers used for context and content negotiation.
     *
     * @return request headers
     */
    public HttpHeadersBlock headers() {
        return headers;
    }

    /**
     * Returns parsed request body fields.
     *
     * @return parsed body fields, or an empty field set
     */
    public ApiBodyFields bodyFields() {
        return bodyFields;
    }

    /**
     * Returns the raw request body.
     *
     * @return raw body text, never null
     */
    public String body() {
        return body;
    }

    /**
     * Returns the format selected for a QUERY body.
     *
     * @return query body format
     */
    public QueryBodyFormat queryBodyFormat() {
        return queryBodyFormat;
    }
}
