package uk.co.compendiumdev.thingifier.core.domain.randomdata;

import java.util.concurrent.ThreadLocalRandom;

public class RandomString {

    // https://www.lipsum.com/
    String baseString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    public String get(int length){

        int baseCount = baseString.length();

        if(length<baseCount){
            int startBetween = baseCount-length;
            if(startBetween<0){
                startBetween=0;
            }
            int startAt = ThreadLocalRandom.current().
                    nextInt(0, startBetween);
            String retString = baseString.substring(startAt, startAt+length).trim();

            while(retString.length()<length){
                retString = retString + "a";
            }
            return retString;
        }else{
            // todo generate longer one
         return baseString;
        }
    }
}
