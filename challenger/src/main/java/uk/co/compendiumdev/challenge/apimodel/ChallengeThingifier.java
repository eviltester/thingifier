package uk.co.compendiumdev.challenge.apimodel;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class ChallengeThingifier {

    public Thingifier challengeThingifier;
    public EntityDefinition challengeDefn;
    private ChallengeDefinitions challengeDefinitions;

    public ChallengeThingifier(){
        createThingifier();
    }

    private void createThingifier(){
        // fake the data storage
        this.challengeThingifier = new Thingifier();

        this.challengeThingifier.apiConfig().setApiToShowPrimaryKeyHeaderInResponse(true);
        this.challengeThingifier.apiConfig().forParams().willEnforceFilteringThroughUrlParams();
        this.challengeThingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams();
        this.challengeThingifier.apiConfig().setApiToAllowRobotsIndexingResponses(false);
        this.challengeThingifier.apiConfig().setReturnSingleGetItemsAsCollection(true);

        this.challengeDefn = this.challengeThingifier.defineThing(
                                    "challenge", "challenges");
        this.challengeDefn.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        this.challengeDefn.addFields(

                Field.is("name", FieldType.STRING).withDefaultValue("")
                    .withDescription("title to describe the challenge"),
                Field.is("description", FieldType.STRING).withDefaultValue("")
                        .withDescription("description of the challenge"),
                Field.is("status", FieldType.BOOLEAN).withDefaultValue("false")
                        .withDescription("status to track if challenges is completed")
        );
    }

    public void populateThingifierFrom(ChallengeDefinitions challengeDefinitions) {
        this.challengeDefinitions = challengeDefinitions;
        // create all instances from the definitions, then when we want to
        // set all the status codes to the specific challenger status
        ThingRepository repository = challengeThingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        for (ChallengeDefinitionData challenge : challengeDefinitions.getChallenges()) {
            EntityInstance instance = new EntityInstance(challengeDefn).
                    overrideValue("id", challenge.id).
                    setValue("name", challenge.name).
                    setValue("description", challenge.description);
            repository.addInstance(instance);
        }
    }

    public void populateThingifierFromStatus(ChallengerAuthData challenger){

        ChallengerAuthData challengerToUse = challenger;

        if(challenger == null){
            // create one just to show no progress
            challengerToUse = new ChallengerAuthData(challengeDefinitions.getDefinedChallenges());
        }

        ThingRepository repository = challengeThingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        for (ChallengeDefinitionData challenge : challengeDefinitions.getChallenges()) {
            final EntityInstance instance =
                    repository.findInstanceByFieldNameAndValue(challengeDefn, "id", challenge.id);
            instance.setValue("status",
                                        challengerToUse.statusOfChallenge(
                                                challengeDefinitions.
                                                        getChallenge(challenge.name)
                                ).toString()
            );
            repository.updateInstance(instance);
        }
    }
}
