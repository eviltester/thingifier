package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.application.RelationshipConnection;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class RelationshipBodyCommands {

    private final List<Map.Entry<String, String>> relationshipEntries;
    private final List<RelationshipConnection> connections;
    private final ValidationReport validationReport;

    RelationshipBodyCommands(
            final List<Map.Entry<String, String>> relationshipEntries,
            final List<RelationshipConnection> connections,
            final ValidationReport validationReport) {
        this.relationshipEntries = new ArrayList<>(relationshipEntries);
        this.connections = new ArrayList<>(connections);
        this.validationReport = validationReport;
    }

    public List<Map.Entry<String, String>> relationshipEntries() {
        return new ArrayList<>(relationshipEntries);
    }

    public List<RelationshipConnection> connections() {
        return new ArrayList<>(connections);
    }

    public ValidationReport validationReport() {
        return validationReport;
    }
}
