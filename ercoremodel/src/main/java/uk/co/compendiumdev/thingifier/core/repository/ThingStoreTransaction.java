package uk.co.compendiumdev.thingifier.core.repository;

public interface ThingStoreTransaction extends AutoCloseable {

    void commit();

    void rollback();

    @Override
    void close();
}
