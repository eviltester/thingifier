package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

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
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

import java.util.Map;

public class DefaultGuiHtmlPagesRepositoryTest {

    @Test
    public void guiInstancePagesReadFromRepositoryWithoutLoadingCompatibilitySnapshot() {
        try (Thingifier thingifier = new Thingifier(
                new EntityRelModel(SqliteThingRepositoryProvider.inMemory()))) {

            EntityDefinition project = thingifier.defineThing("project", "projects");
            project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            project.addField(Field.is("title", FieldType.STRING));

            EntityDefinition todo = thingifier.defineThing("todo", "todos");
            todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            todo.addField(Field.is("title", FieldType.STRING));

            thingifier.defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY()).
                    whenReversed(Cardinality.ONE_TO_MANY(), "tasksof");

            ThingRepository repository =
                    thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

            EntityInstance projectInstance = repository.createInstance(EntityInstanceDraft.forEntity(project).withField("title", "Repository Project"));
            EntityInstance todoInstance = repository.createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "Repository Todo"));

            repository.connectRelationship(projectInstance, "tasks", todoInstance);

            DefaultGuiHtmlPages pages = new DefaultGuiHtmlPages(
                    new DefaultGUIHTML(), thingifier, "/gui");

            String listHtml = pages.getInstancesListPage(
                    EntityRelModel.DEFAULT_DATABASE_NAME, "todo");
            Assertions.assertTrue(listHtml.contains("Repository&nbsp;Todo"), listHtml);

            String detailHtml = pages.getInstanceDetailsPage(
                    EntityRelModel.DEFAULT_DATABASE_NAME,
                    "project",
                    Map.of("id", projectInstance.getPrimaryKeyValue()));

            Assertions.assertTrue(detailHtml.contains("Repository&nbsp;Project"), detailHtml);
            Assertions.assertTrue(detailHtml.contains("Repository&nbsp;Todo"), detailHtml);
        }
    }
}
