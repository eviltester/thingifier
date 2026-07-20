package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreWriteException;

final class InMemoryEntityInstanceCollection {

    private final EntityDefinition definition;
    private final Map<String, EntityInstance> instances = new ConcurrentHashMap<>();

    // id's should be auto incremented at an instance collection level, not on the field definitions
    private final Map<String, AutoIncrement> counters = new ConcurrentHashMap<>();

    InMemoryEntityInstanceCollection(EntityDefinition thingDefinition) {
        this.definition = thingDefinition;
        ensureCountersInitialized();
    }

    private void ensureCountersInitialized() {
        for (Field fieldDefn :
                definition.getFieldsOfType(FieldType.AUTO_GUID, FieldType.AUTO_INCREMENT)) {
            if (fieldDefn.getType() == FieldType.AUTO_INCREMENT) {
                createCounterFor(fieldDefn);
            }
        }
    }

    private AutoIncrement createCounterFor(Field fieldDefn) {
        if (!counters.containsKey(fieldDefn.getName())) {
            AutoIncrement auto =
                    new AutoIncrement(fieldDefn.getName(), fieldDefn.getDefaultValue().asInteger());
            counters.put(fieldDefn.getName(), auto);
            return auto;
        } else {
            return counters.get(fieldDefn.getName());
        }
    }

    InMemoryEntityInstanceCollection(
            final EntityDefinition entity, final List<EntityInstance> instances) {
        this.definition = entity;
        ensureCountersInitialized();
        addInstances(instances);
    }

    InMemoryEntityInstanceCollection addInstances(List<EntityInstance> addInstances) {

        if (definition.hasMaxInstanceLimit()
                && ((instances.size() + addInstances.size()) > definition.getMaxInstanceLimit())) {
            throw ThingStoreWriteException.maxInstanceLimitWouldBeExceeded(definition);
        }

        for (EntityInstance instance : addInstances) {
            addInstance(instance);
        }

        return this;
    }

    EntityInstance addInstance(final MutableEntityInstance mutableInstance) {
        EntityInstance prepared = prepareInstanceForInsert(mutableInstance);
        return addInstance(prepared);
    }

    EntityInstance prepareInstanceForInsert(final MutableEntityInstance mutableInstance) {
        ensureCountersInitialized();

        if (mutableInstance.getEntity() != definition) {
            throw ThingStoreWriteException.wrongEntityType(definition, mutableInstance.getEntity());
        }

        if (definition.hasMaxInstanceLimit()
                && instances.size() >= definition.getMaxInstanceLimit()) {
            throw ThingStoreWriteException.maxInstanceLimitReached(definition);
        }

        // if there are any AUTO_GUIDs or AUTO-INCREMENTs not set in the instance, then set them now
        for (Field fieldDefn :
                definition.getFieldsOfType(FieldType.AUTO_GUID, FieldType.AUTO_INCREMENT)) {
            if (!mutableInstance.hasInstantiatedFieldNamed(fieldDefn.getName())) {
                // set it here using the counter for the field
                if (fieldDefn.getType() == FieldType.AUTO_GUID) {
                    mutableInstance.setValue(fieldDefn.getName(), UUID.randomUUID().toString());
                }
                if (fieldDefn.getType() == FieldType.AUTO_INCREMENT) {
                    AutoIncrement counter = counters.get(fieldDefn.getName());
                    if (counter == null) {
                        counter = createCounterFor(fieldDefn);
                    }
                    mutableInstance.overrideValue(
                            fieldDefn.getName(), String.valueOf(counter.getNextValueAndUpdate()));
                }
            }
        }

        if (definition.hasPrimaryKeyField()) {
            // check value of primary key exists and is unique
            Field primaryField = definition.getPrimaryKeyField();
            if (!mutableInstance.hasInstantiatedFieldNamed(primaryField.getName())) {
                throw ThingStoreWriteException.missingPrimaryKey(
                        definition, primaryField.getName());
            }
        }

        EntityInstance instance = mutableInstance.toEntityInstance();

        if (definition.hasPrimaryKeyField()) {

            for (EntityInstance existingInstance : instances.values()) {
                if (existingInstance.getPrimaryKeyValue().equals(instance.getPrimaryKeyValue())) {
                    throw ThingStoreWriteException.duplicatePrimaryKey(
                            definition, existingInstance.getPrimaryKeyValue());
                }
            }
        }

        return instance;
    }

    EntityInstance addInstance(EntityInstance instance) {

        ensureCountersInitialized();

        if (instance.getEntity() != definition) {
            throw ThingStoreWriteException.wrongEntityType(definition, instance.getEntity());
        }

        if (definition.hasMaxInstanceLimit()
                && instances.size() >= definition.getMaxInstanceLimit()) {
            throw ThingStoreWriteException.maxInstanceLimitReached(definition);
        }

        instances.put(instance.getInternalId(), instance);

        for (String autoIncrementFieldSet :
                instance.getEntity().getFieldNamesOfType(FieldType.AUTO_INCREMENT)) {
            // auto increment auto increments to above the value
            // should only do this if we actually add the item
            AutoIncrement counter = counters.get(autoIncrementFieldSet);
            if (counter.peekNextValue()
                    < instance.getFieldValue(autoIncrementFieldSet).asInteger()) {
                counter.incrementToNextAbove(
                        instance.getFieldValue(autoIncrementFieldSet).asInteger());
            }
        }

        return instance;
    }

