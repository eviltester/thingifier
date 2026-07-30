package uk.co.compendiumdev.thingifier.core.query;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class EntityInstanceListPaginator {

    private final PaginationParams paginationParams;

    public EntityInstanceListPaginator(final QueryFilterParams queryParams) {
        paginationParams = new PaginationParams(queryParams);
    }

    public List<EntityInstance> paginate(final List<EntityInstance> foundItems) {
        if (paginationParams.hasValidationError()) {
            throw new IllegalArgumentException(paginationParams.validationError());
        }

        List<EntityInstance> items = new ArrayList<>(foundItems);
        if (!paginationParams.hasLimit() && !paginationParams.hasOffset()) {
            return items;
        }

        int limit = paginationParams.limitOr(items.size());
        int offset = paginationParams.offsetOr(0);

        if (limit == 0 || offset >= items.size()) {
            return new ArrayList<>();
        }

        int toIndex = (int) Math.min((long) offset + limit, items.size());
        return new ArrayList<>(items.subList(offset, toIndex));
    }
}
