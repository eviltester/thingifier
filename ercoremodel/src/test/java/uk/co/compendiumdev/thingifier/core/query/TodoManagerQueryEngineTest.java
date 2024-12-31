package uk.co.compendiumdev.thingifier.core.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.List;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

public class TodoManagerQueryEngineTest {

    private EntityRelModel todoManager;
    EntityInstance paperwork;
    EntityInstance filework;
    EntityInstanceCollection projects;
    EntityInstance officeCategory;
    private EntityDefinition project;

    // todo: simplify setup and move this test into core
    @BeforeEach
    public void createDefinitions(){

        todoManager = new EntityRelModel();
        final EntityDefinition todo = todoManager.createEntityDefinition("todo", "todos").
                addFields(Field.is("title", STRING));
        todo.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        project = todoManager.createEntityDefinition("project", "projects")
                .addFields(
                        Field.is("title", STRING));
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));


        final EntityDefinition category = todoManager.createEntityDefinition("category", "categories")
                .addFields(
                        Field.is("title", STRING));
        category.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        todoManager.createRelationshipDefinition(project, todo, "tasks", Cardinality.ONE_TO_MANY()).
                whenReversed(Cardinality.ONE_TO_MANY(),"task-of");

        todoManager.createRelationshipDefinition(project, category, "categories", Cardinality.ONE_TO_MANY());
        todoManager.createRelationshipDefinition(category, todo, "todos", Cardinality.ONE_TO_MANY());
        todoManager.createRelationshipDefinition(category, project, "projects", Cardinality.ONE_TO_MANY());
        todoManager.createRelationshipDefinition(todo, category, "categories", Cardinality.ONE_TO_MANY());

        final EntityInstanceCollection todos = todoManager.getInstanceData().getInstanceCollectionForEntityNamed("todo");
        final EntityInstanceCollection categories = todoManager.getInstanceData().getInstanceCollectionForEntityNamed("category");
        projects = todoManager.getInstanceData().getInstanceCollectionForEntityNamed("project");

        paperwork = todos.addInstance(new EntityInstance(todos.definition())).setValue("title", "scan paperwork");

        //System.out.println(new Gson().toJson(JsonThing.asJsonObject(paperwork)));

        filework = todos.addInstance(new EntityInstance(todos.definition())).setValue("title", "file paperwork");

        officeCategory = categories.addInstance(new EntityInstance(categories.definition())).setValue("title", "Office");

        EntityInstance homeCategory = categories.addInstance(new EntityInstance(categories.definition())).setValue("title", "Home");


        paperwork.getRelationships().connect("categories", officeCategory);

    }

   /*
        API Prototype backend query engine
     */


    @Test
    public void canGetListOfEntityInstancesViaName(){
        // to do

        final SimpleQuery query = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), "todo");

        List<EntityInstance> queryResults = query.performQuery().getListEntityInstances();

        Assertions.assertTrue(query.isResultACollection());

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));

    }

    @Test
    public void canGetListOfEntityInstancesViaPluralName(){
        // todos
        final SimpleQuery query = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), "todos");

        List<EntityInstance> queryResults = query.performQuery().getListEntityInstances();

        Assertions.assertTrue(query.isResultACollection());

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));
    }

    @Test
    public void canGetSpecificEntityInstanceUsingGUID(){

        List<EntityInstance> queryResults;

        // to do/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), "todo/" + paperwork.getPrimaryKeyValue());

        queryResults = query.performQuery().getListEntityInstances();

        Assertions.assertFalse(query.isResultACollection()); // it can still be returned as a collection but is valid to return as a single

        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertFalse(queryResults.contains(filework));

    }

    @Test
    public void canGetSpecificEntityInstanceUsingGUIDOnPlural(){

        List<EntityInstance> queryResults;

        // to do/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), "todos/" + paperwork.getPrimaryKeyValue());

        queryResults = query.performQuery().getListEntityInstances();

        Assertions.assertTrue(query.isResultACollection()); // plural should always report itself as a collection even on instance

        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertFalse(queryResults.contains(filework));

    }


    @Test
    public void cannotGetGuidThatDoesNotExist(){

        List<EntityInstance> queryResults;

        // to do/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), "todo/" + paperwork.getPrimaryKeyValue() + "bob");

        queryResults = query.performQuery().getListEntityInstances();

        // even though it doesn not match anything I should know what type of thing this empty collection is
        Assertions.assertTrue(query.isResultACollection());
        Assertions.assertEquals(todoManager.getInstanceData().getInstanceCollectionForEntityNamed("todo").definition(), query.resultContainsDefn());

        Assertions.assertEquals(0, queryResults.size());
    }

    @Test
    public void cannotGetGuidThatDoesNotExistWithPlural(){

        List<EntityInstance> queryResults;

        // to do/_GUID_

        final SimpleQuery query = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), "todos/" + paperwork.getPrimaryKeyValue() + "bob");

        queryResults = query.performQuery().getListEntityInstances();

        // even though it doesn not match anything I should know what type of thing this empty collection is
        Assertions.assertTrue(query.isResultACollection());
        Assertions.assertEquals(todoManager.getInstanceData().getInstanceCollectionForEntityNamed("todo").definition(), query.resultContainsDefn());

        Assertions.assertEquals(0, queryResults.size());

    }



    @Test
    public void connectionTesting() {


        // stuff we could get for free from backend

        List<EntityInstance> queryResults;

        //
        EntityInstance officeWork = projects.addInstance(new EntityInstance(projects.definition())).setValue("title", "Office Work");

        officeWork.getRelationships().connect("tasks", paperwork);
        officeWork.getRelationships().connect("tasks", filework);


        // match on relationships
        // project/_GUID_/tasks

        queryResults = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), String.format("project/%s/tasks", officeWork.getPrimaryKeyValue())).performQuery().getListEntityInstances();

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));


        // should be able to get projects for a task

        queryResults = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), String.format("todo/%s/task-of", paperwork.getPrimaryKeyValue())).performQuery().getListEntityInstances();
        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(officeWork));


        // match on entity types
        // project/_GUID_/to do

        queryResults = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), String.format("project/%s/todo", officeWork.getPrimaryKeyValue())).performQuery().getListEntityInstances();

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));

        // project/_GUID_/to do/category

        queryResults = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), String.format("project/%s/todo/category", officeWork.getPrimaryKeyValue())).performQuery().getListEntityInstances();

        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(officeCategory));

        // invalid query should match nothing there is no entity called task
        // project/_GUID_/task

        queryResults = new SimpleQuery(todoManager.getSchema(), todoManager.getInstanceData(), String.format("project/%s/task", officeWork.getPrimaryKeyValue())).performQuery().getListEntityInstances();

        Assertions.assertEquals(0, queryResults.size());

    }


}
