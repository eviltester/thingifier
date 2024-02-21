package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.List;

public class EntityInstanceBulkUpdater {

    private final EntityInstance instance;

    public EntityInstanceBulkUpdater(EntityInstance instance) {
        this.instance = instance;
    }

    public void setFieldValuesFrom(List<NamedValue> fieldValues) {

        final List<String> anyErrors = instance.getFields().findAnyGuidOrIdDifferences(fieldValues);
        if(anyErrors.size()>0){
            throw new RuntimeException(anyErrors.get(0));
        }

        setFieldValuesFromArgsIgnoring(fieldValues, instance.getEntity().getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));
    }

    public void setFieldValuesFromArgsIgnoring(List<NamedValue> fieldValues,
                                               final List<String> ignoreFields) {

        for (NamedValue entry : fieldValues) {

            // Handle attempt to amend a protected field
            if (!ignoreFields.contains(entry.getName())) {
                // set the value because it is not protected
                instance.setValue(entry.getName(), entry.asString());
            }
        }
    }

    public void overrideFieldValuesFromArgsIgnoring(final List<NamedValue> fieldValues,
                                                    final List<String> ignoreFields) {
        for (NamedValue entry : fieldValues) {

            // Handle attempt to amend a protected field
            if (!ignoreFields.contains(entry.getName())) {
                // set the value because it is not protected
                instance.overrideValue(entry.getName(), entry.asString());
            }
        }
    }
}
