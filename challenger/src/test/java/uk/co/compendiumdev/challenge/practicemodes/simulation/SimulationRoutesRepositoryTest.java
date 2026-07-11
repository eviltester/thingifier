package uk.co.compendiumdev.challenge.practicemodes.simulation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.ChallengerConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStore;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class SimulationRoutesRepositoryTest {

    @Test
    void simulationRoutesUseConfiguredSqliteMemoryRepository() {
        ChallengerConfig config = new ChallengerConfig();
        config.setSimulationRepositoryFromArgs(new String[] {"-sim-repository=sqlite-memory"});

        SimulationRoutes routes =
                new SimulationRoutes(new DefaultGUIHTML(), config.getSimulationRepositoryConfig());

        try {
            routes.setUpRepositoryBackedData();

            ThingStore repository =
                    routes.simulation.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
            Assertions.assertTrue(repository instanceof SqliteThingStore);
            Assertions.assertEquals(11, repository.entityQueries().count(routes.entityDefn));
        } finally {
            routes.close();
        }
    }
}
