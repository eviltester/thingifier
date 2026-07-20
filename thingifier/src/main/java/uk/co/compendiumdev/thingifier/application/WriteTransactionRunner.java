package uk.co.compendiumdev.thingifier.application;

import java.util.function.Supplier;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreTransaction;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreWriteException;

final class WriteTransactionRunner {

    private final ThingStore store;

    WriteTransactionRunner(final ThingStore store) {
        this.store = store;
    }

    ThingCommandResult run(final Supplier<ThingCommandResult> operation) {
        try (ThingStoreTransaction transaction = store.beginTransaction()) {
            ThingCommandResult result = operation.get();
            if (result.isSuccessful()) {
                transaction.commit();
            } else {
                transaction.rollback();
            }
            return result;
        } catch (ThingStoreWriteException e) {
            return ThingCommandResult.error(errorFrom(e));
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    private ApplicationError errorFrom(final ThingStoreWriteException exception) {
        return switch (exception.reason()) {
            case MAX_INSTANCE_LIMIT_REACHED ->
                    ApplicationError.maxInstanceLimitReached(
                            messageFrom(exception), exception.details());
            case MAX_INSTANCE_LIMIT_WOULD_BE_EXCEEDED ->
                    ApplicationError.maxInstanceLimitWouldBeExceeded(
                            messageFrom(exception), exception.details());
            case DUPLICATE_PRIMARY_KEY ->
                    ApplicationError.duplicatePrimaryKey(
                            messageFrom(exception), exception.details());
            case MISSING_PRIMARY_KEY ->
                    ApplicationError.missingPrimaryKey(messageFrom(exception), exception.details());
            case WRONG_ENTITY_TYPE ->
                    ApplicationError.storeWriteValidationFailed(
                            messageFrom(exception), exception.details());
        };
    }

    private String messageFrom(final Exception exception) {
        String message = exception.getMessage();
        return message == null ? "" : message;
    }
}
