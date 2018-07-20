package uk.co.compendiumdev.thingifier.generic.dsl.relationship;


public final class AndCall {


    private final String nameToCallIt;

    public AndCall(final String nameToCallIt) {
        this.nameToCallIt = nameToCallIt;
    }

    public static AndCall it(final String nameToCallIt) {
        return new AndCall(nameToCallIt);
    }

    public String isCalled() {
        return nameToCallIt;
    }
}
