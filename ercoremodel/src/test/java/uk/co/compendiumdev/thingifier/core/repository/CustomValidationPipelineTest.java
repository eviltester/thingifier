package uk.co.compendiumdev.thingifier.core.repository;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStore;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStore;

class CustomValidationPipelineTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void customFieldValidatorRejectsCreate(
            final String storeName, final StoreFactory storeFactory) {
        final ERSchema schema = taskSchema();
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        task.getField("title")
                .withCustomValidation(value -> invalidReport("custom field rejected"));

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            final IllegalArgumentException thrown =
                    Assertions.assertThrows(
                            IllegalArgumentException.class,
                            () -> createTask(store, task, "custom"));

            Assertions.assertTrue(thrown.getMessage().contains("custom field rejected"), storeName);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void instanceValidatorRejectsInconsistentCandidate(
            final String storeName, final StoreFactory storeFactory) {
        final ERSchema schema = taskSchema();
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        task.addField(Field.is("start", FieldType.INTEGER).makeMandatory());
        task.addField(Field.is("end", FieldType.INTEGER).makeMandatory());
        task.withInstanceValidation(
                context -> {
                    final int start = context.candidate().getFieldValue("start").asInteger();
                    final int end = context.candidate().getFieldValue("end").asInteger();
                    if (start > end) {
                        return invalidReport("start must be before end");
                    }
                    return new ValidationReport();
                });

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            final IllegalArgumentException thrown =
                    Assertions.assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    store.entities()
                                            .create(
                                                    EntityInstanceDraft.forEntity(task)
                                                            .withField("title", "bad range")
                                                            .withField("start", "10")
                                                            .withField("end", "5")));

            Assertions.assertTrue(
                    thrown.getMessage().contains("start must be before end"), storeName);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void instanceValidatorIsSkippedWhenBuiltInFieldValidationFails(
            final String storeName, final StoreFactory storeFactory) {
        final AtomicInteger calls = new AtomicInteger();
        final ERSchema schema = taskSchema();
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        task.addField(Field.is("start", FieldType.INTEGER).makeMandatory());
        task.withInstanceValidation(
                context -> {
                    calls.incrementAndGet();
                    return invalidReport("instance validator should not run");
                });

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            final IllegalArgumentException thrown =
                    Assertions.assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    store.entities()
                                            .create(
                                                    EntityInstanceDraft.forEntity(task)
                                                            .withField("title", "missing start")));

            Assertions.assertTrue(thrown.getMessage().contains("start : field is mandatory"));
            Assertions.assertEquals(0, calls.get(), storeName);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void entityDomainValidatorCanCompareCandidateWithStoredInstances(
            final String storeName, final StoreFactory storeFactory) {
        final ERSchema schema = taskSchema();
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        task.withDomainValidation(
                context -> {
                    final String title = context.candidate().getFieldValue("title").asString();
                    final EntityInstance found =
                            context.store().entityQueries().findByField(task, "title", title);
                    if (found != null
                            && !found.getInternalId().equals(context.candidate().getInternalId())) {
                        return invalidReport("title already exists in domain");
                    }
                    return new ValidationReport();
                });

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            createTask(store, task, "duplicate");

            final IllegalArgumentException thrown =
                    Assertions.assertThrows(
                            IllegalArgumentException.class,
                            () -> createTask(store, task, "duplicate"));

            Assertions.assertTrue(
                    thrown.getMessage().contains("title already exists in domain"), storeName);
            Assertions.assertEquals(1, store.entityQueries().count(task));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void entityDomainValidatorOnlyRunsForItsOwningEntity(
            final String storeName, final StoreFactory storeFactory) {
        final AtomicInteger calls = new AtomicInteger();
        final ERSchema schema = taskSchema();
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        task.withDomainValidation(
                context -> {
                    calls.incrementAndGet();
                    return invalidReport("task domain validator should not run");
                });
        final EntityDefinition project = schema.defineEntity("project", "projects", -1);
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING).makeMandatory());

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            store.entities()
                    .create(EntityInstanceDraft.forEntity(project).withField("title", "project"));

            Assertions.assertEquals(0, calls.get(), storeName);
            Assertions.assertEquals(1, store.entityQueries().count(project));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void globalValidatorCanCompareCandidateWithStoredInstances(
            final String storeName, final StoreFactory storeFactory) {
        final ERSchema schema = taskSchema();
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        schema.withGlobalValidation(
                context -> {
                    final String title = context.candidate().getFieldValue("title").asString();
                    final EntityInstance found =
                            context.store().entityQueries().findByField(task, "title", title);
                    if (found != null
                            && !found.getInternalId().equals(context.candidate().getInternalId())) {
                        return invalidReport("title already exists globally");
                    }
                    return new ValidationReport();
                });

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            createTask(store, task, "duplicate");

            final IllegalArgumentException thrown =
                    Assertions.assertThrows(
                            IllegalArgumentException.class,
                            () -> createTask(store, task, "duplicate"));

            Assertions.assertTrue(
                    thrown.getMessage().contains("title already exists globally"), storeName);
            Assertions.assertEquals(1, store.entityQueries().count(task));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void instanceValidatorRunsForAmend(final String storeName, final StoreFactory storeFactory) {
        final ERSchema schema = taskSchema();
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        task.addField(Field.is("start", FieldType.INTEGER).makeMandatory());
        task.addField(Field.is("end", FieldType.INTEGER).makeMandatory());
        task.withInstanceValidation(
                context -> {
                    if (context.candidate().getFieldValue("start").asInteger()
                            > context.candidate().getFieldValue("end").asInteger()) {
                        return invalidReport("amended instance is inconsistent");
                    }
                    return new ValidationReport();
                });

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            final EntityInstance existing =
                    store.entities()
                            .create(
                                    EntityInstanceDraft.forEntity(task)
                                            .withField("title", "range")
                                            .withField("start", "1")
                                            .withField("end", "2"));

            final IllegalArgumentException thrown =
                    Assertions.assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    store.entities()
                                            .patch(
                                                    existing,
                                                    EntityInstanceDraft.forEntity(task)
                                                            .withField("end", "0")));

            Assertions.assertTrue(
                    thrown.getMessage().contains("amended instance is inconsistent"), storeName);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void instanceValidatorRunsForReplace(final String storeName, final StoreFactory storeFactory) {
        final ERSchema schema = taskSchema();
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        task.addField(Field.is("start", FieldType.INTEGER).makeMandatory());
        task.addField(Field.is("end", FieldType.INTEGER).makeMandatory());
        task.withInstanceValidation(
                context -> {
                    if (context.candidate().getFieldValue("start").asInteger()
                            > context.candidate().getFieldValue("end").asInteger()) {
                        return invalidReport("replacement is inconsistent");
                    }
                    return new ValidationReport();
                });

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            final EntityInstance existing =
                    store.entities()
                            .create(
                                    EntityInstanceDraft.forEntity(task)
                                            .withField("title", "range")
                                            .withField("start", "1")
                                            .withField("end", "2"));

            final IllegalArgumentException thrown =
                    Assertions.assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    store.entities()
                                            .replace(
                                                    existing,
                                                    EntityInstanceDraft.forEntity(task)
                                                            .withField("title", "range")
                                                            .withField("start", "5")
                                                            .withField("end", "1")));

            Assertions.assertTrue(
                    thrown.getMessage().contains("replacement is inconsistent"), storeName);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void pureRelationshipConnectDoesNotRunEntityDomainValidators(
            final String storeName, final StoreFactory storeFactory) {
        final AtomicInteger calls = new AtomicInteger();
        final ERSchema schema = taskSchema();
        final EntityDefinition project = schema.defineEntity("project", "projects", -1);
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING).makeMandatory());
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        schema.defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY());

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            final EntityInstance projectInstance =
                    store.entities()
                            .create(
                                    EntityInstanceDraft.forEntity(project)
                                            .withField("title", "project"));
            final EntityInstance taskInstance = createTask(store, task, "task");
            task.withDomainValidation(
                    context -> {
                        calls.incrementAndGet();
                        return invalidReport("entity domain validator should not run");
                    });

            store.relationships().connect(projectInstance, "tasks", taskInstance);

            Assertions.assertEquals(0, calls.get(), storeName);
            Assertions.assertTrue(
                    store.relationships()
                            .listRelated(projectInstance, "tasks")
                            .contains(taskInstance));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stores")
    void pureRelationshipConnectDoesNotRunGlobalValidators(
            final String storeName, final StoreFactory storeFactory) {
        final AtomicInteger calls = new AtomicInteger();
        final ERSchema schema = taskSchema();
        final EntityDefinition project = schema.defineEntity("project", "projects", -1);
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING).makeMandatory());
        final EntityDefinition task = schema.getEntityDefinitionNamed("task");
        schema.defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY());

        try (ThingStore store = initializedStore(storeFactory, schema)) {
            final EntityInstance projectInstance =
                    store.entities()
                            .create(
                                    EntityInstanceDraft.forEntity(project)
                                            .withField("title", "project"));
            final EntityInstance taskInstance = createTask(store, task, "task");
            schema.withGlobalValidation(
                    context -> {
                        calls.incrementAndGet();
                        return invalidReport("global validator should not run");
                    });

            store.relationships().connect(projectInstance, "tasks", taskInstance);

            Assertions.assertEquals(0, calls.get(), storeName);
            Assertions.assertTrue(
                    store.relationships()
                            .listRelated(projectInstance, "tasks")
                            .contains(taskInstance));
        }
    }

    private static Stream<Arguments> stores() {
        return Stream.of(
                Arguments.of(
                        "in-memory",
                        (StoreFactory)
                                () -> new InMemoryThingStore(EntityRelModel.DEFAULT_DATABASE_NAME)),
                Arguments.of(
                        "sqlite",
                        (StoreFactory)
                                () ->
                                        SqliteThingStore.inMemory(
                                                EntityRelModel.DEFAULT_DATABASE_NAME)));
    }

    private static ThingStore initializedStore(
            final StoreFactory storeFactory, final ERSchema schema) {
        final ThingStore store = storeFactory.create();
        store.administration().initializeFrom(schema);
        return store;
    }

    private static ERSchema taskSchema() {
        final ERSchema schema = new ERSchema();
        final EntityDefinition task = schema.defineEntity("task", "tasks", -1);
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(Field.is("title", FieldType.STRING).makeMandatory());
        return schema;
    }

    private static EntityInstance createTask(
            final ThingStore store, final EntityDefinition task, final String title) {
        return store.entities()
                .create(EntityInstanceDraft.forEntity(task).withField("title", title));
    }

    private static ValidationReport invalidReport(final String message) {
        return new ValidationReport().setValid(false).addErrorMessage(message);
    }

    private interface StoreFactory {
        ThingStore create();
    }
}
