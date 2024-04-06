package uk.co.compendiumdev.challenge.practicemodes.simpleapi;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

import java.util.Random;

public class SimpleAPITestDataPopulator implements DataPopulator {

    @Override
    public void populate(final ERSchema schema, final ERInstanceData database) {

        String [] types={
                        "book",
                        "book",
                        "dvd",
                        "blu-ray",
                        "cd",
                        "cd",
                        "dvd",
                        "blu-ray"};

        EntityInstanceCollection items = database.getInstanceCollectionForEntityNamed("item");

        Random random = new Random();
        for(String type : types){
            items.createManagedInstance().
                    setValue("type", type).
                    setValue("numberinstock", String.valueOf(random.nextInt(20))).
                    setValue("isbn13", randomIsbn(random)).
                    setValue("price", String.valueOf(random.nextInt(99)) + "." + String.valueOf(random.nextInt(99)) )
            ;
        }
    }

    private String randomIsbn(Random random){

        String isbn13 = "xxx-x-xx-xxxxxx-x";

        while(isbn13.contains("x")){
            isbn13 = isbn13.replaceFirst("x", String.valueOf(random.nextInt(9)));
        }

        return isbn13;
    }
}
