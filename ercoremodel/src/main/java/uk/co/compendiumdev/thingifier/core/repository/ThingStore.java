package uk.co.compendiumdev.thingifier.core.repository;

public interface ThingStore extends AutoCloseable {

    String databaseKey();

    EntityInstanceRepository entities();

    EntityInstanceQuery entityQueries();

    RelationshipRepository relationships();

    RepositoryAdministration administration();

    @Override
    default void close() {
        // Most store implementations do not own closeable resources.
    }
}
