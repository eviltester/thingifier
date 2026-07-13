package uk.co.compendiumdev.thingifier.api.restapihandlers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class RestApiGetHandlerTest {

    @Test
    public void canReturnSingleInstanceAsInstance() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance task = createTask(thingifier, "Task");

        ApiResponse response =
                thingifier.api().get("task/" + task.getPrimaryKeyValue(), params(), headers());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertFalse(response.isCollection());
        Assertions.assertEquals(task, response.getReturnedInstance());
    }

    @Test
    public void canReturnSingleInstanceAsCollectionWhenConfigured() {
        Thingifier thingifier = taskProjectThingifier();
        thingifier.apiConfig().setReturnSingleGetItemsAsCollection(true);
        EntityInstance task = createTask(thingifier, "Task");

        ApiResponse response =
                thingifier.api().get("task/" + task.getPrimaryKeyValue(), params(), headers());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.isCollection());
        Assertions.assertEquals(1, response.getReturnedInstanceCollection().size());
        Assertions.assertEquals(task, response.getReturnedInstanceCollection().get(0));
    }

    @Test
    public void relationshipReadPreservesResultTypeMetadata() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance taskInstance = createTask(thingifier, "Task");
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));
        store.relationships().connect(projectInstance, "tasks", taskInstance);

        ApiResponse response =
                thingifier
                        .api()
                        .get(
                                "project/" + projectInstance.getPrimaryKeyValue() + "/tasks",
                                params(),
                                headers());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.isCollection());
        Assertions.assertEquals(task, response.getTypeOfThingReturned());
    }

    @Test
    public void missingInstanceMapsToNotFound() {
        Thingifier thingifier = taskProjectThingifier();

        ApiResponse response = thingifier.api().get("task/missing", params(), headers());

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertTrue(
                response.getErrorMessages()
                        .contains("Could not find an instance with task/missing"));
    }

    @Test
    public void rejectsQueryParamsWhenFilteringIsEnforcedButDisabled() {
        Thingifier thingifier = taskProjectThingifier();
        thingifier.apiConfig().forParams().setAllowFilteringThroughUrlParams(false);
        thingifier.apiConfig().forParams().setEnforceFilteringThroughUrlParams(true);
        QueryFilterParams queryParams = new QueryFilterParams();
        queryParams.put("title", "=Task");

        ApiResponse response = thingifier.api().get("tasks", queryParams, headers());

        Assertions.assertEquals(400, response.getStatusCode());
        Assertions.assertTrue(
                response.getErrorMessages().contains("Can not use query parameters with tasks"));
    }

    @Test
    public void ignoresQueryParamsWhenFilteringIsDisabledButNotEnforced() {
        Thingifier thingifier = taskProjectThingifier();
        thingifier.apiConfig().forParams().setAllowFilteringThroughUrlParams(false);
        thingifier.apiConfig().forParams().setEnforceFilteringThroughUrlParams(false);
        createTask(thingifier, "Keep");
        createTask(thingifier, "Also returned");
        QueryFilterParams queryParams = new QueryFilterParams();
        queryParams.put("title", "=Keep");

        ApiResponse response = thingifier.api().get("tasks", queryParams, headers());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(2, response.getReturnedInstanceCollection().size());
    }

    @Test
    public void headUsesGetMappingAndClearsBody() {
        Thingifier thingifier = taskProjectThingifier();
        createTask(thingifier, "Task");

        ApiResponse response = thingifier.api().head("tasks", params(), headers());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertFalse(response.hasABody());
    }

    private Thingifier taskProjectThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        task.addField(Field.is("title", FieldType.STRING));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_MANY(), "task-of");
        return thingifier;
    }

    private EntityInstance createTask(final Thingifier thingifier, final String title) {
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        return storeFor(thingifier)
                .entities()
                .create(EntityInstanceDraft.forEntity(task).withField("title", title));
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private QueryFilterParams params() {
        return new QueryFilterParams();
    }

    private HttpHeadersBlock headers() {
        return new HttpHeadersBlock();
    }
}
