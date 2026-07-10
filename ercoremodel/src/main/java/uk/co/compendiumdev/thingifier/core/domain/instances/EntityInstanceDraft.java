package uk.co.compendiumdev.thingifier.core.domain.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.validation.EntityInstanceDraftValidator;

public final class EntityInstanceDraft {

    private static final EntityInstanceDraftValidator VALIDATOR =
            new EntityInstanceDraftValidator();

    private final EntityDefinition entityDefinition;
    private final List<NamedValue> fieldValues;
    private final List<NamedValue> protectedFieldValues;

    private EntityInstanceDraft(final EntityDefinition entityDefinition) {
        this.entityDefinition = entityDefinition;
        this.fieldValues = new ArrayList<>();
        this.protectedFieldValues = new ArrayList<>();
    }

    public static EntityInstanceDraft forEntity(final EntityDefinition entityDefinition) {
        return new EntityInstanceDraft(entityDefinition);
    }

    public static EntityInstanceDraft fromNamedValues(
            final EntityDefinition entityDefinition, final List<NamedValue> values) {
        EntityInstanceDraft draft = forEntity(entityDefinition);
        for (NamedValue value : values) {
            draft.withField(value.getName(), value.asString());
        }
        return draft;
    }

    public EntityInstanceDraft withField(final String name, final String value) {
        VALIDATOR.assertCanAddField(entityDefinition, name, value);
        fieldValues.add(new NamedValue(name, value));
        return this;
    }

    public EntityInstanceDraft withProtectedField(final String name, final String value) {
        VALIDATOR.assertCanAddProtectedField(entityDefinition, name, value);
        protectedFieldValues.add(new NamedValue(name, value));
        return this;
    }

    public EntityDefinition getEntity() {
        return entityDefinition;
    }

    public List<NamedValue> getFieldValues() {
        return Collections.unmodifiableList(fieldValues);
    }

    public List<NamedValue> getProtectedFieldValues() {
        return Collections.unmodifiableList(protectedFieldValues);
    }

    public void validate() {
        VALIDATOR.assertValid(this);
    }
}
