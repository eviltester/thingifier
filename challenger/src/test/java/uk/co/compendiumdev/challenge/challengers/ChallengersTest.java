package uk.co.compendiumdev.challenge.challengers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.ChallengerAuthData;

import java.lang.reflect.Field;

public class ChallengersTest {

    @Test
    void canPurgeUserData() throws NoSuchFieldException, IllegalAccessException {

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();
        ChallengerAuthData challenger = challengers.createNewChallenger();
        String guid = challenger.getXChallenger();


        // hack last accessed time so that we can purge it
        Field lastAccessedField = ChallengerAuthData.class.getDeclaredField("lastAccessed");
        lastAccessedField.setAccessible(true);
        lastAccessedField.set(challenger, 0L);
        Assertions.assertEquals(0, challenger.getLastAccessed());

        //forget about it
        challenger=null;

        challengers.purgeOldAuthData();

        challenger = challengers.getChallenger(guid);
        Assertions.assertNull(challenger);
    }
}
