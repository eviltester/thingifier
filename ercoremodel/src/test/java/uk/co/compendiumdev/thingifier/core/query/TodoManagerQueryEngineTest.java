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
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/** Repository-backed query coverage for entity and relationship reads. */
public class TodoManagerQueryEngineTest {

    private EntityRelModel todoManager;
    private EntityDefinition todo;
    private EntityDefinition project;
    private EntityDefinition category;
    private EntityInstance paperwork;
    private EntityInstance filework;
    private EntityInstance officeCategory;

    @BeforeEach
    public void createDefinitions() {
        todoManager = new EntityRelModel();
        todo =
                todoManager
                        .createEntityDefinition("todo", "todos")
                        .addFields(Field.is("title", STRING));
        todo.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        project =
                todoManager
                        .createEntityDefinition("project", "projects")
                        .addFields(Field.is("title", STRING));
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        category =
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

        paperwork =
                store().entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "scan paperwork"));

        filework =
                store().entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "file paperwork"));

        officeCategory =
                store().entities()
                        .create(
                                EntityInstanceDraft.forEntity(category)
                                        .withField("title", "Office"));

        store().entities()
                .create(EntityInstanceDraft.forEntity(category).withField("title", "Home"));

        store().relationships().connect(paperwork, "categories", officeCategory);
    }

    @Test
    public void canGetListOfEntityInstances() {
        final RepositoryQuery query = query(RepositoryQuerySpec.collection(todo));

        List<EntityInstance> queryResults = query.getListEntityInstances();

        Assertions.assertTrue(query.isResultACollection());
        Assertions.assertEquals(2, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertTrue(queryResults.contains(filework));
    }

    @Test
    public void canGetSpecificEntityInstanceUsingGUID() {
        final RepositoryQuery query =
                query(RepositoryQuerySpec.instance(todo, paperwork.getPrimaryKeyValue()));

        List<EntityInstance> queryResults = query.getListEntityInstances();

        Assertions.assertFalse(query.isResultACollection());
        Assertions.assertEquals(1, queryResults.size());
        Assertions.assertTrue(queryResults.contains(paperwork));
        Assertions.assertFalse(queryResults.contains(filework));
    }

    @Test
    public void cannotGetGuidThatDoesNotExist() {
        final RepositoryQuery query =
                query(RepositoryQuerySpec.instance(todo, paperwork.getPrimaryKeyValue() + "bob"));

        List<EntityInstance> queryResults = query.getListEntityInstances();

        Assertions.assertTrue(query.wasQueryIntendedToMatchAnInstance());
        Assertions.assertTrue(query.lastMatchWasNothing());
        Assertions.assertEquals(todo, query.resultContainsDefn());
        Assertions.assertEquals(0, queryResults.size());
    }

    @Test
    public void canQueryRelationships() {
        EntityInstance officeWork =
                store().entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Office Work"));

        store().relationships().connect(officeWork, "tasks", paperwork);
        store().relationships().connect(officeWork, "tasks", filework);

        List<EntityInstance> tasksForProject =
                query(
                                RepositoryQuerySpec.relationship(
                                        project, officeWork.getPrimaryKeyValue(), "tasks"))
                        .getListEntityInstances();

        Assertions.assertEquals(2, tasksForProject.size());
        Assertions.assertTrue(tasksForProject.contains(paperwork));
        Assertions.assertTrue(tasksForProject.contains(filework));

        List<EntityInstance> projectsForTask =
                query(
                                RepositoryQuerySpec.relationship(
                                        todo, paperwork.getPrimaryKeyValue(), "task-of"))
                        .getListEntityInstances();

        Assertions.assertEquals(1, projectsForTask.size());
        Assertions.assertTrue(projectsForTask.contains(officeWork));
    }

    private RepositoryQuery query(final RepositoryQuerySpec spec) {
        return new RepositoryQuery(store(), spec).performQuery();
    }

    private ThingStore store() {
        return todoManager.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }
}
