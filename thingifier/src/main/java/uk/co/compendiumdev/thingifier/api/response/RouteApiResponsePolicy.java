package uk.co.compendiumdev.thingifier.api.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;

/**
 * Declarative response shaping for one route outcome.
 *
 * <p>A route response policy belongs to the API contract rather than the entity model. It lets a
 * route keep Thingifier's generated operation handling while changing the outward HTTP-style
 * response shape for compatibility or documented route semantics. Policies run after the generated
 * operation has produced an {@link ApiResponse} and before that response is rendered.
 */
public final class RouteApiResponsePolicy {

    /** Describes how the policy should handle the response body. */
    public enum BodyAction {
        /** Leave the generated Thingifier response body unchanged. */
        PRESERVE,
        /** Remove the response body while preserving status and headers. */
        SUPPRESS,
        /** Replace the response body with explicit text. */
        TEXT,
        /** Render the existing returned entity data through a named entity view. */
        ENTITY_VIEW
    }

    private Integer statusCode;
    private final List<HeaderValue> staticHeaders;
    private final List<String> removedHeaders;
    private final List<InstanceFieldHeader> instanceFieldHeaders;
    private final List<RequestCondition> requestConditions;
    private BodyAction bodyAction;
    private String bodyText;
    private String entityViewName;

    /** Creates an empty policy that preserves Thingifier's generated response. */
    public RouteApiResponsePolicy() {
        statusCode = null;
        staticHeaders = new ArrayList<>();
        removedHeaders = new ArrayList<>();
        instanceFieldHeaders = new ArrayList<>();
        requestConditions = new ArrayList<>();
        bodyAction = BodyAction.PRESERVE;
        bodyText = null;
        entityViewName = null;
    }

