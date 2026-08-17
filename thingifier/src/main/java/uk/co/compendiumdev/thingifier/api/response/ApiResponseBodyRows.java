package uk.co.compendiumdev.thingifier.api.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

/**
 * Converts an {@link ApiResponse} into field names and rows for tabular response formats.
 *
 * <p>The tabular formats cannot include nested object fields, and they must respect response views
 * so the same public field visibility applies as JSON and XML rendering.
 */
final class ApiResponseBodyRows {

    private final ApiResponse apiResponse;

    ApiResponseBodyRows(final ApiResponse apiResponse) {
        this.apiResponse = apiResponse;
    }

    /**
     * Returns the visible scalar field names for the response entity type.
     *
     * @return ordered field names suitable for a table header
     */
    public List<String> fieldNames() {
        List<String> names = new ArrayList<>();
        EntityDefinition entity = entityDefinition();
        if (entity == null) {
            return names;
        }

        EntityViewDefinition view = apiResponse.responseViewFor(entity);
        for (String fieldName : entity.getFieldNames()) {
            if (view != null && !view.isResponseVisible(fieldName)) {
                continue;
            }

            Field field = entity.getField(fieldName);
            if (field.getType() == FieldType.OBJECT) {
                continue;
            }
            names.add(fieldName);
        }
        return names;
    }

    /**
     * Returns the response body as scalar string rows.
     *
     * @return rows matching the order returned by {@link #fieldNames()}
     */
    public List<List<String>> rows() {
        List<List<String>> rows = new ArrayList<>();
        if (!apiResponse.hasABody() || apiResponse.isErrorResponse()) {
            return rows;
        }

        List<String> fieldNames = fieldNames();
        if (apiResponse.isCollection()) {
            for (EntityInstance instance : apiResponse.getReturnedInstanceCollection()) {
                rows.add(rowFor(instance, fieldNames));
            }
            return rows;
        }

        if (apiResponse.hasReturnedDraft()) {
            rows.add(rowFor(apiResponse.getReturnedDraft(), fieldNames));
            return rows;
        }

        rows.add(rowFor(apiResponse.getReturnedInstance(), fieldNames));
        return rows;
    }

    /**
     * Resolves the entity definition represented by the response body.
     *
     * @return response entity definition, or null when no entity body is present
     */
    private EntityDefinition entityDefinition() {
        if (apiResponse.getTypeOfThingReturned() != null) {
            return apiResponse.getTypeOfThingReturned();
        }
        if (!apiResponse.hasABody() || apiResponse.isErrorResponse()) {
            return null;
        }
        if (apiResponse.isCollection()) {
            List<EntityInstance> instances = apiResponse.getReturnedInstanceCollection();
            if (!instances.isEmpty()) {
                return instances.get(0).getEntity();
            }
            return null;
        }
        if (apiResponse.hasReturnedDraft()) {
            return apiResponse.getReturnedDraft().getEntity();
        }
        return apiResponse.getReturnedInstance().getEntity();
    }

    /**
     * Converts a persisted instance into a table row.
     *
     * @param instance instance to render
     * @param fieldNames fields selected for the table
     * @return scalar string values for the selected fields
     */
    private List<String> rowFor(final EntityInstance instance, final List<String> fieldNames) {
        List<String> row = new ArrayList<>();
        for (String fieldName : fieldNames) {
            FieldValue value = instance.getFieldValue(fieldName);
            row.add(value == null ? "" : value.asString());
        }
        return row;
    }

    /**
     * Converts a draft instance into a table row using explicit, protected, and default values.
     *
     * @param draft draft to render
     * @param fieldNames fields selected for the table
     * @return scalar string values for the selected fields
     */
    private List<String> rowFor(final EntityInstanceDraft draft, final List<String> fieldNames) {
        List<String> row = new ArrayList<>();
        Map<String, String> values = new HashMap<>();
        for (NamedValue value : draft.getFieldValues()) {
            values.put(value.getName().toLowerCase(), value.asString());
        }
        for (NamedValue value : draft.getProtectedFieldValues()) {
            values.put(value.getName().toLowerCase(), value.asString());
        }

        EntityDefinition entity = draft.getEntity();
        for (String fieldName : fieldNames) {
            Field field = entity.getField(fieldName);
            String value = values.get(fieldName.toLowerCase());
            if (value == null) {
                if (field.hasDefaultValue()) {
                    value = field.getDefaultValue().asString();
                } else {
                    value = field.getType().getDefault();
                }
            }
            row.add(value == null ? "" : value);
        }
        return row;
    }
}
