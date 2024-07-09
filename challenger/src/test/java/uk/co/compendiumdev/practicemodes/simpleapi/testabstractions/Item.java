package uk.co.compendiumdev.practicemodes.simpleapi.testabstractions;

import java.util.Random;

public class Item {

    public Integer id;
    public Float price;
    public Integer numberinstock;
    public String isbn13;
    public String type;


    public static String randomIsbn(Random random){

        String isbn13 = "xxx-x-xx-xxxxxx-x";

        while(isbn13.contains("x")){
            isbn13 = isbn13.replaceFirst("x", String.valueOf(random.nextInt(9)));
        }

        return isbn13;
    }
}
