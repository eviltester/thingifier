package uk.co.compendiumdev.challenge.practicemodes.simpleapi;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

import java.util.Random;

public class SimpleAPITestDataPopulator implements RepositoryDataPopulator {

    @Override
    public void populate(final ERSchema schema, final ThingRepository repository) {
        String [] types={
                        "book",
                        "book",
                        "dvd",
                        "blu-ray",
                        "cd",
                        "cd",
                        "dvd",
                        "blu-ray"};

        EntityDefinition item = schema.getEntityDefinitionNamed("item");

        Random random = new Random();
        for(String type : types){
            repository.createInstance(
                    EntityInstanceDraft.forEntity(item).
                            withField("type", type).
                            withField("numberinstock", String.valueOf(random.nextInt(20))).
                            withField("isbn13", randomIsbn(random)).
                            withField("price", String.valueOf(random.nextInt(99)) + "." + random.nextInt(99)));
        }
    }

    private String randomIsbn(Random random){
        return RandomIsbnGenerator.generate(random);
    }
}
