package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;

final class RepositoryUrlQueryTestSupport {

    private RepositoryUrlQueryTestSupport() {
    }

    static RepositoryUrlQuery query(final EntityRelModel model, final String query) {
        return new RepositoryUrlQuery(
                model.getSchema(),
                model.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME),
                query);
    }
}
