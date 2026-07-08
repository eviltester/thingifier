package uk.co.compendiumdev.casestudy.todomanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.testsupport.ThingifierRepositoryTestSupport;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.Collection;
import java.util.Random;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
public class ModelsTest {
    private Thingifier todoManager;

    // create a set of models to build up the interface and usage

    // mp4 video (name, description, file) -> released to -> Site : YouTube (as url)
    // Youtube Playlist -> mp4 video
    // YouTube Description Template -> released video on YouTube


    // Tweet to Store
    // Group of Social Media References

    // Site -> Page

    @BeforeEach
    public void createDefinitions(){

        todoManager = TodoManagerModel.definedAsThingifier();

    }

    @Test
    public void createAndDelete(){

        final EntityDefinition todos = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        for(int todoCount=0; todoCount < 100; todoCount++){
            ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todos)).
                    setValue("title", "title " + System.nanoTime());
        }

        Assertions.assertEquals(100, ThingifierRepositoryTestSupport.repository(todoManager).countInstances(todos));

        todoManager.clearAllData();

        Assertions.assertEquals(0, ThingifierRepositoryTestSupport.repository(todoManager).countInstances(todos));
    }

    @Test
    public void createAndDeleteRelationships(){

        final EntityDefinition todos = ThingifierRepositoryTestSupport.entity(todoManager, "todo");

        for(int todoCount=0; todoCount < 100; todoCount++){
            ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(todos)).
                    setValue("title", "title " + System.nanoTime());
        }

        final EntityDefinition projects = ThingifierRepositoryTestSupport.entity(todoManager, "project");

        for(int todoCount=0; todoCount < 50; todoCount++){
            ThingifierRepositoryTestSupport.repository(todoManager).addInstance(new EntityInstance(projects)).
                    setValue("title", "title " + System.nanoTime());
        }


        Assertions.assertEquals(100, ThingifierRepositoryTestSupport.repository(todoManager).countInstances(todos));
        Assertions.assertEquals(50, ThingifierRepositoryTestSupport.repository(todoManager).countInstances(projects));

        for(EntityInstance project : ThingifierRepositoryTestSupport.repository(todoManager).listInstances(projects)){

            project.getRelationships().connect("tasks", getRandomThingInstance(ThingifierRepositoryTestSupport.repository(todoManager).listInstances(todos)));
        }


        for(EntityInstance todo : ThingifierRepositoryTestSupport.repository(todoManager).listInstances(todos)){

            todo.getRelationships().connect("task-of", getRandomThingInstance(ThingifierRepositoryTestSupport.repository(todoManager).listInstances(projects)));
        }

        System.out.println(todoManager.toString());

        todoManager.clearAllData();

        Assertions.assertEquals(0, ThingifierRepositoryTestSupport.repository(todoManager).countInstances(todos));
        Assertions.assertEquals(0, ThingifierRepositoryTestSupport.repository(todoManager).countInstances(projects));

        System.out.println(todoManager.toString());
    }

    private EntityInstance getRandomThingInstance(final Collection<EntityInstance> instances) {
        int pos = new Random().nextInt(instances.size());
        for(EntityInstance instance : instances){
            if(pos==0){
                return instance;
            }
            pos--;
        }
        return null;
    }


}
