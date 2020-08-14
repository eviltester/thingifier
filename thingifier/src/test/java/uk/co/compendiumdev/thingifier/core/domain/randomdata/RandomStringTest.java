package uk.co.compendiumdev.thingifier.core.domain.randomdata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RandomStringTest {

    @Test
    public void truncateStrings(){

        for(int x=0; x<100; x++){
            // check that we have no exceptions
            int desiredLength = x+10;
            String generated = new RandomString().get(desiredLength);
            System.out.println(generated);
            Assertions.assertEquals(desiredLength, generated.length());
        }
    }
}