    EntityInstance replaceInstance(final EntityInstance instance) {
        if (!instances.containsKey(instance.getInternalId())) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "Unable to replace, could not find a %s with internal id %s",
                            definition.getName(), instance.getInternalId()));
        }
        instances.put(instance.getInternalId(), instance);
        return instance;
    }

    public int countInstances() {
        return instances.size();
    }

    public EntityInstance findInstanceByFieldNameAndValue(String fieldName, String fieldValue) {

        if (fieldName == null) {
            return null;
        }
        if (fieldValue == null) {
            return null;
        }

        for (EntityInstance thing : instances.values()) {
            if (thing.hasFieldNamed(fieldName)) {
                if (thing.getFieldValue(fieldName).asString().contentEquals(fieldValue)) {
                    return thing;
                }
            }
        }

        return null;
    }

    public EntityInstance findInstanceByInternalID(String instanceFieldValue) {

        // first - if it is not a GUID then dump it
        try {
            UUID.fromString(instanceFieldValue);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (instances.containsKey(instanceFieldValue)) {
            return instances.get(instanceFieldValue);
        }

        return null;
    }

    public Collection<EntityInstance> getInstances() {
        return instances.values();
    }

    public void deleteInstance(String guid) {

        if (!instances.containsKey(guid)) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "unable to delete, could not find a %s with GUID %s",
                            definition.getName(), guid));
        }

        EntityInstance item = instances.get(guid);

        deleteInstance(item);
    }

    public void deleteInstance(EntityInstance anInstance) {

        if (!instances.containsValue(anInstance)) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "Unable to delete, could not find a %s with %s of %s",
                            definition.getName(),
                            definition.getPrimaryKeyField().getName(),
                            anInstance.getPrimaryKeyValue()));
        }

        instances.remove(anInstance.getInternalId());
    }

    /*

       Definition abstractions

    */

    public EntityDefinition definition() {
        return definition;
    }

    public EntityInstance findInstanceByPrimaryKey(String primaryKeyValue) {
        for (EntityInstance instance : instances.values()) {
            if (instance.getPrimaryKeyValue().equals(primaryKeyValue)) {
                return instance;
            }
        }

        return null;
    }

    public Map<String, AutoIncrement> getCounters() {
        ensureCountersInitialized();
        return counters;
    }

    Snapshot snapshot() {
        Map<String, Integer> counterValues = new HashMap<>();
        ensureCountersInitialized();
        for (Map.Entry<String, AutoIncrement> counter : counters.entrySet()) {
            counterValues.put(counter.getKey(), counter.getValue().peekNextValue());
        }
        return new Snapshot(definition, new ArrayList<>(instances.values()), counterValues);
    }

    void restore(final Snapshot snapshot) {
        instances.clear();
        for (EntityInstance instance : snapshot.instances) {
            instances.put(instance.getInternalId(), instance);
        }

        counters.clear();
        for (Map.Entry<String, Integer> counter : snapshot.counterValues.entrySet()) {
            counters.put(counter.getKey(), new AutoIncrement(counter.getKey(), counter.getValue()));
        }
    }

    public ValidationReport checkFieldsForUniqueNess(EntityInstance instance, boolean isAmendment) {

        ValidationReport report = new ValidationReport();

        for (String fieldName : instance.getEntity().getFieldNames()) {
            Field field = instance.getEntity().getField(fieldName);
            if (field.mustBeUnique()) {
                String valueThatMustBeUnique =
                        instance.getFieldValue(fieldName).asUniqueComparisonString();
                // check all instances to see if it is
                for (EntityInstance instanceToCheck : instances.values()) {
                    FieldValue existingValue = instanceToCheck.getFieldValue(fieldName);
                    if (valueThatMustBeUnique.equals(existingValue.asUniqueComparisonString())) {
                        // it is not
                        boolean dupeFound = true;
                        if (isAmendment) {
                            if (instanceToCheck
                                    .getPrimaryKeyValue()
                                    .equals(instance.getPrimaryKeyValue())) {
                                // same item so ignore this one
                                dupeFound = false;
                            }
                        }
                        if (dupeFound) {
                            report.setValid(false);
                            report.addErrorMessage(
                                    "Field %s Value is not unique".formatted(fieldName));
                            // we only need to find one to end the check
                            return report;
                        }
                    }
                }
            }
        }

        return report;
    }

    static final class Snapshot {

        final EntityDefinition definition;
        private final List<EntityInstance> instances;
        private final Map<String, Integer> counterValues;

        private Snapshot(
                final EntityDefinition definition,
                final List<EntityInstance> instances,
                final Map<String, Integer> counterValues) {
            this.definition = definition;
            this.instances = instances;
            this.counterValues = counterValues;
        }
    }
}
