package uk.co.compendiumdev.thingifier.core.domain.definitions;

import java.util.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.EntityDomainValidator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.InstanceValidator;
import uk.co.compendiumdev.thingifier.core.domain.instances.InstanceFields;

public class EntityDefinition {
    private int maxInstanceCount; // use -ve for no limit
    private final String name;
    private final String plural;
    private String description;

    // TODO: consider adding candidate keys e.g. guid or id
    // TODO: consider adding composite keys e.g. name and id
    private Field primaryKeyField;

    private final DefinedFields fields;
    private final DefinedRelationships definedRelationships;
    private final Map<String, EntityViewDefinition> views;
    private final List<InstanceValidator> instanceValidators;
    private final List<EntityDomainValidator> domainValidators;

    private static final int NO_INSTANCE_LIMIT = -1;

    public EntityDefinition(String name, String plural) {
        this(name, plural, NO_INSTANCE_LIMIT);
    }

    public EntityDefinition(String name, String plural, int maxInstanceCount) {

        this.name = name;
        this.plural = plural;
        definedRelationships = new DefinedRelationships();
        fields = new DefinedFields();
        views = new HashMap<>();
        instanceValidators = new ArrayList<>();
        domainValidators = new ArrayList<>();
        this.maxInstanceCount = maxInstanceCount;
        this.description = "";

        // todo: add some validation to report against no primary key having been defined

        this.primaryKeyField = null;
    }

    public EntityDefinition addAsPrimaryKeyField(Field aField) {
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

    public EntityDefinition withDescription(final String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public String getDescription() {
        return description;
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

    /**
     * Adds a cross-field validator that only needs the candidate instance.
     *
     * <p>Instance validators run after field validation and uniqueness pass. They are intentionally
     * scoped to one candidate instance; use {@link #withDomainValidation(EntityDomainValidator)}
     * when a rule for this entity needs the active schema or store.
     *
     * @param validationRule validator to add to this entity definition
     * @return this entity definition for fluent model setup
     */
    public EntityDefinition withInstanceValidation(final InstanceValidator validationRule) {
        instanceValidators.add(validationRule);
        return this;
    }

    /**
     * Adds cross-field validators that only need the candidate instance.
     *
     * @param validationRule validators to add to this entity definition
     * @return this entity definition for fluent model setup
     */
    public EntityDefinition withInstanceValidation(final InstanceValidator... validationRule) {
        instanceValidators.addAll(Arrays.asList(validationRule));
        return this;
    }

    /**
     * Returns the instance validators attached to this entity.
     *
     * @return immutable list of instance validators
     */
    public List<InstanceValidator> instanceValidators() {
        return Collections.unmodifiableList(instanceValidators);
    }

    /**
     * Adds an entity domain validator for rules that belong to this entity but need domain access.
     *
     * <p>Entity domain validators run after field, uniqueness, and instance validation have passed.
     * They receive the active schema and store, but only execute for writes to this entity
     * definition. This is the right home for entity-owned rules that must compare against other
     * instances or relationships.
     *
     * @param validationRule validator to add to this entity definition
     * @return this entity definition for fluent model setup
     */
    public EntityDefinition withDomainValidation(final EntityDomainValidator validationRule) {
        domainValidators.add(validationRule);
        return this;
    }

    /**
     * Adds entity domain validators for rules that belong to this entity but need domain access.
     *
     * @param validationRule validators to add to this entity definition
     * @return this entity definition for fluent model setup
     */
    public EntityDefinition withDomainValidation(final EntityDomainValidator... validationRule) {
        domainValidators.addAll(Arrays.asList(validationRule));
        return this;
    }

    /**
     * Returns the entity domain validators attached to this entity.
     *
     * @return immutable list of entity domain validators
     */
    public List<EntityDomainValidator> domainValidators() {
        return Collections.unmodifiableList(domainValidators);
    }

    public Field getField(String fieldName) {
        return fields.getField(fieldName);
    }

    public List<Field> getFieldsOfType(final FieldType... types) {
        return fields.getFieldsOfType(types);
    }

    public List<String> getFieldNamesOfType(final FieldType... types) {
        return fields.getFieldNamesOfType(types);
    }

    public DefinedRelationships related() {
        return definedRelationships;
    }

    public RelationshipVectorDefinition getNamedRelationshipTo(
            final String relationshipName, final EntityDefinition entity) {

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
        return maxInstanceCount >= 0;
    }

    public void setNoMaxInstanceLimit() {
        maxInstanceCount = NO_INSTANCE_LIMIT;
    }

    public boolean hasPrimaryKeyField() {
        return primaryKeyField != null;
    }

    public Field getPrimaryKeyField() {
        return primaryKeyField;
    }

    public boolean hasAnyOfFieldNamesDefined(List<String> fieldNames) {
        for (String aFieldName : fieldNames) {
            if (hasFieldNameDefined(aFieldName)) {
                return true;
            }
        }
        return false;
    }

    public EntityViewDefinition defineView(final String viewName) {
        final String normalizedName = viewName == null ? "" : viewName.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("View name is required");
        }
        if (views.containsKey(normalizedName)) {
            throw new IllegalArgumentException(
                    String.format(
                            "View %s is already defined for entity %s", normalizedName, getName()));
        }

        final EntityViewDefinition view = new EntityViewDefinition(this, normalizedName);
        views.put(normalizedName, view);
        return view;
    }

    public boolean hasViewNamed(final String viewName) {
        return views.containsKey(viewName);
    }

    public EntityViewDefinition getViewNamed(final String viewName) {
        return views.get(viewName);
    }

    public Collection<EntityViewDefinition> getViews() {
        return Collections.unmodifiableCollection(views.values());
    }
}
