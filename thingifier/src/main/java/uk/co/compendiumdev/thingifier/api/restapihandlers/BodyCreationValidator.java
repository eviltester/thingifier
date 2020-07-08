package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;

import java.util.List;
import java.util.Map;

public class BodyCreationValidator {
    private final Thingifier thingifier;

    public BodyCreationValidator(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ValidationReport validate(final BodyParser bodyargs, final Thing thing) {
        final ThingDefinition thingDefinition = thing.definition();
        return validate(bodyargs, thingDefinition);
    }

    public ValidationReport validate(final BodyParser bodyargs, final ThingDefinition thingDefinition) {
        final ValidationReport report = new ValidationReport();

        // on creation, we should not have any protected fields in the body i.e. id or guid

        List<String> notAllowedToCreateWithList = thingDefinition.getProtectedFieldNamesList();
        final Map<String, Object> bodyFields = bodyargs.getMap();
        for(String fieldName : notAllowedToCreateWithList){
            if(bodyFields.containsKey(fieldName)){
                report.setValid(false);
                report.addErrorMessage(String.format("Not allowed to create with %s", fieldName));
            }
        }

        return report;
    }
}
