package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.*;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public class ThingifierRestAPIHandler {
    private final RestApiDeleteHandler delete;
    private final RestApiPostHandler post;
    private final RestApiPutHandler put;
    private final RestApiGetHandler get;

    public ThingifierRestAPIHandler(final Thingifier aThingifier) {
        this.get = new RestApiGetHandler(aThingifier);
        this.delete = new RestApiDeleteHandler(aThingifier);
        this.post = new RestApiPostHandler(aThingifier);
        this.put = new RestApiPutHandler(aThingifier);
    }


    // TODO: we should be able to accept xml with correct content type
    // TODO: we should be able to accept html forms with correct content type
    // todo allow an accept text/html to create different output - (probably handled by routings rather than api)
    // todo : generate examples when outputing the api documentation

    // TODO: - listed here https://www.lisihocke.com/2018/07/testing-tour-stop-16-pair-exploring-an-api-with-thomas.html
    // TODO: ensure that relationshps enforce the type of thing e.g. if I pass in a GUID of the wrong type then it should not cross ref
    // TODO: possibly consider an X- header which has the number of items in the collection


    public ApiResponse get(final String url, final QueryFilterParams queryParams, HttpHeadersBlock headers) {
        return get.handle(url, queryParams, headers);
    }

    public ApiResponse head(final String url, final QueryFilterParams queryParams, HttpHeadersBlock headers) {
        final ApiResponse response = get.handle(url, queryParams, headers);
        response.clearBody();
        return response;
    }

    public ApiResponse delete(final String url, HttpHeadersBlock headers) {
        return delete.handle(url, headers);
    }

    public ApiResponse post(final String url, final BodyParser args, HttpHeadersBlock headers) {
        return post.handle(url, args, headers);
    }

    public ApiResponse put(final String url, final BodyParser args, HttpHeadersBlock headers) {
        return put.handle(url, args, headers);
    }



}