    /**
     * Overrides the response status code for this route outcome.
     *
     * @param statusCode HTTP-style status code to expose
     * @return this policy so response actions can be chained
     */
    public RouteApiResponsePolicy status(final int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Adds or replaces a static response header.
     *
     * @param name header name
     * @param value header value, with null treated as an empty value
     * @return this policy so response actions can be chained
     * @throws IllegalArgumentException when the header name is blank
     */
    public RouteApiResponsePolicy header(final String name, final String value) {
        staticHeaders.add(new HeaderValue(requireText(name, "header name"), value));
        return this;
    }

    /**
     * Removes a response header when this policy applies.
     *
     * <p>This is useful for route contracts that need to suppress framework-generated challenge
     * headers, such as browser-facing {@code 401} responses that should not trigger a credential
     * prompt.
     *
     * @param name header name
     * @return this policy so response actions can be chained
     * @throws IllegalArgumentException when the header name is blank
     */
    public RouteApiResponsePolicy removeHeader(final String name) {
        removedHeaders.add(requireText(name, "header name"));
        return this;
    }

    /**
     * Adds a header whose value is read from the single returned instance or draft.
     *
     * <p>The action is deliberately narrow: it does not attempt collection behavior and it does
     * nothing when the named field is not present on the returned instance/draft.
     *
     * @param headerName response header name
     * @param fieldName returned instance/draft field name
     * @return this policy so response actions can be chained
     * @throws IllegalArgumentException when either name is blank
     */
    public RouteApiResponsePolicy addInstanceFieldAsHeader(
            final String headerName, final String fieldName) {
        instanceFieldHeaders.add(
                new InstanceFieldHeader(
                        requireText(headerName, "header name"),
                        requireText(fieldName, "field name")));
        return this;
    }

    /**
     * Applies this policy only when the request header has exactly the expected value.
     *
     * <p>Multiple request conditions are combined with logical AND. Header names are matched using
     * HTTP's case-insensitive rules; values are compared exactly after normal request header
     * parsing.
     *
     * @param headerName request header name
     * @param expectedValue expected request header value, with null treated as an empty value
     * @return this policy so response actions can be chained
     * @throws IllegalArgumentException when the header name is blank
     */
    public RouteApiResponsePolicy whenRequestHeader(
            final String headerName, final String expectedValue) {
        requestConditions.add(
                RequestCondition.headerEquals(
                        requireText(headerName, "header name"),
                        expectedValue == null ? "" : expectedValue));
        return this;
    }

    /**
     * Applies this policy only when the request header is present.
     *
     * @param headerName request header name
     * @return this policy so response actions can be chained
     * @throws IllegalArgumentException when the header name is blank
     */
    public RouteApiResponsePolicy whenRequestHeaderPresent(final String headerName) {
        requestConditions.add(
                RequestCondition.headerPresent(requireText(headerName, "header name")));
        return this;
    }

    /**
     * Applies this policy only when the request header is absent.
     *
     * @param headerName request header name
     * @return this policy so response actions can be chained
     * @throws IllegalArgumentException when the header name is blank
     */
    public RouteApiResponsePolicy whenRequestHeaderMissing(final String headerName) {
        requestConditions.add(
                RequestCondition.headerMissing(requireText(headerName, "header name")));
        return this;
    }

    /**
     * Suppresses the rendered response body while preserving status and headers.
     *
     * @return this policy so response actions can be chained
     */
    public RouteApiResponsePolicy suppressBody() {
        bodyAction = BodyAction.SUPPRESS;
        bodyText = null;
        entityViewName = null;
        return this;
    }

    /**
     * Replaces the rendered response body with plain text.
     *
     * @param body body text, with null treated as an empty body
     * @return this policy so response actions can be chained
     */
    public RouteApiResponsePolicy bodyText(final String body) {
        bodyAction = BodyAction.TEXT;
        bodyText = body == null ? "" : body;
        entityViewName = null;
        return this;
    }

    /**
     * Renders the existing returned entity data through the named response view.
     *
     * <p>This changes only response rendering. It does not mutate any stored entity fields.
     *
     * @param viewName entity view name used for rendering
     * @return this policy so response actions can be chained
     * @throws IllegalArgumentException when the view name is blank
     */
    public RouteApiResponsePolicy bodyUsingEntityView(final String viewName) {
        bodyAction = BodyAction.ENTITY_VIEW;
        bodyText = null;
        entityViewName = requireText(viewName, "entity view name");
        return this;
    }

    /**
     * Returns the configured status override.
     *
     * @return status override, or null when the generated status should be preserved
     */
    public Integer statusCode() {
        return statusCode;
    }

    /**
     * Returns static response headers in declaration order.
     *
     * @return immutable static header actions
     */
    public List<HeaderValue> staticHeaders() {
        return Collections.unmodifiableList(staticHeaders);
    }

    /**
     * Returns response headers removed by this policy.
     *
     * @return immutable header names
     */
    public List<String> removedHeaders() {
        return Collections.unmodifiableList(removedHeaders);
    }

    /**
     * Returns instance-field header actions in declaration order.
     *
     * @return immutable instance-field header actions
     */
    public List<InstanceFieldHeader> instanceFieldHeaders() {
        return Collections.unmodifiableList(instanceFieldHeaders);
    }

    /**
     * Returns request conditions that must match before this policy applies.
     *
     * @return immutable request conditions
     */
    public List<RequestCondition> requestConditions() {
        return Collections.unmodifiableList(requestConditions);
    }

    /**
     * Reports whether this policy should apply to the supplied request headers.
     *
     * <p>A policy with no request conditions matches every request. Conditions are deliberately
     * request-only so response policy selection stays deterministic and does not depend on later
     * body rendering.
     *
     * @param requestHeaders request headers from the active API call
     * @return true when every configured request condition matches
     */
    public boolean matchesRequest(final HttpHeadersBlock requestHeaders) {
        final HttpHeadersBlock headers =
                requestHeaders == null ? new HttpHeadersBlock() : requestHeaders;
        for (RequestCondition condition : requestConditions) {
            if (!condition.matches(headers)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the configured body action.
     *
     * @return body action, defaulting to preserve
     */
    public BodyAction bodyAction() {
        return bodyAction;
    }

    /**
     * Returns the configured plain text body.
     *
     * @return body text, or null when not configured
     */
    public String bodyText() {
        return bodyText;
    }

    /**
     * Returns the configured entity view name.
     *
     * @return entity view name, or null when not configured
     */
    public String entityViewName() {
        return entityViewName;
    }

    private String requireText(final String value, final String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }

    /** Static response header action. */
    public static final class HeaderValue {
        private final String name;
        private final String value;

        private HeaderValue(final String name, final String value) {
            this.name = name;
            this.value = value == null ? "" : value;
        }

        /**
         * Returns the response header name.
         *
         * @return header name
         */
        public String name() {
            return name;
        }

        /**
         * Returns the response header value.
         *
         * @return header value
         */
        public String value() {
            return value;
        }
    }

    /** One request-header predicate used to decide if a route response policy should run. */
    public static final class RequestCondition {
        private enum Type {
            HEADER_EQUALS,
            HEADER_PRESENT,
            HEADER_MISSING
        }

        private final Type type;
        private final String headerName;
        private final String expectedValue;

        private RequestCondition(
                final Type type, final String headerName, final String expectedValue) {
            this.type = type;
            this.headerName = headerName;
            this.expectedValue = expectedValue;
        }

        private static RequestCondition headerEquals(
                final String headerName, final String expectedValue) {
            return new RequestCondition(Type.HEADER_EQUALS, headerName, expectedValue);
        }

        private static RequestCondition headerPresent(final String headerName) {
            return new RequestCondition(Type.HEADER_PRESENT, headerName, null);
        }

        private static RequestCondition headerMissing(final String headerName) {
            return new RequestCondition(Type.HEADER_MISSING, headerName, null);
        }

        /**
         * Reports whether this condition matches the supplied request headers.
         *
         * @param headers request headers
         * @return true when the predicate matches
         */
        public boolean matches(final HttpHeadersBlock headers) {
            switch (type) {
                case HEADER_EQUALS:
                    return headers.headerExists(headerName)
                            && headers.get(headerName).equals(expectedValue);
                case HEADER_PRESENT:
                    return headers.headerExists(headerName);
                case HEADER_MISSING:
                    return !headers.headerExists(headerName);
                default:
                    return false;
            }
        }
    }

    /** Header action that reads its value from a returned instance or draft field. */
    public static final class InstanceFieldHeader {
        private final String headerName;
        private final String fieldName;

        private InstanceFieldHeader(final String headerName, final String fieldName) {
            this.headerName = headerName;
            this.fieldName = fieldName;
        }

        /**
         * Returns the response header name.
         *
         * @return header name
         */
        public String headerName() {
            return headerName;
        }

        /**
         * Returns the returned instance/draft field name.
         *
         * @return field name
         */
        public String fieldName() {
            return fieldName;
        }
    }
}
