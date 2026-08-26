package uk.co.compendiumdev.thingifier.api.callbacks;

import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

/**
 * Immutable final HTTP response details supplied to route {@code afterResponse} callbacks.
 *
 * <p>This object is created after Thingifier has applied route response policies, response views,
 * and HTTP content negotiation. It deliberately exposes the HTTP response as observed by the
 * caller, not just the internal structured {@link ApiResponse}.
 */
public final class ThingifierApiFinalResponse {

    private final int statusCode;
    private final String contentType;
    private final HttpHeadersBlock headers;
    private final String body;
    private final ApiResponse apiResponse;

    private ThingifierApiFinalResponse(
            final int statusCode,
            final String contentType,
            final HttpHeadersBlock headers,
            final String body,
            final ApiResponse apiResponse) {
        this.statusCode = statusCode;
        this.contentType = contentType == null ? "" : contentType;
        this.headers = copyHeaders(headers);
        this.body = body;
        this.apiResponse = apiResponse;
    }

    /**
     * Creates a final response view from Thingifier's rendered HTTP response.
     *
     * @param response rendered HTTP response
     * @param bodyAvailable false when the body should not be exposed, such as HEAD responses
     * @return immutable final response details
     */
    public static ThingifierApiFinalResponse from(
            final HttpApiResponse response, final boolean bodyAvailable) {
        if (response == null) {
            throw new IllegalArgumentException("response is required");
        }
        final HttpHeadersBlock headers = response.getHeaders();
        final String body = bodyAvailable ? response.getBody() : null;
        return new ThingifierApiFinalResponse(
                response.getStatusCode(),
                headers.get("Content-Type"),
                headers,
                body,
                response.apiResponse());
    }

    /**
     * Returns the final HTTP status code.
     *
     * @return HTTP status code visible to the caller
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Returns the final HTTP {@code Content-Type} header after content negotiation.
     *
     * <p>This is the final outbound HTTP header value, not the internal {@link ApiResponse}'s
     * preferred or structured representation.
     *
     * @return final HTTP content type, or an empty string when no content type is present
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Returns a copy of the final HTTP response headers.
     *
     * @return response headers
     */
    public HttpHeadersBlock headers() {
        return copyHeaders(headers);
    }

    /**
     * Returns the final response body when Thingifier has one available for callback inspection.
     *
     * <p>The body may be unavailable for HEAD, streaming, or other no-body responses. When the body
     * is available but empty, the optional contains an empty string.
     *
     * @return optional response body
     */
    public Optional<String> body() {
        return Optional.ofNullable(body);
    }

    /**
     * Returns the structured API response that was rendered into the final HTTP response.
     *
     * @return structured API response
     */
    public ApiResponse apiResponse() {
        return apiResponse;
    }

    private static HttpHeadersBlock copyHeaders(final HttpHeadersBlock original) {
        final HttpHeadersBlock copy = new HttpHeadersBlock();
        if (original != null) {
            copy.putAll(original);
        }
        return copy;
    }
}
