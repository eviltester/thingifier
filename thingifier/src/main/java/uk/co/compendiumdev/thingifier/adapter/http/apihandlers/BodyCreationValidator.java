package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;

public class BodyCreationValidator {

    public ValidationReport validate(
            final ApiBodyFields bodyFields, final EntityDefinition thingDefinition) {
        final ValidationReport report = new ValidationReport();

        // on creation, we should not have any protected fields in the body i.e. id or guid

        List<String> notAllowedToCreateWithList =
                thingDefinition.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
        final Map<String, Object> bodyFieldValues = bodyFields.asMap();
        for (String fieldName : notAllowedToCreateWithList) {
            if (bodyFieldValues.containsKey(fieldName)) {
                report.setValid(false);
                report.addErrorMessage(String.format("Not allowed to create with %s", fieldName));
            }
        }

        return report;
    }

    public ValidationReport areFieldsUnique(
            final ApiBodyFields bodyFields,
            final EntityDefinition thingDefinition,
            final EntityInstanceQuery query,
            final List<String> uniqueFields) {

        final ValidationReport report = new ValidationReport();

        for (Map.Entry<String, String> entry : bodyFields.asFlattenedStringMap()) {

            if (uniqueFields.contains(entry.getKey())) {
                String existingValue = entry.getValue();

                if (existingValue != null && existingValue.trim().length() > 0) {
                    // not unique if we can find something by that field value
                    final EntityInstance foundInstance;
                    if (query == null) {
                        foundInstance = null;
                    } else {
                        foundInstance =
                                query.findByField(
                                        thingDefinition, entry.getKey(), entry.getValue());
                    }

                    if (foundInstance != null) {
                        report.setValid(false);
                        report.addErrorMessage(
                                String.format(
                                        "Found Existing item with %s of %s",
                                        entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
        return report;
    }
}
