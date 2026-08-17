package uk.co.compendiumdev.thingifier.api.spec;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public final class ThingifierApiEntityRule {

    private final String entityName;
    private String defaultRequestView;
    private String defaultResponseView;

    ThingifierApiEntityRule(final String entityName) {
        this.entityName = requiredName("Entity name", entityName);
        this.defaultRequestView = null;
        this.defaultResponseView = null;
    }

    public String entityName() {
        return entityName;
    }

    public ThingifierApiEntityRule defaultRequestView(final String viewName) {
        defaultRequestView = requiredName("View name", viewName);
        return this;
    }

    public ThingifierApiEntityRule defaultResponseView(final String viewName) {
        defaultResponseView = requiredName("View name", viewName);
        return this;
    }

    public ThingifierApiEntityRule defaultEntityView(final String viewName) {
        defaultRequestView(viewName);
        defaultResponseView(viewName);
        return this;
    }

    public boolean hasDefaultRequestView() {
        return defaultRequestView != null;
    }

    public String defaultRequestView() {
        return defaultRequestView;
    }

    public boolean hasDefaultResponseView() {
        return defaultResponseView != null;
    }

    public String defaultResponseView() {
        return defaultResponseView;
    }

    boolean matches(final EntityDefinition entity) {
        if (entity == null) {
            return false;
        }
        return normalized(entityName).equals(normalized(entity.getName()))
                || normalized(entityName).equals(normalized(entity.getPlural()));
    }

    boolean sameEntityName(final String name) {
        return normalized(entityName).equals(normalized(name));
    }

    private String requiredName(final String label, final String name) {
        final String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return normalized;
    }

    private String normalized(final String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }
}
