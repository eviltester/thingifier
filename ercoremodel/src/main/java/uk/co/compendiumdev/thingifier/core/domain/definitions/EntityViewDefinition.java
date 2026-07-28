package uk.co.compendiumdev.thingifier.core.domain.definitions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class EntityViewDefinition {

    private final EntityDefinition entity;
    private final String name;
    private final Set<String> requestVisibleFields;
    private final Set<String> responseVisibleFields;
    private final Set<String> inputAllowedFields;

    EntityViewDefinition(final EntityDefinition entity, final String name) {
        this.entity = entity;
        this.name = name;
        this.requestVisibleFields = new HashSet<>(entity.getFieldNames());
        this.responseVisibleFields = new HashSet<>(entity.getFieldNames());
        this.inputAllowedFields = new HashSet<>(entity.getFieldNames());
    }

    public String getName() {
        return name;
    }

    public EntityDefinition getEntity() {
        return entity;
    }

    public EntityViewDefinition showFields(final String... fieldNames) {
        showRequestFields(fieldNames);
        showResponseFields(fieldNames);
        return this;
    }

    public EntityViewDefinition hideFields(final String... fieldNames) {
        hideRequestFields(fieldNames);
        hideResponseFields(fieldNames);
        return this;
    }

    public EntityViewDefinition showRequestFields(final String... fieldNames) {
        for (String fieldName : validFieldNames(fieldNames)) {
            requestVisibleFields.add(fieldName);
        }
        return this;
    }

    public EntityViewDefinition hideRequestFields(final String... fieldNames) {
        for (String fieldName : validFieldNames(fieldNames)) {
            requestVisibleFields.remove(fieldName);
        }
        return this;
    }

    public EntityViewDefinition showResponseFields(final String... fieldNames) {
        for (String fieldName : validFieldNames(fieldNames)) {
            responseVisibleFields.add(fieldName);
        }
        return this;
    }

    public EntityViewDefinition hideResponseFields(final String... fieldNames) {
        for (String fieldName : validFieldNames(fieldNames)) {
            responseVisibleFields.remove(fieldName);
        }
        return this;
    }

    public EntityViewDefinition allowInputFields(final String... fieldNames) {
        for (String fieldName : validFieldNames(fieldNames)) {
            inputAllowedFields.add(fieldName);
        }
        return this;
    }

    public EntityViewDefinition disallowInputFields(final String... fieldNames) {
        for (String fieldName : validFieldNames(fieldNames)) {
            inputAllowedFields.remove(fieldName);
        }
        return this;
    }

    public boolean isRequestVisible(final String fieldName) {
        return requestVisibleFields.contains(fieldName);
    }

    public boolean isResponseVisible(final String fieldName) {
        return responseVisibleFields.contains(fieldName);
    }

    public boolean isInputAllowed(final String fieldName) {
        return inputAllowedFields.contains(fieldName);
    }

    public Set<String> requestVisibleFields() {
        return Collections.unmodifiableSet(requestVisibleFields);
    }

    public Set<String> responseVisibleFields() {
        return Collections.unmodifiableSet(responseVisibleFields);
    }

    public Set<String> inputAllowedFields() {
        return Collections.unmodifiableSet(inputAllowedFields);
    }

    private Set<String> validFieldNames(final String... fieldNames) {
        if (fieldNames == null) {
            return Set.of();
        }
        final Set<String> validNames = new HashSet<>();
        for (String fieldName : fieldNames) {
            if (!entity.hasFieldNameDefined(fieldName)) {
                throw new IllegalArgumentException(
                        String.format(
                                "Field %s is not defined for entity %s",
                                fieldName, entity.getName()));
            }
            validNames.add(fieldName);
        }
        return validNames;
    }
}
