package uk.co.compendiumdev.thingifier.application;

import java.util.function.Supplier;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreTransaction;

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
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    private String messageFrom(final Exception exception) {
        String message = exception.getMessage();
        return message == null ? "" : message;
    }
}
