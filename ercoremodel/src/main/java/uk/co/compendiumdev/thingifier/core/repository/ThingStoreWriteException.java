package uk.co.compendiumdev.thingifier.core.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public final class ThingStoreWriteException extends RuntimeException {

    public enum Reason {
        MAX_INSTANCE_LIMIT_REACHED,
        MAX_INSTANCE_LIMIT_WOULD_BE_EXCEEDED,
        DUPLICATE_PRIMARY_KEY,
        MISSING_PRIMARY_KEY,
        WRONG_ENTITY_TYPE
    }

    private final Reason reason;
    private final String entityName;
    private final Map<String, String> details;

    private ThingStoreWriteException(
            final Reason reason,
            final String entityName,
            final String message,
            final Map<String, String> details) {
        super(message);
        this.reason = reason;
        this.entityName = entityName;
        this.details = Collections.unmodifiableMap(new HashMap<>(details));
    }

    public static ThingStoreWriteException maxInstanceLimitReached(final EntityDefinition entity) {
        return new ThingStoreWriteException(
                Reason.MAX_INSTANCE_LIMIT_REACHED,
                entity.getName(),
                String.format(
                        "ERROR: Cannot add instance, maximum limit of %d reached",
                        entity.getMaxInstanceLimit()),
                Map.of("maxInstances", String.valueOf(entity.getMaxInstanceLimit())));
    }

    public static ThingStoreWriteException maxInstanceLimitWouldBeExceeded(
            final EntityDefinition entity) {
        return new ThingStoreWriteException(
                Reason.MAX_INSTANCE_LIMIT_WOULD_BE_EXCEEDED,
                entity.getName(),
                String.format(
                        "ERROR: Cannot add instances, would exceed maximum limit of %d",
                        entity.getMaxInstanceLimit()),
                Map.of("maxInstances", String.valueOf(entity.getMaxInstanceLimit())));
    }

    public static ThingStoreWriteException missingPrimaryKey(
            final EntityDefinition entity, final String fieldName) {
        return new ThingStoreWriteException(
                Reason.MISSING_PRIMARY_KEY,
                entity.getName(),
                String.format(
                        "ERROR: Cannot add instance, primary key field %s not set", fieldName),
                Map.of("fieldName", fieldName));
    }

    public static ThingStoreWriteException duplicatePrimaryKey(
            final EntityDefinition entity, final String primaryKeyValue) {
        return new ThingStoreWriteException(
                Reason.DUPLICATE_PRIMARY_KEY,
                entity.getName(),
                "ERROR: Cannot add instance, another instance with primary key value exists: "
                        + primaryKeyValue,
                Map.of("primaryKeyValue", primaryKeyValue));
    }

    public static ThingStoreWriteException wrongEntityType(
            final EntityDefinition expectedEntity, final EntityDefinition actualEntity) {
        return new ThingStoreWriteException(
                Reason.WRONG_ENTITY_TYPE,
                expectedEntity.getName(),
                String.format(
                        "ERROR: Tried to add a %s instance to the %s",
                        actualEntity.getName(), expectedEntity.getName()),
                Map.of("actualEntityName", actualEntity.getName()));
    }

    public Reason reason() {
        return reason;
    }

    public String entityName() {
        return entityName;
    }

    public Map<String, String> details() {
        return details;
    }
}
