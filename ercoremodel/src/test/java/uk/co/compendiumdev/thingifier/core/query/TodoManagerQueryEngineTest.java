package uk.co.compendiumdev.thingifier.core.query;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

/** Repository-backed URL query coverage for API-style entity reads. */
public class TodoManagerQueryEngineTest {

    private EntityRelModel todoManager;
    EntityInstance paperwork;
    EntityInstance filework;
    EntityInstance officeCategory;
    private EntityDefinition project;

    // todo: simplify setup and move this test into core
    @BeforeEach
    public void createDefinitions() {

        todoManager = new EntityRelModel();
        final EntityDefinition todo =
                todoManager
                        .createEntityDefinition("todo", "todos")
                        .addFields(Field.is("title", STRING));
        todo.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        project =
                todoManager
                        .createEntityDefinition("project", "projects")
                        .addFields(Field.is("title", STRING));
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        final EntityDefinition category =
                todoManager
                        .createEntityDefinition("category", "categories")
                        .addFields(Field.is("title", STRING));
        category.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        todoManager
                .createRelationshipDefinition(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_MANY(), "task-of");

        todoManager.createRelationshipDefinition(
                project, category, "categories", Cardinality.ONE_TO_MANY());
        todoManager.createRelationshipDefinition(
                category, todo, "todos", Cardinality.ONE_TO_MANY());
        todoManager.createRelationshipDefinition(
                category, project, "projects", Cardinality.ONE_TO_MANY());
        todoManager.createRelationshipDefinition(
                todo, category, "categories", Cardinality.ONE_TO_MANY());

        ThingRepository repository =
                todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

        paperwork =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todo).withField("title", "scan paperwork"));

        // System.out.println(new Gson().toJson(JsonThing.asJsonObject(paperwork)));

        filework =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todo).withField("title", "file paperwork"));

        officeCategory =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(category).withField("title", "Office"));

        repository.createInstance(
                EntityInstanceDraft.forEntity(category).withField("title", "Home"));

        repository.connectRelationship(paperwork, "categories", officeCategory);
    }

    /*
       API Prototype backend query engine
    */

    @Test
    public void canGetListOfEntityInstancesViaName() {
        // to do

        final RepositoryUrlQuery query =
                new RepositoryUrlQuery(
                        todoManager.getSchema(),
                        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                        "todo");

        List<EntityInstance> queryResults = query.performQuery().getListEntityInstances();

        Assertions.assertTrue(query.isResultACollection());

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));
    }

    @Test
    public void canGetListOfEntityInstancesViaPluralName() {
        // todos
        final RepositoryUrlQuery query =
                new RepositoryUrlQuery(
                        todoManager.getSchema(),
                        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                        "todos");

        List<EntityInstance> queryResults = query.performQuery().getListEntityInstances();

        Assertions.assertTrue(query.isResultACollection());

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));
    }

    @Test
    public void canGetSpecificEntityInstanceUsingGUID() {

        List<EntityInstance> queryResults;

        // to do/_GUID_

        final RepositoryUrlQuery query =
                new RepositoryUrlQuery(
                        todoManager.getSchema(),
                        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                        "todo/" + paperwork.getPrimaryKeyValue());

        queryResults = query.performQuery().getListEntityInstances();

        Assertions.assertFalse(
                query.isResultACollection()); // it can still be returned as a collection but is
        // valid to return as a single

        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertFalse(queryResults.contains(filework));
    }

    @Test
    public void canGetSpecificEntityInstanceUsingGUIDOnPlural() {

        List<EntityInstance> queryResults;

        // to do/_GUID_

        final RepositoryUrlQuery query =
                new RepositoryUrlQuery(
                        todoManager.getSchema(),
                        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                        "todos/" + paperwork.getPrimaryKeyValue());

        queryResults = query.performQuery().getListEntityInstances();

        Assertions.assertTrue(query.wasQueryIntendedToMatchAnInstance());
        Assertions.assertFalse(query.isResultACollection());

        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertFalse(queryResults.contains(filework));
    }

    @Test
    public void cannotGetGuidThatDoesNotExist() {

        List<EntityInstance> queryResults;

        // to do/_GUID_

        final RepositoryUrlQuery query =
                new RepositoryUrlQuery(
                        todoManager.getSchema(),
                        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                        "todo/" + paperwork.getPrimaryKeyValue() + "bob");

        queryResults = query.performQuery().getListEntityInstances();

        // even though it does not match anything I should know what type of thing this empty
        // collection is
        Assertions.assertTrue(query.wasQueryIntendedToMatchAnInstance());
        Assertions.assertTrue(query.lastMatchWasNothing());
        Assertions.assertEquals(
                todoManager.getSchema().getEntityDefinitionNamed("todo"),
                query.resultContainsDefn());

        Assertions.assertEquals(0, queryResults.size());
    }

    @Test
    public void cannotGetGuidThatDoesNotExistWithPlural() {

        List<EntityInstance> queryResults;

        // to do/_GUID_

        final RepositoryUrlQuery query =
                new RepositoryUrlQuery(
                        todoManager.getSchema(),
                        todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                        "todos/" + paperwork.getPrimaryKeyValue() + "bob");

        queryResults = query.performQuery().getListEntityInstances();

        // even though it does not match anything I should know what type of thing this empty
        // collection is
        Assertions.assertTrue(query.wasQueryIntendedToMatchAnInstance());
        Assertions.assertTrue(query.lastMatchWasNothing());
        Assertions.assertEquals(
                todoManager.getSchema().getEntityDefinitionNamed("todo"),
                query.resultContainsDefn());

        Assertions.assertEquals(0, queryResults.size());
    }

    @Test
    public void connectionTesting() {

        // stuff we could get for free from backend

        List<EntityInstance> queryResults;

        //
        ThingRepository repository =
                todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance officeWork =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(project).withField("title", "Office Work"));

        repository.connectRelationship(officeWork, "tasks", paperwork);
        repository.connectRelationship(officeWork, "tasks", filework);

        // match on relationships
        // project/_GUID_/tasks

        queryResults =
                new RepositoryUrlQuery(
                                todoManager.getSchema(),
                                todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                                String.format("project/%s/tasks", officeWork.getPrimaryKeyValue()))
                        .performQuery()
                        .getListEntityInstances();

        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));

        // should be able to get projects for a task

        queryResults =
                new RepositoryUrlQuery(
                                todoManager.getSchema(),
                                todoManager.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                                String.format("todo/%s/task-of", paperwork.getPrimaryKeyValue()))
                        .performQuery()
                        .getListEntityInstances();
        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(officeWork));

        // Repository URL query handles explicit relationship names, not legacy
        // entity-type traversal or multi-hop guesses.
        Assertions.assertFalse(
                RepositoryUrlQuery.canHandle(
                        todoManager.getSchema(),
                        String.format("project/%s/todo", officeWork.getPrimaryKeyValue())));

        Assertions.assertFalse(
                RepositoryUrlQuery.canHandle(
                        todoManager.getSchema(),
                        String.format(
                                "project/%s/todo/category", officeWork.getPrimaryKeyValue())));

        // invalid query should match nothing there is no entity called task
        // project/_GUID_/task

        Assertions.assertFalse(
                RepositoryUrlQuery.canHandle(
                        todoManager.getSchema(),
                        String.format("project/%s/task", officeWork.getPrimaryKeyValue())));
    }
}
