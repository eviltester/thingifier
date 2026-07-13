package uk.co.compendiumdev.challenge.persistence;

public interface DatabaseContentPersistenceMechanism {

    PersistenceResponse saveDatabaseContent(String guid, String databaseContents);

    PersistenceResponse loadDatabaseContent(String guid);
}
