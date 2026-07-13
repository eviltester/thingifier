package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public class BodyRelationshipValidator {
    private final Thingifier thingifier;

    public BodyRelationshipValidator(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ValidationReport validate(
            final BodyParser bodyargs,
            final EntityDefinition thingDefinition,
            final String database) {
        return new RelationshipBodyCommandParser(thingifier)
                .parse(bodyargs, thingDefinition, database)
                .validationReport();
    }
}
