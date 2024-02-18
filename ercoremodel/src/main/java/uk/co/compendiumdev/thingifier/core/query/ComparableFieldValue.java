package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

/* Because the Field Value does not have a Field Defn reference we will
   create a comparableFieldValue that maps the two together for comparisons
   ideally we would add a fieldDefn into FieldValue and this class will
   not be required because compare would be implemented on the fieldValue itself.

   This is an interim refactoring to move towards that concept.

   TODO: fieldValue should know the definition is it related to and remove this comparable class.
 */
public class ComparableFieldValue {

    private final Field fieldDefn;
    private final FieldValue fieldValue;

    public ComparableFieldValue(final Field fieldDefn, final FieldValue fieldValue) {
        this.fieldDefn = fieldDefn;
        this.fieldValue = fieldValue;
    }

    public int compareTo(final ComparableFieldValue otherValue) {

        if( fieldDefn.getType() == FieldType.AUTO_INCREMENT ||
                fieldDefn.getType() == FieldType.INTEGER ){
            int field1Value = fieldValue.asInteger();
            int field2Value = otherValue.getValue().asInteger();
            return Integer.compare(field1Value, field2Value);
        }

        if( fieldDefn.getType() == FieldType.FLOAT){
            float field1Value = fieldValue.asFloat();
            float field2Value = otherValue.getValue().asFloat();
            return Float.compare(field1Value, field2Value);
        }

        if( fieldDefn.getType() == FieldType.BOOLEAN){
            boolean field1Value = fieldValue.asBoolean();
            boolean field2Value = otherValue.getValue().asBoolean();
            return Boolean.compare(field1Value, field2Value);
        }

        if( fieldDefn.getType() == FieldType.STRING ||
                fieldDefn.getType() == FieldType.ENUM){
            String field1Value = fieldValue.asString();
            String field2Value = otherValue.getValue().asString();
            return field1Value.compareTo(field2Value);
        }

        // don't know how to handle that field type
        // so the instances are by default the same
        // TODO: FieldType.OBJECT, FieldType.DATE
        return 0;
    }

    public FieldValue getValue() {
        return fieldValue;
    }
}
