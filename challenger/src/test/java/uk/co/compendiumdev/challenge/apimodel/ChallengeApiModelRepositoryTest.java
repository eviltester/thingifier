package uk.co.compendiumdev.challenge.apimodel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.repository.SqliteThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.SqliteThingRepositoryProvider;

public class ChallengeApiModelRepositoryTest {

    @Test
    public void challengeApiCanUseSqliteInMemoryRepository() {
        Thingifier thingifier = new ChallengeApiModel().get(SqliteThingRepositoryProvider.inMemory());

        Assertions.assertTrue(
                thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        instanceof SqliteThingRepository);
        Assertions.assertEquals(
                10,
                thingifier.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME).
                        countInstances());
    }
}
