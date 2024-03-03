package uk.co.compendiumdev.challenge.challengers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ChallengersTest {

    @Test
    void canPurgeUserData() throws NoSuchFieldException, IllegalAccessException {

        EntityRelModel erModel = new EntityRelModel( new ERSchema(), new ERInstanceData());
        erModel.createInstanceDatabaseIfNotExisting("an-active-user");

        int originalNumberOfDatabases = erModel.getDatabaseNames().size();

        Challengers challengers = new Challengers(erModel, Arrays.asList(CHALLENGE.values()));

        challengers.setMultiPlayerMode();

        ChallengerAuthData challenger = challengers.createNewChallenger();
        String guid = challenger.getXChallenger();
        erModel.createInstanceDatabaseIfNotExisting(guid);


        // hack last accessed time so that we can purge it
        Field lastAccessedField = ChallengerAuthData.class.getDeclaredField("lastAccessed");
        lastAccessedField.setAccessible(true);
        lastAccessedField.set(challenger, 0L);
        Assertions.assertEquals(0, challenger.getLastAccessed());

        //forget about it
        challenger=null;

        Assertions.assertTrue(erModel.getDatabaseNames().contains(guid));

        challengers.purgeOldAuthData();

        challenger = challengers.getChallenger(guid);
        Assertions.assertNull(challenger);

        Assertions.assertEquals(originalNumberOfDatabases, erModel.getDatabaseNames().size());
        Assertions.assertTrue(erModel.getDatabaseNames().contains("an-active-user"));
        Assertions.assertFalse(erModel.getDatabaseNames().contains(guid));
    }
}
