package uk.co.compendiumdev.challenge.persistence;

import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;

public interface DatabaseContentPersistenceMechanism {

    PersistenceResponse saveDatabaseContent(String guid, ERInstanceData instanceCollection);
    PersistenceResponse loadDatabaseContent(String guid);
}
