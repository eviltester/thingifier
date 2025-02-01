package uk.co.compendiumdev.challenge.practicemodes.simpleapi;

import java.util.Random;

public class RandomIsbnGenerator {

    public static String generate(){
        return generate(new Random());
    }

    public static String generate(Random random){

        String isbn13 = "xxx-x-xx-xxxxxx-x";

        while(isbn13.contains("x")){
            isbn13 = isbn13.replaceFirst("x", String.valueOf(random.nextInt(9)));
        }

        return isbn13;
    }
}
