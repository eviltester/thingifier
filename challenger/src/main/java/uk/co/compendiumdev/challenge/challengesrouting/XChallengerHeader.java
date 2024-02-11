package uk.co.compendiumdev.challenge.challengesrouting;

import spark.Response;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.InternalHttpResponse;

public class XChallengerHeader {

    public static final String NOT_FOUND_ERROR_MESSAGE = "UNKNOWN CHALLENGER - Challenger not found";

    public static void setResultHeaderBasedOnChallenger(final Response result,
                                                        final ChallengerAuthData challenger) {

        if(result==null){
            return;
        }

        if(challenger==null){
            result.raw().setHeader("X-CHALLENGER", NOT_FOUND_ERROR_MESSAGE);
            //result.raw().setHeader("X-CHALLENGER", "Challenger not recognised");
        }else{
            result.raw().setHeader("X-CHALLENGER",challenger.getXChallenger());
        }
    }

    public static void setResultHeaderBasedOnChallenger(final Response result,
                                                        final String challengerGUID) {

        if(result==null){
            return;
        }

        if(challengerGUID==null || challengerGUID.trim().length()==0){
            result.raw().setHeader("X-CHALLENGER", NOT_FOUND_ERROR_MESSAGE);
            //result.raw().setHeader("X-CHALLENGER", "Challenger not recognised");
        }else{
            result.raw().setHeader("X-CHALLENGER",challengerGUID);
        }
    }

    public static void setResultHeaderBasedOnChallenger(final InternalHttpResponse response,
                                                        final ChallengerAuthData challenger) {

        if(response==null){
            return;
        }

        if(response.getHeaders().headerExists("X-CHALLENGER")){
            return;
        }

        if(challenger==null){
            response.setHeader("X-CHALLENGER", NOT_FOUND_ERROR_MESSAGE);
        }else{
            response.setHeader("X-CHALLENGER", challenger.getXChallenger());
        }
    }
}
