package uk.co.compendiumdev.thingifier.generic.dsl.relationship;


public class AndCall {


    private final String nameToCallIt;

    public AndCall(String nameToCallIt) {
        this.nameToCallIt = nameToCallIt;
    }

    public static AndCall it(String nameToCallIt) {
        return new AndCall(nameToCallIt);
    }

    public String isCalled() {
        return nameToCallIt;
    }
}
