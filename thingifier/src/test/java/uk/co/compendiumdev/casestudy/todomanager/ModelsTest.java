package uk.co.compendiumdev.casestudy.todomanager;

import java.util.Collection;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class ModelsTest {
    private Thingifier todoManager;
    private ThingRepository repository;

    // create a set of models to build up the interface and usage

    // mp4 video (name, description, file) -> released to -> Site : YouTube (as url)
    // Youtube Playlist -> mp4 video
    // YouTube Description Template -> released video on YouTube

    // Tweet to Store
    // Group of Social Media References

    // Site -> Page

    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();
        repository = todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    @Test
    public void createAndDelete() {

        final EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        for (int todoCount = 0; todoCount < 100; todoCount++) {
            repository.createInstance(
                    EntityInstanceDraft.forEntity(todos)
                            .withField("title", "title " + System.nanoTime()));
        }

        Assertions.assertEquals(100, repository.countInstances(todos));

        todoManager.clearAllData();

        Assertions.assertEquals(0, repository.countInstances(todos));
    }

    @Test
    public void createAndDeleteRelationships() {

        final EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        for (int todoCount = 0; todoCount < 100; todoCount++) {
            repository.createInstance(
                    EntityInstanceDraft.forEntity(todos)
                            .withField("title", "title " + System.nanoTime()));
        }

        final EntityDefinition projects =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("project");

        for (int todoCount = 0; todoCount < 50; todoCount++) {
            repository.createInstance(
                    EntityInstanceDraft.forEntity(projects)
                            .withField("title", "title " + System.nanoTime()));
        }

        Assertions.assertEquals(100, repository.countInstances(todos));
        Assertions.assertEquals(50, repository.countInstances(projects));

        for (EntityInstance project : repository.listInstances(projects)) {

            repository.connectRelationship(
                    project, "tasks", getRandomThingInstance(repository.listInstances(todos)));
        }

        for (EntityInstance todo : repository.listInstances(todos)) {

            repository.connectRelationship(
                    todo, "task-of", getRandomThingInstance(repository.listInstances(projects)));
        }

        System.out.println(todoManager.toString());

        todoManager.clearAllData();

        Assertions.assertEquals(0, repository.countInstances(todos));
        Assertions.assertEquals(0, repository.countInstances(projects));

        System.out.println(todoManager.toString());
    }

    private EntityInstance getRandomThingInstance(final Collection<EntityInstance> instances) {
        int pos = new Random().nextInt(instances.size());
        for (EntityInstance instance : instances) {
            if (pos == 0) {
                return instance;
            }
            pos--;
        }
        return null;
    }
}
