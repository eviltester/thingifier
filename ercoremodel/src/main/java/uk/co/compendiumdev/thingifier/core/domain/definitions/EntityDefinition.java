package uk.co.compendiumdev.thingifier.core.domain.definitions;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.InstanceFields;

import java.util.*;



public class EntityDefinition {
    private int maxInstanceCount; // use -ve for no limit
    private final String name;
    private final String plural;

    // TODO: consider adding candidate keys e.g. guid or id
    // TODO: consider adding composite keys e.g. name and id
    private Field primaryKeyField;

    private final DefinedFields fields;
    private final DefinedRelationships definedRelationships;

    private final static int NO_INSTANCE_LIMIT = -1;
    public EntityDefinition(String name, String plural) {
        this(name,plural,EntityDefinition.NO_INSTANCE_LIMIT);
    }

    public EntityDefinition(String name, String plural, int maxInstanceCount) {


        this.name = name;
        this.plural = plural;
        definedRelationships = new DefinedRelationships();
        fields = new DefinedFields();
        this.maxInstanceCount = maxInstanceCount;

        // todo: add some validation to report against no primary key having been defined

        this.primaryKeyField = null;
    }

    public EntityDefinition addAsPrimaryKeyField(Field aField){
        fields.addField(aField);
        primaryKeyField = aField;
        return this;
    }

    public String toString() {
        return "\t" + name + "\n" + fields.toString();
    }


    public String getName() {
        return name;
    }


    public String getPlural() {
        return plural;
    }


    public void addField(Field aField) {
        fields.addField(aField);
    }

    public List<String> getFieldNames() {
        return fields.getFieldNames();
    }

    public boolean hasFieldNameDefined(String fieldName) {
        return fields.hasFieldNameDefined(fieldName);
    }

    public EntityDefinition addFields(Field... theseFields) {
        fields.addFields(theseFields);
        return this;
    }

    public Field getField(String fieldName) {
        return fields.getField(fieldName);
    }

    public List<Field> getFieldsOfType(final FieldType... types) {
        return  fields.getFieldsOfType(types);
    }

    public List<String> getFieldNamesOfType(final FieldType... types) {
        return  fields.getFieldNamesOfType(types);
    }

    public DefinedRelationships related(){
        return definedRelationships;
    }

    public RelationshipVectorDefinition getNamedRelationshipTo(final String relationshipName,
                                                               final EntityDefinition entity) {

        List<RelationshipVectorDefinition> relationshipsWithThisName =
                definedRelationships.getRelationships(relationshipName);

        for (RelationshipVectorDefinition relationship : relationshipsWithThisName) {
            if (relationship.getTo() == entity) {
                return relationship;
            }
        }

        // there is no relationship with this name between the things we want
        return null;
    }

    public InstanceFields instantiateFields() {
        return new InstanceFields(fields);
    }



    public int getMaxInstanceLimit() {
        return maxInstanceCount;
    }

    public boolean hasMaxInstanceLimit() {
        return maxInstanceCount>=0;
    }

    public void setNoMaxInstanceLimit() {
        maxInstanceCount = EntityDefinition.NO_INSTANCE_LIMIT;
    }

    public boolean hasPrimaryKeyField() {
        return primaryKeyField!=null;
    }

    public Field getPrimaryKeyField() {
        return primaryKeyField;
    }

    public boolean hasAnyOfFieldNamesDefined(List<String> fieldNames) {
        for(String aFieldName : fieldNames){
            if(hasFieldNameDefined(aFieldName)){
                return true;
            }
        }
        return false;
    }

}
