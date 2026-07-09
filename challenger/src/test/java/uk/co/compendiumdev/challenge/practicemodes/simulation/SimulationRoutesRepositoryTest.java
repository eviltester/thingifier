package uk.co.compendiumdev.challenge.practicemodes.simulation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.ChallengerConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.repository.SqliteThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class SimulationRoutesRepositoryTest {

    @Test
    void simulationRoutesUseConfiguredSqliteMemoryRepository() {
        ChallengerConfig config = new ChallengerConfig();
        config.setSimulationRepositoryFromArgs(
                new String[]{"-sim-repository=sqlite-memory"});

        SimulationRoutes routes = new SimulationRoutes(
                new DefaultGUIHTML(),
                config.getSimulationRepositoryConfig());

        try {
            routes.setUpRepositoryBackedData();

            ThingRepository repository =
                    routes.simulation.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            Assertions.assertTrue(repository instanceof SqliteThingRepository);
            Assertions.assertEquals(11, repository.countInstances(routes.entityDefn));
        } finally {
            routes.close();
        }
    }
}
