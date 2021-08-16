package uk.co.compendiumdev.challenge.apimodel;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class ChallengeThingifier {

    public Thingifier challengeThingifier;
    public EntityInstanceCollection challengeDefn;
    private ChallengeDefinitions challengeDefinitions;

    public ChallengeThingifier(){
        createThingifier();
    }

    private void createThingifier(){
        // fake the data storage
        this.challengeThingifier = new Thingifier();

        this.challengeThingifier.apiConfig().setResponsesToShowGuids(false);

        this.challengeDefn = this.challengeThingifier.createThing(
                                    "challenge", "challenges");

        this.challengeDefn.definition().addFields(
                Field.is("id", FieldType.ID),
                Field.is("name", FieldType.STRING).withDefaultValue(""),
                Field.is("description", FieldType.STRING).withDefaultValue(""),
                Field.is("status", FieldType.BOOLEAN).withDefaultValue("false")
        );
    }

    public void populateThingifierFrom(ChallengeDefinitions challengeDefinitions) {
        this.challengeDefinitions = challengeDefinitions;
        // create all instances from the definitions, then when we want to
        // set all the status codes to the specific challenger status
        for (ChallengeDefinitionData challenge : challengeDefinitions.getChallenges()) {
            this.challengeDefn.createManagedInstance().
                    overrideValue("id", challenge.id).
                    setValue("name", challenge.name).
                    setValue("description", challenge.description);
        }
    }

    public void populateThingifierFromStatus(ChallengerAuthData challenger){
        for (ChallengeDefinitionData challenge : challengeDefinitions.getChallenges()) {
            final EntityInstance instance = this.challengeDefn.findInstanceByGUIDorID(challenge.id);
            instance.setValue("status",
                                        challenger.statusOfChallenge(
                                                challengeDefinitions.
                                                        getChallenge(challenge.name)
                                ).toString()
            );
        }
    }
}
