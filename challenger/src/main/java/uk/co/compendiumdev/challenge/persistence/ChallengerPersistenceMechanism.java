package uk.co.compendiumdev.challenge.persistence;

import uk.co.compendiumdev.challenge.ChallengerAuthData;

public interface ChallengerPersistenceMechanism {

    PersistenceResponse saveChallengerStatus(ChallengerAuthData data);
    PersistenceResponse loadChallengerStatus(String guid);
}
