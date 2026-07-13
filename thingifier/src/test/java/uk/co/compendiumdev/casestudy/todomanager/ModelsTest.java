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
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class ModelsTest {
    private Thingifier todoManager;
    private ThingStore repository;

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
        repository = todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    @Test
    public void createAndDelete() {

        final EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        for (int todoCount = 0; todoCount < 100; todoCount++) {
            repository
                    .entities()
                    .create(
                            EntityInstanceDraft.forEntity(todos)
                                    .withField("title", "title " + System.nanoTime()));
        }

        Assertions.assertEquals(100, repository.entityQueries().count(todos));

        repository.administration().clearAllData();

        Assertions.assertEquals(0, repository.entityQueries().count(todos));
    }

    @Test
    public void createAndDeleteRelationships() {

        final EntityDefinition todos =
                todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        for (int todoCount = 0; todoCount < 100; todoCount++) {
            repository
                    .entities()
                    .create(
                            EntityInstanceDraft.forEntity(todos)
                                    .withField("title", "title " + System.nanoTime()));
        }

        final EntityDefinition projects =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("project");

        for (int todoCount = 0; todoCount < 50; todoCount++) {
            repository
                    .entities()
                    .create(
                            EntityInstanceDraft.forEntity(projects)
                                    .withField("title", "title " + System.nanoTime()));
        }

        Assertions.assertEquals(100, repository.entityQueries().count(todos));
        Assertions.assertEquals(50, repository.entityQueries().count(projects));

        for (EntityInstance project : repository.entityQueries().list(projects)) {

            repository
                    .relationships()
                    .connect(
                            project,
                            "tasks",
                            getRandomThingInstance(repository.entityQueries().list(todos)));
        }

        for (EntityInstance todo : repository.entityQueries().list(todos)) {

            repository
                    .relationships()
                    .connect(
                            todo,
                            "task-of",
                            getRandomThingInstance(repository.entityQueries().list(projects)));
        }

        System.out.println(todoManager.toString());

        repository.administration().clearAllData();

        Assertions.assertEquals(0, repository.entityQueries().count(todos));
        Assertions.assertEquals(0, repository.entityQueries().count(projects));

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
