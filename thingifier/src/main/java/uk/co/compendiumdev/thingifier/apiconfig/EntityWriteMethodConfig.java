package uk.co.compendiumdev.thingifier.apiconfig;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public final class EntityWriteMethodConfig {

    private EnumSet<EntityWriteOperation> postOperations;
    private EnumSet<EntityWriteOperation> putOperations;
    private EnumSet<EntityPatchUpdateStyle> patchUpdateStyles;

    public EntityWriteMethodConfig() {
        postOperations = operations(EntityWriteOperation.CREATE, EntityWriteOperation.UPDATE);
        putOperations = operations(EntityWriteOperation.CREATE, EntityWriteOperation.UPDATE);
        patchUpdateStyles = patchStyles();
    }

    public EntityWriteMethodConfig postCan(final EntityWriteOperation... operations) {
        postOperations = operations(operations);
        return this;
    }

    public EntityWriteMethodConfig putCan(final EntityWriteOperation... operations) {
        putOperations = operations(operations);
        return this;
    }

    public EntityWriteMethodConfig patchCan(final EntityPatchUpdateStyle... styles) {
        patchUpdateStyles = patchStyles(styles);
        return this;
    }

    public EntityWriteMethodConfig postNotSupported() {
        return postCan();
    }

    public EntityWriteMethodConfig putNotSupported() {
        return putCan();
    }

    public EntityWriteMethodConfig patchNotSupported() {
        return patchCan();
    }

    public Set<EntityWriteOperation> postOperations() {
        return immutableCopyOf(postOperations);
    }

    public Set<EntityWriteOperation> putOperations() {
        return immutableCopyOf(putOperations);
    }

    public Set<EntityPatchUpdateStyle> patchUpdateStyles() {
        return immutablePatchStyleCopyOf(patchUpdateStyles);
    }

    public Set<EntityWriteOperation> operationsFor(final RoutingVerb verb) {
        if (verb == RoutingVerb.POST) {
            return postOperations();
        }
        if (verb == RoutingVerb.PUT) {
            return putOperations();
        }
        return Set.of();
    }

    public void setFrom(final EntityWriteMethodConfig source) {
        postOperations = copyOf(source.postOperations);
        putOperations = copyOf(source.putOperations);
        patchUpdateStyles = patchStyleCopyOf(source.patchUpdateStyles);
    }

    static EnumSet<EntityWriteOperation> operations(final EntityWriteOperation... operations) {
        EnumSet<EntityWriteOperation> selected = EnumSet.noneOf(EntityWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        return selected;
    }

    static EnumSet<EntityPatchUpdateStyle> patchStyles(final EntityPatchUpdateStyle... styles) {
        EnumSet<EntityPatchUpdateStyle> selected = EnumSet.noneOf(EntityPatchUpdateStyle.class);
        if (styles != null) {
            Collections.addAll(selected, styles);
        }
        return selected;
    }

    private Set<EntityWriteOperation> immutableCopyOf(
            final EnumSet<EntityWriteOperation> operations) {
        if (operations.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(operations));
    }

    private EnumSet<EntityWriteOperation> copyOf(final EnumSet<EntityWriteOperation> operations) {
        if (operations.isEmpty()) {
            return operations();
        }
        return EnumSet.copyOf(operations);
    }

    private Set<EntityPatchUpdateStyle> immutablePatchStyleCopyOf(
            final EnumSet<EntityPatchUpdateStyle> styles) {
        if (styles.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(styles));
    }

    private EnumSet<EntityPatchUpdateStyle> patchStyleCopyOf(
            final EnumSet<EntityPatchUpdateStyle> styles) {
        if (styles.isEmpty()) {
            return patchStyles();
        }
        return EnumSet.copyOf(styles);
    }
}
