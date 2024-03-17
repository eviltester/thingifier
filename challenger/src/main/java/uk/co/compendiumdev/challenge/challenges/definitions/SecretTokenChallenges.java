package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class SecretTokenChallenges {

    //    POST /secret/token with incorrect username and password credentials get 401
    public static ChallengeDefinitionData createSecretTokenNotAuthenticated401(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /secret/token (401)",
                "Issue a POST request on the `/secret/token` end point and receive 401 when Basic auth username/password is not admin/password");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authentication/post-secret-401");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "RSQGADU3SLA");
        return aChallenge;
    }

    //    POST /secret/token with correct username and password credentials get secret token 201
    public static ChallengeDefinitionData createSecretTokenAuthenticated201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /secret/token (201)",
                "Issue a POST request on the `/secret/token` end point and receive 201 when Basic auth username/password is admin/password");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authentication/post-secret-201");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "J2GQiuEfHkI");
        return aChallenge;
    }


    //    GET /secret/note with no token and 403
    public static ChallengeDefinitionData forbiddenNotAuthorized403(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /secret/note (403)",
                "Issue a GET request on the `/secret/note` end point and receive 403 when X-AUTH-TOKEN does not match a valid token");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authorization/get-secret-note-403");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "77mnUQezdas");
        return aChallenge;
    }

    //    GET /secret/note with invalid token and 401
    public static ChallengeDefinitionData invalidRequestNoAuthHeader401(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /secret/note (401)",
                "Issue a GET request on the `/secret/note` end point and receive 401 when no X-AUTH-TOKEN header present");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authorization/get-secret-note-401");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "__uZlQZ48io");
        return aChallenge;
    }

    //    GET /secret/note with token and see token
    public static ChallengeDefinitionData authorizedGet200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /secret/note (200)",
                "Issue a GET request on the `/secret/note` end point receive 200 when valid X-AUTH-TOKEN used - response body should contain the note");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authorization/get-secret-note-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "2uRpzr2OmEY");
        return aChallenge;
    }

    //    POST /secret/note with token and update secret note
    public static ChallengeDefinitionData authorizedUpdate200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /secret/note (200)",
                "Issue a POST request on the `/secret/note` end point with a note payload e.g. {\"note\":\"my note\"} and receive 200 when valid X-AUTH-TOKEN used. Note is maximum length 100 chars and will be truncated when stored.");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authorization/post-secret-note-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "A9T9yjzEOEE");
        return aChallenge;
    }

    //    POST /secret/note with invalid token and 401
    public static ChallengeDefinitionData postMissingTokenAuth401(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /secret/note (401)",
                "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 401 when no X-AUTH-TOKEN present");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authorization/post-secret-note-401-403");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "76U5TEvLzLI");
        return aChallenge;
    }

    //    POST /secret/note with no token and 403
    public static ChallengeDefinitionData postInvalidTokenAuth401(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /secret/note (403)",
                "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 403 when X-AUTH-TOKEN does not match a valid token");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authorization/post-secret-note-401-403");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "76U5TEvLzLI");
        return aChallenge;
    }

    //    GET /secret/note with bearer token authorization
    public static ChallengeDefinitionData getWithValidBearerToken200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /secret/note (Bearer)",
                "Issue a GET request on the `/secret/note` end point receive 200 when using the X-AUTH-TOKEN value as an Authorization Bearer token - response body should contain the note");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authorization/get-post-secret-note-bearer");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "8GsMTZxEItw");
        return aChallenge;
    }

    //    POST /secret/note with token and update secret note
    public static ChallengeDefinitionData postUpdateWithValidBearerToken200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /secret/note (Bearer)",
                "Issue a POST request on the `/secret/note` end point with a note payload e.g. {\"note\":\"my note\"} and receive 200 when valid X-AUTH-TOKEN value used as an Authorization Bearer token. Status code 200 received. Note is maximum length 100 chars and will be truncated when stored.");

        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/authorization/get-post-secret-note-bearer");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "8GsMTZxEItw");
        return aChallenge;
    }

}
