package uk.co.compendiumdev.thingifier.apiconfig;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public final class RelationshipWriteMethodConfig {

    private EnumSet<RelationshipWriteOperation> postOperations;
    private EnumSet<RelationshipWriteOperation> deleteOperations;
    private EnumSet<RelationshipWriteOperation> putOperations;
    private EnumSet<RelationshipWriteOperation> patchOperations;

    public RelationshipWriteMethodConfig() {
        postOperations =
                operations(
                        RelationshipWriteOperation.CREATE_AND_CONNECT,
                        RelationshipWriteOperation.CONNECT_EXISTING);
        deleteOperations = operations(RelationshipWriteOperation.DISCONNECT);
        putOperations = operations();
        patchOperations = operations();
    }

    public RelationshipWriteMethodConfig postCan(final RelationshipWriteOperation... operations) {
        postOperations = operations(operations);
        return this;
    }

    public RelationshipWriteMethodConfig deleteCan(final RelationshipWriteOperation... operations) {
        deleteOperations = operations(operations);
        return this;
    }

    public RelationshipWriteMethodConfig putCan(final RelationshipWriteOperation... operations) {
        putOperations = operations(operations);
        return this;
    }

    public RelationshipWriteMethodConfig patchCan(final RelationshipWriteOperation... operations) {
        patchOperations = operations(operations);
        return this;
    }

    public RelationshipWriteMethodConfig postNotSupported() {
        return postCan();
    }

    public RelationshipWriteMethodConfig deleteNotSupported() {
        return deleteCan();
    }

    public RelationshipWriteMethodConfig putNotSupported() {
        return putCan();
    }

    public RelationshipWriteMethodConfig patchNotSupported() {
        return patchCan();
    }

    public Set<RelationshipWriteOperation> postOperations() {
        return immutableCopyOf(postOperations);
    }

    public Set<RelationshipWriteOperation> deleteOperations() {
        return immutableCopyOf(deleteOperations);
    }

    public Set<RelationshipWriteOperation> putOperations() {
        return immutableCopyOf(putOperations);
    }

    public Set<RelationshipWriteOperation> patchOperations() {
        return immutableCopyOf(patchOperations);
    }

    public Set<RelationshipWriteOperation> operationsFor(final RoutingVerb verb) {
        if (verb == RoutingVerb.POST) {
            return postOperations();
        }
        if (verb == RoutingVerb.DELETE) {
            return deleteOperations();
        }
        if (verb == RoutingVerb.PUT) {
            return putOperations();
        }
        if (verb == RoutingVerb.PATCH) {
            return patchOperations();
        }
        return Set.of();
    }

    public void setFrom(final RelationshipWriteMethodConfig source) {
        postOperations = copyOf(source.postOperations);
        deleteOperations = copyOf(source.deleteOperations);
        putOperations = copyOf(source.putOperations);
        patchOperations = copyOf(source.patchOperations);
    }

    static EnumSet<RelationshipWriteOperation> operations(
            final RelationshipWriteOperation... operations) {
        EnumSet<RelationshipWriteOperation> selected =
                EnumSet.noneOf(RelationshipWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        return selected;
    }

    private Set<RelationshipWriteOperation> immutableCopyOf(
            final EnumSet<RelationshipWriteOperation> operations) {
        if (operations.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(operations));
    }

    private EnumSet<RelationshipWriteOperation> copyOf(
            final EnumSet<RelationshipWriteOperation> operations) {
        if (operations.isEmpty()) {
            return operations();
        }
        return EnumSet.copyOf(operations);
    }
}
