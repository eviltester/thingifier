package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingCommandResultApiMapper;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.ApplicationError;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ReplaceThingCommand;

public class ThingCommandResultApiMapperTest {

    @Test
    public void mapsReplaceCreateAutoFieldErrorToLegacyPutMessage() {
        ThingCommandResult result =
                ThingCommandResult.error(
                        ApplicationError.replaceCreateAutoFieldsNotAllowed("entity", "id"));

        ApiResponse response =
                mapper().map(new ReplaceThingCommand("entity", "1", List.of(), List.of()), result);

        Assertions.assertEquals(400, response.getStatusCode());
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

        Assertions.assertEquals(400, response.getStatusCode());
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
                400, ThingCommandResultApiMapper.statusFor(ApplicationError.validation("bad")));
        Assertions.assertEquals(
                404, ThingCommandResultApiMapper.statusFor(ApplicationError.notFound("missing")));
        Assertions.assertEquals(
                409, ThingCommandResultApiMapper.statusFor(ApplicationError.conflict("dupe")));
        Assertions.assertEquals(
                400,
                ThingCommandResultApiMapper.statusFor(ApplicationError.unsupported("unknown")));
    }

    private ThingCommandResultApiMapper mapper() {
        return new ThingCommandResultApiMapper(new ThingifierApiConfig(""));
    }
}
