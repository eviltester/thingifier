package uk.co.compendiumdev.challenger.restassured.api;

import io.restassured.RestAssured;
import uk.co.compendiumdev.challenger.payloads.Challenge;
import uk.co.compendiumdev.challenger.payloads.Challenges;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;

import static uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest.xChallenger;

public class ChallengesStatus {


    private Challenges challengeStatuses;

    public ChallengesStatus(){
        challengeStatuses = new Challenges();
        challengeStatuses.challenges = new ArrayList<>();
    }

    public List<Challenge> get(){

        challengeStatuses = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(Environment.getEnv("/challenges")).
                then().
                statusCode(200).
                and().extract().response().as(Challenges.class);

        return challengeStatuses.challenges;
    }

    public Challenge getChallengeNamed(String name){
        for(Challenge challenge : challengeStatuses.challenges){
            if(challenge.name.equals(name)){
                return challenge;
            }
        }
        return null;
    }
}
