package uk.co.compendiumdev.thingifier.api.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final List<InstanceFieldHeader> instanceFieldHeaders;
    private BodyAction bodyAction;
    private String bodyText;
    private String entityViewName;

    /** Creates an empty policy that preserves Thingifier's generated response. */
    public RouteApiResponsePolicy() {
        statusCode = null;
        staticHeaders = new ArrayList<>();
        instanceFieldHeaders = new ArrayList<>();
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
     * Returns instance-field header actions in declaration order.
     *
     * @return immutable instance-field header actions
     */
    public List<InstanceFieldHeader> instanceFieldHeaders() {
        return Collections.unmodifiableList(instanceFieldHeaders);
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
