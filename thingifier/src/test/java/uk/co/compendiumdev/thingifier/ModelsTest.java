package uk.co.compendiumdev.thingifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

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

        final Thing todos = todoManager.getThingNamed("todo");

        for(int todoCount=0; todoCount < 100; todoCount++){
            todos.addInstance(todos.createInstance().setValue("title", "title " + System.nanoTime()));
        }

        Assertions.assertEquals(100, todos.countInstances());

        todoManager.clearAllData();

        Assertions.assertEquals(0, todos.countInstances());
    }

    @Test
    public void createAndDeleteRelationships(){

        final Thing todos = todoManager.getThingNamed("todo");

        for(int todoCount=0; todoCount < 100; todoCount++){
            todos.addInstance(todos.createInstance().setValue("title", "title " + System.nanoTime()));
        }

        final Thing projects = todoManager.getThingNamed("project");

        for(int todoCount=0; todoCount < 50; todoCount++){
            projects.addInstance(projects.createInstance().setValue("title", "title " + System.nanoTime()));
        }


        Assertions.assertEquals(100, todos.countInstances());
        Assertions.assertEquals(50, projects.countInstances());

        for(ThingInstance project : projects.getInstances()){

            project.connects("tasks", getRandomThingInstance(todos.getInstances()));
        }


        for(ThingInstance todo : todos.getInstances()){

            todo.connects("task-of", getRandomThingInstance(projects.getInstances()));
        }

        System.out.println(todoManager.toString());

        todoManager.clearAllData();

        Assertions.assertEquals(0, todos.countInstances());
        Assertions.assertEquals(0, projects.countInstances());

        System.out.println(todoManager.toString());
    }

    private ThingInstance getRandomThingInstance(final Collection<ThingInstance> instances) {
        int pos = new Random().nextInt(instances.size());
        for(ThingInstance instance : instances){
            if(pos==0){
                return instance;
            }
            pos--;
        }
        return null;
    }


}
