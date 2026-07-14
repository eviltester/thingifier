package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class RelationshipBodyCommands {

    private final List<Map.Entry<String, String>> relationshipEntries;
    private final List<RelationshipReference> references;
    private final ValidationReport validationReport;

    RelationshipBodyCommands(
            final List<Map.Entry<String, String>> relationshipEntries,
            final List<RelationshipReference> references,
            final ValidationReport validationReport) {
        this.relationshipEntries = new ArrayList<>(relationshipEntries);
        this.references = new ArrayList<>(references);
        this.validationReport = validationReport;
    }

    public List<Map.Entry<String, String>> relationshipEntries() {
        return new ArrayList<>(relationshipEntries);
    }

    public List<RelationshipReference> references() {
        return new ArrayList<>(references);
    }

    public ValidationReport validationReport() {
        return validationReport;
    }
}
