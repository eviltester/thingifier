package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.RelationshipConnection;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public class RelationshipCreator {
    private final Thingifier thingifier;

    public RelationshipCreator(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse createRelationships(
            final BodyParser bodyargs, final EntityInstance instance, final String database) {
        try {
            ThingCommandResult result =
                    new ThingCommandService(thingifier.getStore(database))
                            .connectRelationships(
                                    instance,
                                    relationshipConnectionsFromArgs(bodyargs, instance, database));
            if (result.isError()) {
                return ApiResponse.error(400, result.getErrorMessages());
            }

            return ApiResponse.created(instance, thingifier.apiConfig());
        } catch (Exception e) {
            return ApiResponse.error(400, "Error creating relationships " + e.getMessage());
        }
    }

    public List<RelationshipConnection> relationshipConnectionsFromArgs(
            final BodyParser bodyargs, final EntityInstance instance, final String database) {
        return relationshipConnectionsFromArgs(bodyargs, instance.getEntity(), database);
    }

    public List<RelationshipConnection> relationshipConnectionsFromArgs(
            final BodyParser bodyargs, final EntityDefinition entity, final String database) {
        RelationshipBodyCommands commands =
                new RelationshipBodyCommandParser(thingifier).parse(bodyargs, entity, database);
        ValidationReport validation = commands.validationReport();
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getCombinedErrorMessages());
        }
        return commands.connections();
    }
}
