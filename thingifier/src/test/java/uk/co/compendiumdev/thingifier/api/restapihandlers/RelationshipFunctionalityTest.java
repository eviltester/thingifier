package uk.co.compendiumdev.thingifier.api.restapihandlers;

import static uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation.UPDATE_CONNECTED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class RelationshipFunctionalityTest {

    @Test
    public void relationshipPostUpdatesAlreadyConnectedChildWhenEnabled() {
        Thingifier thingifier = projectTaskThingifier(false, false);
        thingifier.apiDefaults().writeMethods().relationships().postCan(UPDATE_CONNECTED);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance task = createTask(thingifier, "Original");
        storeFor(thingifier).relationships().connect(project, "tasks", task);

        ApiResponse response =
                postRelationship(
                        thingifier,
                        project,
                        Map.of("id", Integer.valueOf(task.getPrimaryKeyValue()), "title", "New"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "New", currentTask(thingifier, task).getFieldValue("title").asString());
        Assertions.assertEquals(1, relatedTasks(thingifier, project).size());
    }

    @Test
    public void defaultRelationshipPostKeepsRepeatedConnectExistingBehavior() {
        Thingifier thingifier = projectTaskThingifier(false, false);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance task = createTask(thingifier, "Original");
        storeFor(thingifier).relationships().connect(project, "tasks", task);

        ApiResponse response =
                postRelationship(
                        thingifier,
                        project,
                        Map.of("id", Integer.valueOf(task.getPrimaryKeyValue()), "title", "New"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(
                "Original", currentTask(thingifier, task).getFieldValue("title").asString());
        Assertions.assertEquals(1, relatedTasks(thingifier, project).size());
    }

    @Test
    public void updateOnlyRelationshipPostRejectsUnconnectedChildren() {
        Thingifier thingifier = projectTaskThingifier(false, false);
        thingifier.apiDefaults().writeMethods().relationships().postCan(UPDATE_CONNECTED);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance task = createTask(thingifier, "Original");

        ApiResponse response =
                postRelationship(
                        thingifier,
                        project,
                        Map.of("id", Integer.valueOf(task.getPrimaryKeyValue()), "title", "New"));

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals(
                "Original", currentTask(thingifier, task).getFieldValue("title").asString());
        Assertions.assertTrue(relatedTasks(thingifier, project).isEmpty());
    }

    @Test
    public void invalidUpdateConnectedChildLeavesExistingChildUnchanged() {
        Thingifier thingifier = projectTaskThingifier(false, false);
        thingifier.apiDefaults().writeMethods().relationships().postCan(UPDATE_CONNECTED);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance task = createTask(thingifier, "Original");
        storeFor(thingifier).relationships().connect(project, "tasks", task);

        ApiResponse response =
                postRelationship(
                        thingifier,
                        project,
                        Map.of("id", Integer.valueOf(task.getPrimaryKeyValue()), "title", ""));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(
                "Original", currentTask(thingifier, task).getFieldValue("title").asString());
    }

    @Test
    public void relationshipDeleteDisconnectsByDefault() {
        Thingifier thingifier = projectTaskThingifier(false, false);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance task = createTask(thingifier, "Task");
        storeFor(thingifier).relationships().connect(project, "tasks", task);

        ApiResponse response =
                thingifier
                        .api()
                        .delete(
                                "projects/"
                                        + project.getPrimaryKeyValue()
                                        + "/tasks/"
                                        + task.getPrimaryKeyValue(),
                                new HttpHeadersBlock());

        Assertions.assertEquals(204, response.getStatusCode());
        Assertions.assertNotNull(currentTask(thingifier, task));
        Assertions.assertTrue(relatedTasks(thingifier, project).isEmpty());
    }

    @Test
    public void relationshipDeleteCanDeleteDisconnectedTarget() {
        Thingifier thingifier = projectTaskThingifier(true, false);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance task = createTask(thingifier, "Task");
        storeFor(thingifier).relationships().connect(project, "tasks", task);

        ApiResponse response =
                thingifier
                        .api()
                        .delete(
                                "projects/"
                                        + project.getPrimaryKeyValue()
                                        + "/tasks/"
                                        + task.getPrimaryKeyValue(),
                                new HttpHeadersBlock());

        Assertions.assertEquals(204, response.getStatusCode());
        Assertions.assertNull(currentTask(thingifier, task));
    }

    @Test
    public void parentDeleteCanCascadeToRelatedTargets() {
        Thingifier thingifier = projectTaskThingifier(false, true);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance firstTask = createTask(thingifier, "First");
        EntityInstance secondTask = createTask(thingifier, "Second");
        storeFor(thingifier).relationships().connect(project, "tasks", firstTask);
        storeFor(thingifier).relationships().connect(project, "tasks", secondTask);

        ApiResponse response =
                thingifier
                        .api()
                        .delete("projects/" + project.getPrimaryKeyValue(), new HttpHeadersBlock());

        Assertions.assertEquals(204, response.getStatusCode());
        Assertions.assertNull(currentProject(thingifier, project));
        Assertions.assertNull(currentTask(thingifier, firstTask));
        Assertions.assertNull(currentTask(thingifier, secondTask));
    }

    @Test
    public void parentDeleteCascadeHandlesRelationshipCycles() {
        Thingifier thingifier = nodeThingifier();
        EntityInstance first = createNode(thingifier, "a");
        EntityInstance second = createNode(thingifier, "b");
        storeFor(thingifier).relationships().connect(first, "children", second);
        storeFor(thingifier).relationships().connect(second, "children", first);

        ApiResponse response = thingifier.api().delete("nodes/a", new HttpHeadersBlock());

        Assertions.assertEquals(204, response.getStatusCode());
        Assertions.assertNull(currentNode(thingifier, "a"));
        Assertions.assertNull(currentNode(thingifier, "b"));
    }

    @Test
    public void fieldReferenceCreateConnectsReferencedTarget() {
        Thingifier thingifier = productItemThingifier();
        EntityInstance product = createProduct(thingifier, "Widget");

        ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "items",
                                body(
                                        Map.of(
                                                "productId",
                                                Integer.valueOf(product.getPrimaryKeyValue()),
                                                "quantity",
                                                2)),
                                contextFor(thingifier));

        EntityInstance item = response.getReturnedInstance();
        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(
                product.getPrimaryKeyValue(), item.getFieldValue("productId").asString());
        Assertions.assertTrue(isRelated(thingifier, item, "product", product));
    }

    @Test
    public void fieldReferenceCreateRejectsUnknownTarget() {
        Thingifier thingifier = productItemThingifier();

        ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "items",
                                body(Map.of("productId", 999, "quantity", 2)),
                                contextFor(thingifier));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(
                0, storeFor(thingifier).entityQueries().count(itemEntity(thingifier)));
    }

    @Test
    public void fieldReferenceAmendReplacesExistingRelationship() {
        Thingifier thingifier = productItemThingifier();
        EntityInstance firstProduct = createProduct(thingifier, "First");
        EntityInstance secondProduct = createProduct(thingifier, "Second");
        EntityInstance item = createItem(thingifier, firstProduct, 1);

        ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "items/" + item.getPrimaryKeyValue(),
                                body(
                                        Map.of(
                                                "productId",
                                                Integer.valueOf(
                                                        secondProduct.getPrimaryKeyValue()))),
                                contextFor(thingifier));

        EntityInstance updated = currentItem(thingifier, item);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertFalse(isRelated(thingifier, updated, "product", firstProduct));
        Assertions.assertTrue(isRelated(thingifier, updated, "product", secondProduct));
    }

    @Test
    public void partialAmendWithoutFieldReferenceLeavesRelationshipUnchanged() {
        Thingifier thingifier = productItemThingifier();
        EntityInstance product = createProduct(thingifier, "Widget");
        EntityInstance item = createItem(thingifier, product, 1);

        ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "items/" + item.getPrimaryKeyValue(),
                                body(Map.of("quantity", 3)),
                                contextFor(thingifier));

        EntityInstance updated = currentItem(thingifier, item);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("3", updated.getFieldValue("quantity").asString());
        Assertions.assertTrue(isRelated(thingifier, updated, "product", product));
    }

    @Test
    public void fullReplaceWithFieldReferenceReconnectsSuppliedRelationship() {
        Thingifier thingifier = productItemThingifier();
        EntityInstance firstProduct = createProduct(thingifier, "First");
        EntityInstance secondProduct = createProduct(thingifier, "Second");
        EntityInstance item = createItem(thingifier, firstProduct, 1);

        ApiResponse response =
                thingifier
                        .api()
                        .put(
                                "items/" + item.getPrimaryKeyValue(),
                                body(
                                        Map.of(
                                                "productId",
                                                Integer.valueOf(secondProduct.getPrimaryKeyValue()),
                                                "quantity",
                                                7)),
                                contextFor(thingifier));

        EntityInstance updated = currentItem(thingifier, item);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertFalse(isRelated(thingifier, updated, "product", firstProduct));
        Assertions.assertTrue(isRelated(thingifier, updated, "product", secondProduct));
    }

    @Test
    public void fullReplaceWithoutFieldReferenceDisconnectsPreviousRelationship() {
        Thingifier thingifier = productItemThingifier();
        EntityInstance product = createProduct(thingifier, "Widget");
        EntityInstance item = createItem(thingifier, product, 1);

        ApiResponse response =
                thingifier
                        .api()
                        .put(
                                "items/" + item.getPrimaryKeyValue(),
                                body(Map.of("quantity", 7)),
                                contextFor(thingifier));

        EntityInstance updated = currentItem(thingifier, item);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(relatedProducts(thingifier, updated).isEmpty());
    }

    @Test
    public void fieldReferenceConflictWithExplicitRelationshipReferenceIsRejected() {
        Thingifier thingifier = productItemThingifier();
        EntityInstance firstProduct = createProduct(thingifier, "First");
        EntityInstance secondProduct = createProduct(thingifier, "Second");
        Map<String, Object> explicitRelationship = new HashMap<>();
        explicitRelationship.put("id", Integer.valueOf(secondProduct.getPrimaryKeyValue()));

        ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "items",
                                body(
                                        Map.of(
                                                "productId",
                                                Integer.valueOf(firstProduct.getPrimaryKeyValue()),
                                                "product",
                                                explicitRelationship,
                                                "quantity",
                                                2)),
                                contextFor(thingifier));

        Assertions.assertEquals(400, response.getStatusCode());
        Assertions.assertEquals(
                0, storeFor(thingifier).entityQueries().count(itemEntity(thingifier)));
    }

    private Thingifier projectTaskThingifier(
            final boolean deleteTargetWhenDisconnected,
            final boolean deleteTargetsWhenSourceDeleted) {
        Thingifier thingifier = new Thingifier();
        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));

        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(Field.is("title", FieldType.STRING).withValidation(VRule.notEmpty()));

        uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition
                relationship =
                        thingifier.defineRelationship(
                                project, task, "tasks", Cardinality.ONE_TO_MANY());
        if (deleteTargetWhenDisconnected) {
            relationship.deleteTargetWhenDisconnected();
        }
        if (deleteTargetsWhenSourceDeleted) {
            relationship.deleteTargetsWhenSourceDeleted();
        }
        return thingifier;
    }

    private Thingifier nodeThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition node = thingifier.defineThing("node", "nodes");
        node.addAsPrimaryKeyField(Field.is("id", FieldType.STRING));
        thingifier
                .defineRelationship(node, node, "children", Cardinality.ONE_TO_MANY())
                .deleteTargetsWhenSourceDeleted();
        return thingifier;
    }

    private Thingifier productItemThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition product = thingifier.defineThing("product", "products");
        product.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        product.addField(Field.is("name", FieldType.STRING));

        EntityDefinition item = thingifier.defineThing("item", "items");
        item.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        item.addField(
                Field.is("productId", FieldType.INTEGER).references(product, "id", "product"));
        item.addField(Field.is("quantity", FieldType.INTEGER));

        thingifier.defineRelationship(item, product, "product", Cardinality.ONE_TO_ONE());
        return thingifier;
    }

    private EntityInstance createProject(final Thingifier thingifier, final String title) {
        return storeFor(thingifier)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(thingifier.getDefinitionNamed("project"))
                                .withField("title", title));
    }

    private EntityInstance createTask(final Thingifier thingifier, final String title) {
        return storeFor(thingifier)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(thingifier.getDefinitionNamed("task"))
                                .withField("title", title));
    }

    private EntityInstance createNode(final Thingifier thingifier, final String id) {
        return storeFor(thingifier)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(thingifier.getDefinitionNamed("node"))
                                .withField("id", id));
    }

    private EntityInstance createProduct(final Thingifier thingifier, final String name) {
        return storeFor(thingifier)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(thingifier.getDefinitionNamed("product"))
                                .withField("name", name));
    }

    private EntityInstance createItem(
            final Thingifier thingifier, final EntityInstance product, final int quantity) {
        EntityInstance item =
                storeFor(thingifier)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(itemEntity(thingifier))
                                        .withField("productId", product.getPrimaryKeyValue())
                                        .withField("quantity", String.valueOf(quantity)));
        storeFor(thingifier).relationships().connect(item, "product", product);
        return item;
    }

    private ApiResponse postRelationship(
            final Thingifier thingifier,
            final EntityInstance project,
            final Map<String, Object> values) {
        return thingifier
                .api()
                .post(
                        "projects/" + project.getPrimaryKeyValue() + "/tasks",
                        body(values),
                        contextFor(thingifier));
    }

    private EntityInstance currentProject(
            final Thingifier thingifier, final EntityInstance project) {
        return storeFor(thingifier)
                .entityQueries()
                .findByQueryIdentifier(
                        thingifier.getDefinitionNamed("project"), project.getPrimaryKeyValue());
    }

    private EntityInstance currentTask(final Thingifier thingifier, final EntityInstance task) {
        return storeFor(thingifier)
                .entityQueries()
                .findByQueryIdentifier(
                        thingifier.getDefinitionNamed("task"), task.getPrimaryKeyValue());
    }

    private EntityInstance currentNode(final Thingifier thingifier, final String identifier) {
        return storeFor(thingifier)
                .entityQueries()
                .findByQueryIdentifier(thingifier.getDefinitionNamed("node"), identifier);
    }

    private EntityInstance currentItem(final Thingifier thingifier, final EntityInstance item) {
        return storeFor(thingifier)
                .entityQueries()
                .findByQueryIdentifier(itemEntity(thingifier), item.getPrimaryKeyValue());
    }

    private EntityDefinition itemEntity(final Thingifier thingifier) {
        return thingifier.getDefinitionNamed("item");
    }

    private List<EntityInstance> relatedTasks(
            final Thingifier thingifier, final EntityInstance project) {
        return storeFor(thingifier).relationships().listRelated(project, "tasks");
    }

    private List<EntityInstance> relatedProducts(
            final Thingifier thingifier, final EntityInstance item) {
        return storeFor(thingifier).relationships().listRelated(item, "product");
    }

    private boolean isRelated(
            final Thingifier thingifier,
            final EntityInstance source,
            final String relationshipName,
            final EntityInstance target) {
        return storeFor(thingifier).relationships().listRelated(source, relationshipName).stream()
                .anyMatch(related -> related.getInternalId().equals(target.getInternalId()));
    }

    private ApiBodyFields body(final Map<String, Object> values) {
        return ApiBodyFields.fromMap(values);
    }

    private ThingifierRequestContext contextFor(final Thingifier thingifier) {
        return ThingifierRequestContext.from(thingifier, new HttpHeadersBlock());
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }
}
