package uk.co.compendiumdev.challenge.practicemodes.simpleapi;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.InMemoryThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

import java.util.Random;

public class SimpleAPITestDataPopulator implements RepositoryDataPopulator {

    @Override
    public void populate(final ERSchema schema, final ERInstanceData database) {
        populate(schema, new InMemoryThingRepository("__simple-api-populator", database));
    }

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
            EntityInstance instance = new EntityInstance(item).
                    setValue("type", type).
                    setValue("numberinstock", String.valueOf(random.nextInt(20))).
                    setValue("isbn13", randomIsbn(random)).
                    setValue("price", String.valueOf(random.nextInt(99)) + "." + String.valueOf(random.nextInt(99)) );
            repository.addInstance(instance);
        }
    }

    private String randomIsbn(Random random){
        return RandomIsbnGenerator.generate(random);
    }
}
