package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStoreProvider;

public class DefaultGuiHtmlPagesRepositoryTest {

    @Test
    public void guiInstancePagesReadFromRepositoryWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier thingifier =
                new Thingifier(new EntityRelModel(SqliteThingStoreProvider.inMemory()))) {

            EntityDefinition project = thingifier.defineThing("project", "projects");
            project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            project.addField(Field.is("title", FieldType.STRING));

            EntityDefinition todo = thingifier.defineThing("todo", "todos");
            todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            todo.addField(Field.is("title", FieldType.STRING));

            thingifier
                    .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                    .whenReversed(Cardinality.ONE_TO_MANY(), "tasksof");

            ThingStore repository = thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);

            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(project)
                                            .withField("title", "Repository Project"));
            EntityInstance todoInstance =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(todo)
                                            .withField("title", "Repository Todo"));

            repository.relationships().connect(projectInstance, "tasks", todoInstance);

            DefaultGuiHtmlPages pages =
                    new DefaultGuiHtmlPages(new DefaultGUIHTML(), thingifier, "/gui");

            String listHtml =
                    pages.getInstancesListPage(EntityRelModel.DEFAULT_DATABASE_NAME, "todo");
            Assertions.assertTrue(listHtml.contains("Repository&nbsp;Todo"), listHtml);

            String detailHtml =
                    pages.getInstanceDetailsPage(
                            EntityRelModel.DEFAULT_DATABASE_NAME,
                            "project",
                            Map.of("id", projectInstance.getPrimaryKeyValue()));

            Assertions.assertTrue(detailHtml.contains("Repository&nbsp;Project"), detailHtml);
            Assertions.assertTrue(detailHtml.contains("Repository&nbsp;Todo"), detailHtml);
        }
    }

    @Test
    public void guiInstancePagesApplyConfiguredExplorerViewsAndApiSpecVisibility() {
        try (Thingifier thingifier =
                new Thingifier(new EntityRelModel(SqliteThingStoreProvider.inMemory()))) {

            EntityDefinition cart = thingifier.defineThing("cart", "carts");
            cart.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            cart.addField(Field.is("token", FieldType.AUTO_GUID));
            cart.addField(Field.is("state", FieldType.STRING));
            cart.defineView("PublicCart").hideResponseFields("token");

            EntityDefinition project = thingifier.defineThing("project", "projects");
            project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            project.addField(Field.is("title", FieldType.STRING));

            EntityDefinition todo = thingifier.defineThing("todo", "todos");
            todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            todo.addField(Field.is("title", FieldType.STRING));
            todo.addField(Field.is("secret", FieldType.STRING));
            todo.defineView("PublicTodo").hideResponseFields("secret");

            EntityDefinition internal =
                    thingifier.defineThing("internalentity", "internalentities");
            internal.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));

            thingifier
                    .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                    .whenReversed(Cardinality.ONE_TO_ONE(), "project");
            thingifier.defineRelationship(project, todo, "secretTasks", Cardinality.ONE_TO_MANY());
            thingifier.apiSpec().disableEntityRoutes("/internalentities");
            thingifier.apiSpec().hideRelationshipRoutes("/projects", "secretTasks");
            thingifier.guiConfig().dataExplorer().responseView("cart", "PublicCart");
            thingifier.guiConfig().dataExplorer().responseView("todo", "PublicTodo");

            ThingStore repository = thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);

            EntityInstance cartInstance =
                    repository
                            .entities()
                            .create(EntityInstanceDraft.forEntity(cart).withField("state", "open"));
            String token = cartInstance.getFieldValue("token").asString();

            EntityInstance projectInstance =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(project)
                                            .withField("title", "Public Project"));
            EntityInstance todoInstance =
                    repository
                            .entities()
                            .create(
                                    EntityInstanceDraft.forEntity(todo)
                                            .withField("title", "Visible Todo")
                                            .withField("secret", "Hidden Note"));

            repository.relationships().connect(projectInstance, "tasks", todoInstance);
            repository.relationships().connect(projectInstance, "secretTasks", todoInstance);

            DefaultGuiHtmlPages pages =
                    new DefaultGuiHtmlPages(new DefaultGUIHTML(), thingifier, "/gui");

            String entitiesHtml = pages.getEntitiesListPage(EntityRelModel.DEFAULT_DATABASE_NAME);
            Assertions.assertFalse(entitiesHtml.contains("entity=internalentity"), entitiesHtml);

            String cartListHtml =
                    pages.getInstancesListPage(EntityRelModel.DEFAULT_DATABASE_NAME, "cart");
            Assertions.assertFalse(cartListHtml.contains("token"), cartListHtml);
            Assertions.assertFalse(cartListHtml.contains(token), cartListHtml);

            String cartDetailHtml =
                    pages.getInstanceDetailsPage(
                            EntityRelModel.DEFAULT_DATABASE_NAME,
                            "cart",
                            Map.of("id", cartInstance.getPrimaryKeyValue()));
            Assertions.assertFalse(cartDetailHtml.contains("token"), cartDetailHtml);
            Assertions.assertFalse(cartDetailHtml.contains(token), cartDetailHtml);

            String projectDetailHtml =
                    pages.getInstanceDetailsPage(
                            EntityRelModel.DEFAULT_DATABASE_NAME,
                            "project",
                            Map.of("id", projectInstance.getPrimaryKeyValue()));
            Assertions.assertTrue(
                    projectDetailHtml.contains("Visible&nbsp;Todo"), projectDetailHtml);
            Assertions.assertFalse(projectDetailHtml.contains("secret"), projectDetailHtml);
            Assertions.assertFalse(
                    projectDetailHtml.contains("Hidden&nbsp;Note"), projectDetailHtml);
            Assertions.assertFalse(projectDetailHtml.contains("secretTasks"), projectDetailHtml);
        }
    }
}
