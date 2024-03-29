package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.List;
import java.util.Map;

public class BodyCreationValidator {
    private final Thingifier thingifier;

    public BodyCreationValidator(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ValidationReport validate(final BodyParser bodyargs, final EntityInstanceCollection thing) {
        final EntityDefinition thingDefinition = thing.definition();
        return validate(bodyargs, thingDefinition);
    }

    public ValidationReport validate(final BodyParser bodyargs, final EntityDefinition thingDefinition) {
        final ValidationReport report = new ValidationReport();

        // on creation, we should not have any protected fields in the body i.e. id or guid

        List<String> notAllowedToCreateWithList =
                thingDefinition.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
        final Map<String, Object> bodyFields = bodyargs.getMap();
        for(String fieldName : notAllowedToCreateWithList){
            if(bodyFields.containsKey(fieldName)){
                report.setValid(false);
                report.addErrorMessage(String.format("Not allowed to create with %s", fieldName));
            }
        }

        return report;
    }

    public ValidationReport areFieldsUnique(final BodyParser bodyargs, final EntityInstanceCollection thing,
                                            List<String> uniqueFields) {

        final ValidationReport report = new ValidationReport();


        for (Map.Entry<String, String> entry : bodyargs.getFlattenedStringMap()) {

            if (uniqueFields.contains(entry.getKey())) {
                String existingValue = entry.getValue();

                if (existingValue != null && existingValue.trim().length() > 0) {
                    // not unique if we can find something by that field value
                    final EntityInstance foundInstance = thing.findInstanceByFieldNameAndValue(
                                    entry.getKey(),
                                    entry.getValue()
                    );

                    if (foundInstance!=null) {
                        report.setValid(false);
                        report.addErrorMessage(
                                String.format("Found Existing item with %s of %s",
                                        entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
        return report;
    }
}
