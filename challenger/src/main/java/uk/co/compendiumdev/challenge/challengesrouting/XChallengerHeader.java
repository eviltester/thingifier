package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public class XChallengerHeader {

    public static final String NOT_FOUND_ERROR_MESSAGE =
            "UNKNOWN CHALLENGER - Challenger not found";

    public static void setResultHeaderBasedOnChallenger(
            final HttpServerResponse result, final ChallengerAuthData challenger) {

        if (result == null) {
            return;
        }

        if (challenger == null) {
            result.header("X-CHALLENGER", NOT_FOUND_ERROR_MESSAGE);
            // result.header("X-CHALLENGER", "Challenger not recognised");
        } else {
            result.header("X-CHALLENGER", challenger.getXChallenger());
        }
    }

    public static void setResultHeaderBasedOnChallenger(
            final HttpServerResponse result, final String challengerGUID) {

        if (result == null) {
            return;
        }

        if (challengerGUID == null || challengerGUID.trim().length() == 0) {
            result.header("X-CHALLENGER", NOT_FOUND_ERROR_MESSAGE);
            // result.header("X-CHALLENGER", "Challenger not recognised");
        } else {
            result.header("X-CHALLENGER", challengerGUID);
        }
    }

    public static void setResultHeaderBasedOnChallenger(
            final InternalHttpResponse response, final ChallengerAuthData challenger) {

        if (response == null) {
            return;
        }

        if (response.getHeaders().headerExists("X-CHALLENGER")) {
            return;
        }

        if (challenger == null) {
            response.setHeader("X-CHALLENGER", NOT_FOUND_ERROR_MESSAGE);
        } else {
            response.setHeader("X-CHALLENGER", challenger.getXChallenger());
        }
    }
}
