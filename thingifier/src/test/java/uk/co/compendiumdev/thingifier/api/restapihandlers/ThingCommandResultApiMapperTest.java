package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ApiRouteDisplay;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingCommandResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapping;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.ApplicationError;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ReplaceThingCommand;

public class ThingCommandResultApiMapperTest {

    @Test
    public void mapsReplaceCreateAutoFieldErrorToLegacyPutMessage() {
        ThingCommandResult result =
                ThingCommandResult.error(
                        ApplicationError.replaceCreateAutoFieldsNotAllowed("entity", "id"));

        ApiResponse response =
                mapper().map(new ReplaceThingCommand("entity", "1", List.of(), List.of()), result);

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(
                List.of("Cannot create entity with PUT due to Auto fields id"),
                response.getErrorMessages());
    }

    @Test
    public void mapsReplaceCreateKeyMismatchErrorToLegacyPutMessage() {
        ThingCommandResult result =
                ThingCommandResult.error(
                        ApplicationError.replaceCreateKeyMismatch("entity", "newkey", "innerkey"));

        ApiResponse response =
                mapper().map(
                                new ReplaceThingCommand("entity", "newkey", List.of(), List.of()),
                                result);

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(
                List.of(
                        "Cannot create entity with PUT as key does not match body value "
                                + "newkey != innerkey"),
                response.getErrorMessages());
    }

    @Test
    public void mapsConflictWithoutValidationPrefix() {
        ThingCommandResult result =
                ThingCommandResult.error(
                        ApplicationError.conflict(
                                "Cannot Create with duplicate values: Found Existing item "
                                        + "with id of 1"));

        ApiResponse response =
                mapper().map(new CreateThingCommand("entity", List.of(), List.of(), true), result);

        Assertions.assertEquals(409, response.getStatusCode());
        Assertions.assertEquals(
                List.of("Cannot Create with duplicate values: Found Existing item with id of 1"),
                response.getErrorMessages());
    }

    @Test
    public void mapsApplicationCategoriesToHttpStatuses() {
        Assertions.assertEquals(400, ThingCommandResultApiMapper.statusFor(null));
        Assertions.assertEquals(
                422, ThingCommandResultApiMapper.statusFor(ApplicationError.validation("bad")));
        Assertions.assertEquals(
                404, ThingCommandResultApiMapper.statusFor(ApplicationError.notFound("missing")));
        Assertions.assertEquals(
                409, ThingCommandResultApiMapper.statusFor(ApplicationError.conflict("dupe")));
        Assertions.assertEquals(
                400,
                ThingCommandResultApiMapper.statusFor(ApplicationError.unsupported("unknown")));
    }

    @Test
    public void mapsCreateValidationWithExistingLegacyPrefixBehaviour() {
        ThingCommandResult result = ThingCommandResult.error("title : field is mandatory");

        ApiResponse response =
                mapper().map(new CreateThingCommand("entity", List.of(), List.of(), true), result);

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(
                List.of("Failed Validation: title : field is mandatory"),
                response.getErrorMessages());
    }

    @Test
    public void mapsSuccessfulDeleteToNoContent() {
        ApiResponse response =
                mapper().map(new DeleteThingCommand("task", "1"), ThingCommandResult.success());

        Assertions.assertEquals(204, response.getStatusCode());
        Assertions.assertFalse(response.hasABody());
    }

    @Test
    public void mapsInstanceNotFoundWithRouteContextToLegacyPathMessage() {
        ThingCommandResult result =
                ThingCommandResult.error(ApplicationError.instanceNotFound("task", "missing"));

        ApiResponse response =
                mapper().map(
                                ThingWriteRequestMapping.command(
                                        new DeleteThingCommand("task", "missing"),
                                        ApiRouteDisplay.originalPath("tasks/missing")),
                                result);

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(
                List.of("Could not find any instances with tasks/missing"),
                response.getErrorMessages());
    }

    @Test
    public void mapsPostInstanceNotFoundWithAdapterMessage() {
        ThingCommandResult result =
                ThingCommandResult.error(ApplicationError.instanceNotFound("task", "missing"));

        ApiResponse response =
                mapper().map(
                                ThingWriteRequestMapping.command(
                                        new DeleteThingCommand("task", "missing"),
                                        ApiRouteDisplay.missingInstanceMessage(
                                                "No such task entity instance with guid == missing found")),
                                result);

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(
                List.of("No such task entity instance with guid == missing found"),
                response.getErrorMessages());
    }

    @Test
    public void mapsMissingRelationshipParentToLegacyRouteMessage() {
        ThingCommandResult result =
                ThingCommandResult.error(
                        ApplicationError.parentInstanceNotFound(
                                "project", "missing-project", "tasks"));

        ApiResponse response =
                mapper().map(
                                ThingWriteRequestMapping.command(
                                        new RelateThingCommand(
                                                "project",
                                                "missing-project",
                                                "tasks",
                                                List.of(),
                                                List.of()),
                                        ApiRouteDisplay.originalPath(
                                                "projects/missing-project/tasks")),
                                result);

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(
                List.of(
                        "Could not find parent thing for relationship "
                                + "projects/missing-project/tasks"),
                response.getErrorMessages());
    }

    @Test
    public void mapsMissingRelationshipTargetToLegacyRouteMessage() {
        ThingCommandResult result =
                ThingCommandResult.error(
                        ApplicationError.relationshipTargetNotFound(
                                "project", "project-1", "tasks", "missing-task"));

        ApiResponse response =
                mapper().map(
                                ThingWriteRequestMapping.command(
                                        new DisconnectRelationshipCommand(
                                                "project", "project-1", "tasks", "missing-task"),
                                        ApiRouteDisplay.originalPath(
                                                "projects/project-1/tasks/missing-task")),
                                result);

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(
                List.of("Could not find any instances with projects/project-1/tasks/missing-task"),
                response.getErrorMessages());
    }

    private ThingCommandResultApiMapper mapper() {
        return new ThingCommandResultApiMapper(new ThingifierApiConfig(""));
    }
}
