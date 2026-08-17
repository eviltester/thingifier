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

final class LifecycleWriteSupport {

    private LifecycleWriteSupport() {}

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

    private static boolean shouldRemapCommand(final ThingifierApiLifecycleContext lifecycle) {
        return (lifecycle.bodyFieldsWereReplaced() || lifecycle.rawBodyWasReplaced())
                && !lifecycle.writeCommandWasReplaced();
    }

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

    interface CommandRemapper {
        ThingWriteRequestMapping remap();
    }
}
