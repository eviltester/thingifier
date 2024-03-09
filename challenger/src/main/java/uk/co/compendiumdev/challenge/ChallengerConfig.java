package uk.co.compendiumdev.challenge;

import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;

public class ChallengerConfig {

    public boolean single_player_mode = true;
    public boolean isAdminApiEnabled = false;
    public boolean guiStayAlive=false;
    public PersistenceLayer persistenceLayer = new PersistenceLayer(PersistenceLayer.StorageType.LOCAL);

    public void setToMultiPlayerMode() {
        single_player_mode=false;
    }

    public void setToCloudPersistenceMode() {
        persistenceLayer = new PersistenceLayer(PersistenceLayer.StorageType.CLOUD);
    }

    public void setGuiToKeepSessionAlive() {
        guiStayAlive=true;
    }

    public void setToNoPersistenceMode() {
        persistenceLayer = new PersistenceLayer(PersistenceLayer.StorageType.NONE);
    }

    public void enableAdminApi() {
        isAdminApiEnabled=true;
    }
}
