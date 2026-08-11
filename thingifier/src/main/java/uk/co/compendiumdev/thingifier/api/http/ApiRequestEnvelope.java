package uk.co.compendiumdev.thingifier.api.http;

import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class ApiRequestEnvelope {

    public enum QueryBodyFormat {
        URL_ENCODED,
        JSONPATH
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

    private static QueryBodyFormat queryBodyFormatFor(final HttpApiRequest request) {
        final ContentTypeHeaderParser contentType =
                new ContentTypeHeaderParser(request.getContentTypeHeader());
        if (contentType.isJsonPath()) {
            return QueryBodyFormat.JSONPATH;
        }
        return QueryBodyFormat.URL_ENCODED;
    }

    private static QueryFilterParams queryContentAndUriQueryParams(final HttpApiRequest request) {
        QueryFilterParams params = new QueryFilterParams();
        params.addAll(request.getFilterableQueryParams());
        params.addAll(new UrlQueryParamParser().parseStrict(request.getBody()));
        return params;
    }

    public ThingifierHttpApi.HttpVerb verb() {
        return verb;
    }

    public String path() {
        return path;
    }

    public QueryFilterParams queryParams() {
        return queryParams;
    }

    public HttpHeadersBlock headers() {
        return headers;
    }

    public ApiBodyFields bodyFields() {
        return bodyFields;
    }

    public String body() {
        return body;
    }

    public QueryBodyFormat queryBodyFormat() {
        return queryBodyFormat;
    }
}
