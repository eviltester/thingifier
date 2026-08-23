package uk.co.compendiumdev.thingifier.api.callbacks;

import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

/**
 * Immutable operation outcome supplied to route operation callbacks.
 *
 * <p>The result exposes stable API-level facts first, such as status and returned entity data, and
 * uses command/query details only to recover useful affected-instance information for write
 * operations whose response body may have been suppressed by route policy.
 */
public final class ThingifierApiOperationResult {

    private final int statusCode;
    private final String operationType;
    private final ApiResponse apiResponse;
    private final ThingCommandResult writeCommandResult;

    /**
     * Creates an operation result.
     *
     * @param statusCode final API status code
     * @param operationType operation label such as READ, CREATE, UPDATE, DELETE, or QUERY
     * @param apiResponse structured API response
     * @param writeCommandResult write command result when available
     */
    public ThingifierApiOperationResult(
            final int statusCode,
            final String operationType,
            final ApiResponse apiResponse,
            final ThingCommandResult writeCommandResult) {
        this.statusCode = statusCode;
        this.operationType = operationType == null ? "" : operationType;
        this.apiResponse = apiResponse;
        this.writeCommandResult = writeCommandResult;
    }

    /**
     * @return final API status code visible to the caller
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * @return true for 2xx and 3xx responses
     */
    public boolean successful() {
        return statusCode >= 200 && statusCode < 400;
    }

    /**
     * @return true for responses outside the success range
     */
    public boolean failed() {
        return !successful();
    }

    /**
     * @return operation label resolved by Thingifier where possible
     */
    public String operationType() {
        return operationType;
    }

    /**
     * @return true when the operation created a new instance
     */
    public boolean created() {
        return (writeCommandResult != null && writeCommandResult.createdInstance())
                || statusCode == 201;
    }

    /**
     * @return true when the operation appears to update an existing instance
     */
    public boolean updated() {
        return successful()
                && !created()
                && (operationType.equals("UPDATE")
                        || operationType.equals("REPLACE")
                        || operationType.equals("PATCH")
                        || operationType.equals("UPDATE_CONNECTED"));
    }

    /**
     * @return true when the operation appears to delete or disconnect a resource
     */
    public boolean deleted() {
        return successful()
                && (operationType.equals("DELETE") || operationType.equals("DISCONNECT"));
    }

    /**
     * Reports whether a single affected or returned instance is available.
     *
     * @return true when {@link #singleInstance()} can be called safely
     */
    public boolean hasSingleInstance() {
        return maybeSingleInstance().isPresent();
    }

    /**
     * Returns the single affected or returned instance.
     *
     * @return single instance
     * @throws IllegalStateException when no single instance is available
     */
    public EntityInstance singleInstance() {
        return maybeSingleInstance()
                .orElseThrow(() -> new IllegalStateException("operation has no single instance"));
    }

    /**
     * Returns the single affected or returned instance when available.
     *
     * @return optional single instance
     */
    public Optional<EntityInstance> maybeSingleInstance() {
        if (apiResponse != null && apiResponse.hasReturnedInstance()) {
            return Optional.of(apiResponse.getReturnedInstance());
        }
        if (writeCommandResult != null && writeCommandResult.getInstance() != null) {
            return Optional.of(writeCommandResult.getInstance());
        }
        return Optional.empty();
    }

    /**
     * @return true when the response contains a collection of instances
     */
    public boolean hasInstanceCollection() {
        return apiResponse != null && apiResponse.isCollection();
    }

    /**
     * Returns the instance collection from the response.
     *
     * @return immutable copy of returned instances, or an empty list
     */
    public List<EntityInstance> instanceCollection() {
        if (!hasInstanceCollection()) {
            return List.of();
        }
        return List.copyOf(apiResponse.getReturnedInstanceCollection());
    }

    /**
     * @return structured API response after route response policy has been applied
     */
    public ApiResponse apiResponse() {
        return apiResponse;
    }
}
