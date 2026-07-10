package uk.co.compendiumdev.thingifier.core.domain.instances;

import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class EntityInstanceDraftTest {

    @Test
    void normalFieldsMustExistOnTheEntityDefinition() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () ->
                                EntityInstanceDraft.forEntity(entity())
                                        .withField("missing", "value"));

        Assertions.assertTrue(error.getMessage().contains("Could not find field: missing"));
    }

    @Test
    void protectedFieldsMustExistOnTheEntityDefinition() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () ->
                                EntityInstanceDraft.forEntity(entity())
                                        .withProtectedField("missing", "1"));

        Assertions.assertTrue(error.getMessage().contains("Could not find field: missing"));
    }

    @Test
    void normalFieldsValidateSuppliedValuesAgainstFieldType() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () ->
                                EntityInstanceDraft.forEntity(entity())
                                        .withField("estimate", "lots"));

        Assertions.assertTrue(
                error.getMessage().contains("estimate : lots does not match type INTEGER"));
    }

    @Test
    void nestedObjectFieldPathsCanBeSetWhenTheyExist() {
        EntityInstance instance =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(
                                EntityInstanceDraft.forEntity(entity())
                                        .withField("person.age", "42")
                                        .withField("person.name", "Connie"));

        Assertions.assertEquals(
                "42", instance.getFieldValue("person").asObject().getFieldValue("age").asString());
        Assertions.assertEquals(
                "Connie",
                instance.getFieldValue("person").asObject().getFieldValue("name").asString());
    }

    @Test
    void nestedObjectFieldPathsMustExistOnTheObjectDefinition() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () ->
                                EntityInstanceDraft.forEntity(entity())
                                        .withField("person.missing", "value"));

        Assertions.assertTrue(error.getMessage().contains("Could not find field: missing"));
    }

    @Test
    void nestedObjectFieldPathsMustTraverseObjectFields() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () ->
                                EntityInstanceDraft.forEntity(entity())
                                        .withField("name.first", "value"));

        Assertions.assertTrue(
                error.getMessage().contains("Cannot reference fields on non object fields: name"));
    }

    @Test
    void normalFieldsCannotSetAutoIncrementFields() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () -> EntityInstanceDraft.forEntity(entity()).withField("id", "12"));

        Assertions.assertTrue(
                error.getMessage()
                        .contains(
                                "id : field is protected and can only be set with withProtectedField"));
    }

    @Test
    void normalFieldsCannotSetAutoGuidFields() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () ->
                                EntityInstanceDraft.forEntity(entity())
                                        .withField("guid", UUID.randomUUID().toString()));

        Assertions.assertTrue(
                error.getMessage()
                        .contains(
                                "guid : field is protected and can only be set with withProtectedField"));
    }

    @Test
    void protectedFieldsMustActuallyBeProtectedFields() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () ->
                                EntityInstanceDraft.forEntity(entity())
                                        .withProtectedField("name", "Connie"));

        Assertions.assertTrue(
                error.getMessage()
                        .contains(
                                "name : field is not protected and should be set with withField"));
    }

    @Test
    void protectedAutoIncrementFieldsValidateSuppliedValuesAgainstIntegerType() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () ->
                                EntityInstanceDraft.forEntity(entity())
                                        .withProtectedField("id", "not-an-int"));

        Assertions.assertTrue(
                error.getMessage().contains("id : not-an-int does not match type AUTO_INCREMENT"));
    }

    @Test
    void protectedAutoGuidFieldsValidateSuppliedValuesAgainstUuidType() {
        IllegalArgumentException error =
                assertDraftFailure(
                        () ->
                                EntityInstanceDraft.forEntity(entity())
                                        .withProtectedField("guid", "not-a-guid"));

        Assertions.assertTrue(
                error.getMessage().contains("guid : not-a-guid does not match type AUTO_GUID"));
    }

    @Test
    void protectedFieldsCanBeNullForDocumentationSnapshots() {
        EntityInstance instance =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(
                                EntityInstanceDraft.forEntity(entity())
                                        .withProtectedField("id", null));

        Assertions.assertNull(instance.getFieldValue("id").asString());
    }

    private IllegalArgumentException assertDraftFailure(final Runnable operation) {
        return Assertions.assertThrows(IllegalArgumentException.class, operation::run);
    }

    private EntityDefinition entity() {
        EntityDefinition entity = new EntityDefinition("thing", "things");
        entity.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        entity.addField(Field.is("guid", FieldType.AUTO_GUID));
        entity.addField(Field.is("name", FieldType.STRING));
        entity.addField(Field.is("estimate", FieldType.INTEGER));
        entity.addField(
                Field.is("person", FieldType.OBJECT)
                        .withField(Field.is("name", FieldType.STRING))
                        .withField(Field.is("age", FieldType.INTEGER)));
        return entity;
    }
}
