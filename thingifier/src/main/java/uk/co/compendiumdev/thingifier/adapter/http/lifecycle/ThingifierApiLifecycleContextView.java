package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;
import uk.co.compendiumdev.thingifier.application.query.ThingReadQuery;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public interface ThingifierApiLifecycleContextView {

    ThingifierHttpApi.HttpVerb effectiveVerb();

    String path();

    ThingRoute route();

    EntityDefinition targetEntity();

    String targetIdentifier();

    EntityDefinition parentEntity();

    String parentIdentifier();

    String relationshipName();

    String childIdentifier();

    HttpHeadersBlock headers();

    void setHeader(String name, String value);

    QueryFilterParams queryParams();

    void replaceQueryParams(QueryFilterParams queryParams);

    String rawBody();

    void replaceRawBody(String rawBody);

    ApiBodyFields bodyFields();

    void replaceBodyFields(ApiBodyFields bodyFields);

    ApiRequestEnvelope.QueryBodyFormat queryBodyFormat();

    void replaceQueryBodyFormat(ApiRequestEnvelope.QueryBodyFormat queryBodyFormat);

    ThingifierRequestContext requestContext();

    ThingStore store();

    ThingWriteCommand writeCommand();

    void replaceWriteCommand(ThingWriteCommand command);

    ThingReadQuery readQuery();

    void replaceReadQuery(ThingReadQuery query);

    ThingCommandResult validationResult();

    void replaceValidationResult(ThingCommandResult result);

    void clearValidationResult();

    ThingCommandResult writeCommandResult();

    void replaceWriteCommandResult(ThingCommandResult result);

    RepositoryQueryResult readQueryResult();

    void replaceReadQueryResult(RepositoryQueryResult result);

    ApiResponse apiResponse();

    void replaceApiResponse(ApiResponse response);

    void shortCircuitWith(ApiResponse response);

    boolean shouldShortCircuit();
}
