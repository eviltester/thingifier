package uk.co.compendiumdev.casestudy.todomanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.Collection;
import java.util.Random;

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

        final EntityInstanceCollection todos = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);

        for(int todoCount=0; todoCount < 100; todoCount++){
            todos.addInstance(new EntityInstance(todos.definition())).
                    setValue("title", "title " + System.nanoTime());
        }

        Assertions.assertEquals(100, todos.countInstances());

        todoManager.clearAllData();

        Assertions.assertEquals(0, todos.countInstances());
    }

    @Test
    public void createAndDeleteRelationships(){

        final EntityInstanceCollection todos = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);

        for(int todoCount=0; todoCount < 100; todoCount++){
            todos.addInstance(new EntityInstance(todos.definition())).
                    setValue("title", "title " + System.nanoTime());
        }

        final EntityInstanceCollection projects = todoManager.getThingInstancesNamed("project", EntityRelModel.DEFAULT_DATABASE_NAME);

        for(int todoCount=0; todoCount < 50; todoCount++){
            projects.addInstance(new EntityInstance(projects.definition())).
                    setValue("title", "title " + System.nanoTime());
        }


        Assertions.assertEquals(100, todos.countInstances());
        Assertions.assertEquals(50, projects.countInstances());

        for(EntityInstance project : projects.getInstances()){

            project.getRelationships().connect("tasks", getRandomThingInstance(todos.getInstances()));
        }


        for(EntityInstance todo : todos.getInstances()){

            todo.getRelationships().connect("task-of", getRandomThingInstance(projects.getInstances()));
        }

        System.out.println(todoManager.toString());

        todoManager.clearAllData();

        Assertions.assertEquals(0, todos.countInstances());
        Assertions.assertEquals(0, projects.countInstances());

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
