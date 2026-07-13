package uk.co.compendiumdev.thingifier.core.repository;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public interface RelationshipRepository {

    void connect(EntityInstance from, String relationshipName, EntityInstance to);

    default List<EntityInstance> listRelated(
            final EntityInstance instance, final String relationshipName) {
        return listRelated(instance, relationshipName, new QueryFilterParams());
    }

    List<EntityInstance> listRelated(
            EntityInstance instance, String relationshipName, QueryFilterParams queryParams);

    void removeBetween(EntityInstance parent, EntityInstance child, String relationshipName);

    void disconnectBetween(EntityInstance parent, EntityInstance child, String relationshipName);

    void removeAll(EntityInstance instance);

    boolean hasRelationships(EntityInstance instance);

    ValidationReport validate(EntityInstance instance);
}
