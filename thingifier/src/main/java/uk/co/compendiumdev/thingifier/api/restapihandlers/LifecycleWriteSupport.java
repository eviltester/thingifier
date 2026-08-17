package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingCommandResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;

/**
 * Runs write-command lifecycle phases around Thingifier validation and apply steps.
 *
 * <p>The helper keeps POST, PUT, PATCH, and DELETE handlers consistent. It preserves the existing
 * transaction boundary while allowing hooks to inspect or replace mapped commands, validation
 * results, action results, and API responses.
 */
final class LifecycleWriteSupport {

    /** Prevents construction because this class only provides static write lifecycle helpers. */
    private LifecycleWriteSupport() {}

    /**
     * Executes a mapped write request with optional lifecycle hook processing.
     *
     * @param runtime runtime services used to map results
     * @param lifecycleHooks lifecycle hook registry
     * @param lifecycle lifecycle context, or null for normal direct execution
     * @param mapping mapped write request
     * @param context request context containing the active store
     * @param remapper callback used when hooks mutate request data before validation
     * @return API response for the write operation
     */
    static ApiResponse execute(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks,
            final ThingifierApiLifecycleContext lifecycle,
            final ThingWriteRequestMapping mapping,
            final ThingifierRequestContext context,
            final CommandRemapper remapper) {
        ThingCommandResultApiMapper apiMapper =
                new ThingCommandResultApiMapper(runtime.apiConfig());
        if (mapping.isError()) {
            return apiMapper.map(mapping.getError());
        }

        if (lifecycle == null) {
            ThingCommandResult result =
                    runtime.commandService(context).execute(mapping.getCommand());
            return apiMapper.map(mapping, result);
        }

        final ThingWriteRequestMapping[] activeMapping = {mapping};
        lifecycle.useMappedWriteCommand(mapping.getCommand());
        ThingCommandService commandService = runtime.commandService(context);
        ThingCommandResult finalResult =
                commandService.runInTransaction(
                        () ->
                                executeInsideTransaction(
                                        commandService,
                                        lifecycleHooks,
                                        lifecycle,
                                        activeMapping,
                                        apiMapper,
                                        remapper));

        if (lifecycle.shouldShortCircuit() || lifecycle.apiResponse() != null) {
            return lifecycle.apiResponse();
        }
        return mapResult(apiMapper, lifecycle, activeMapping[0], finalResult);
    }

    /**
     * Runs validation, action, and hook phases inside the write transaction.
     *
     * <p>If an after-action hook changes the command result to an error, the transaction returns
     * that error result so normal rollback behavior is preserved.
     *
     * @param commandService command service owning the transaction
     * @param lifecycleHooks lifecycle hook registry
     * @param lifecycle lifecycle context for the request
     * @param activeMapping current mapping, updated when remapping occurs
     * @param apiMapper mapper used to convert command results to API responses
     * @param remapper callback used when hooks mutate request data before validation
     * @return final command result used by the transaction
     */
    private static ThingCommandResult executeInsideTransaction(
            final ThingCommandService commandService,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks,
            final ThingifierApiLifecycleContext lifecycle,
            final ThingWriteRequestMapping[] activeMapping,
            final ThingCommandResultApiMapper apiMapper,
            final CommandRemapper remapper) {
        lifecycleHooks.runBeforeValidationHooks(lifecycle);
        if (lifecycle.shouldShortCircuit()) {
            return ThingCommandResult.success();
        }

        if (shouldRemapCommand(lifecycle) && remapper != null) {
            ThingWriteRequestMapping remapped = remapper.remap();
            if (remapped.isError()) {
                lifecycle.shortCircuitWith(apiMapper.map(remapped.getError()));
                return ThingCommandResult.success();
            }
            activeMapping[0] = remapped;
            lifecycle.useMappedWriteCommand(remapped.getCommand());
        }

        ThingCommandResult validationResult = commandService.validate(lifecycle.writeCommand());
        lifecycle.replaceValidationResult(validationResult);
        lifecycleHooks.runAfterValidationHooks(lifecycle);
        if (lifecycle.shouldShortCircuit()) {
            return ThingCommandResult.success();
        }
        if (lifecycle.validationResult() != null && lifecycle.validationResult().isError()) {
            return lifecycle.validationResult();
        }

        lifecycleHooks.runBeforeActionHooks(lifecycle);
        if (lifecycle.shouldShortCircuit()) {
            return ThingCommandResult.success();
        }

        ThingCommandResult actionResult = commandService.applyValidated(lifecycle.writeCommand());
        lifecycle.replaceWriteCommandResult(actionResult);
        lifecycle.useApiResponse(mapResult(apiMapper, lifecycle, activeMapping[0], actionResult));
        lifecycleHooks.runAfterActionHooks(lifecycle);

        ThingCommandResult finalResult =
                lifecycle.writeCommandResult() == null
                        ? actionResult
                        : lifecycle.writeCommandResult();
        if (finalResult != actionResult && !lifecycle.apiResponseWasReplaced()) {
            lifecycle.useApiResponse(
                    mapResult(apiMapper, lifecycle, activeMapping[0], finalResult));
        }
        return finalResult;
    }

    /**
     * Reports whether hook-mutated request data should be mapped into a new write command.
     *
     * @param lifecycle lifecycle context after before-validation hooks
     * @return true when body data changed but the command was not explicitly replaced
     */
    private static boolean shouldRemapCommand(final ThingifierApiLifecycleContext lifecycle) {
        return (lifecycle.bodyFieldsWereReplaced() || lifecycle.rawBodyWasReplaced())
                && !lifecycle.writeCommandWasReplaced();
    }

    /**
     * Maps a command result back to an API response using the best available mapping context.
     *
     * @param apiMapper command result mapper
     * @param lifecycle lifecycle context for the request
     * @param mapping original or remapped request mapping
     * @param result command result to map
     * @return API response for the command result
     */
    private static ApiResponse mapResult(
            final ThingCommandResultApiMapper apiMapper,
            final ThingifierApiLifecycleContext lifecycle,
            final ThingWriteRequestMapping mapping,
            final ThingCommandResult result) {
        if (mapping != null && lifecycle.writeCommand() == mapping.getCommand()) {
            return apiMapper.map(mapping, result);
        }
        return apiMapper.map(lifecycle.writeCommand(), result);
    }

    /** Callback used to rebuild a write mapping after hooks replace request data. */
    interface CommandRemapper {
        /**
         * Remaps the current lifecycle request data to a write request mapping.
         *
         * @return remapped write request mapping
         */
        ThingWriteRequestMapping remap();
    }
}
