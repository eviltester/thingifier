package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

final class RelationshipWriteIntent {

    private final RelationshipWriteOperation operation;
    private final List<NamedValue> childReferenceFields;

    private RelationshipWriteIntent(
            final RelationshipWriteOperation operation,
            final List<NamedValue> childReferenceFields) {
        this.operation = operation;
        this.childReferenceFields =
                Collections.unmodifiableList(new ArrayList<>(childReferenceFields));
    }

    static RelationshipWriteIntent of(
            final RelationshipWriteOperation operation,
            final List<NamedValue> childReferenceFields) {
        return new RelationshipWriteIntent(operation, childReferenceFields);
    }

    static RelationshipWriteIntent none() {
        return new RelationshipWriteIntent(null, List.of());
    }

    RelationshipWriteOperation operation() {
        return operation;
    }

    List<NamedValue> childReferenceFields() {
        return childReferenceFields;
    }
}
