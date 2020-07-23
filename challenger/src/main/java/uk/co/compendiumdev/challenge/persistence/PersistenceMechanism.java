package uk.co.compendiumdev.challenge.persistence;

import uk.co.compendiumdev.challenge.ChallengerAuthData;

public interface PersistenceMechanism {

    void saveChallengerStatus(ChallengerAuthData data);
    ChallengerAuthData loadChallengerStatus(String guid);
}
