package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class UniqueFieldTest {

    @Test
    public void byDefaultAFieldIsNotUnique(){

        EntityDefinition stringFieldEntity = new EntityDefinition("Entity", "Entities");
        stringFieldEntity.addFields(Field.is("field", FieldType.STRING));

        EntityInstance instance = new EntityInstance(stringFieldEntity);

        Assertions.assertFalse(instance.getEntity().getField("field").mustBeUnique());
    }

    @Test
    public void aFieldCanBeSetToBeUnique(){

        EntityDefinition stringFieldEntity = new EntityDefinition("Entity", "Entities");
        stringFieldEntity.addFields(Field.is("field", FieldType.STRING).setMustBeUnique(true));

        EntityInstance instance = new EntityInstance(stringFieldEntity);

        Assertions.assertTrue(instance.getEntity().getField("field").mustBeUnique());
    }

    @Test
    public void aUniqueFieldCanBeUniqueAfterATransform(){

        EntityDefinition stringFieldEntity = new EntityDefinition("Entity", "Entities");
        stringFieldEntity.addFields(
                Field.is("field", FieldType.STRING).
                setMustBeUnique(true).
                setUniqueAfterTransform(
                        (s) -> s.replace("-", "")
                ));

        EntityInstance instance = new EntityInstance(stringFieldEntity);
        instance.setValue("field", "1-2-3");

        Assertions.assertTrue(instance.getEntity().getField("field").mustBeUnique());
        Assertions.assertEquals("1-2-3",instance.getFieldValue("field").asString());
        Assertions.assertEquals("123",instance.getFieldValue("field").asUniqueComparisonString());
    }

    @Test
    public void aUniqueFieldDoesNotNeedAUniqueTransformFunction(){

        EntityDefinition stringFieldEntity = new EntityDefinition("Entity", "Entities");
        stringFieldEntity.addFields(
                Field.is("field", FieldType.STRING).
                        setMustBeUnique(true));

        EntityInstance instance = new EntityInstance(stringFieldEntity);
        instance.setValue("field", "1-2-3");

        Assertions.assertTrue(instance.getEntity().getField("field").mustBeUnique());
        Assertions.assertEquals("1-2-3",instance.getFieldValue("field").asString());
        Assertions.assertEquals("1-2-3",instance.getFieldValue("field").asUniqueComparisonString());
    }

    @Test
    public void reportErrorsInTransformationFunctionResult(){

        EntityDefinition stringFieldEntity = new EntityDefinition("Entity", "Entities");
        stringFieldEntity.addFields(
                Field.is("field", FieldType.STRING).
                        setMustBeUnique(true).
                        setUniqueAfterTransform(
                                (s) -> {throw new RuntimeException("bob");}
                        ));

        EntityInstance instance = new EntityInstance(stringFieldEntity);
        instance.setValue("field", "1-2-3");

        Assertions.assertTrue(instance.getEntity().getField("field").mustBeUnique());
        Assertions.assertEquals("1-2-3",instance.getFieldValue("field").asString());
        Assertions.assertEquals("ERROR: 1-2-3 bob",instance.getFieldValue("field").asUniqueComparisonString());
    }

}
