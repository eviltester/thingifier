package uk.co.compendiumdev.thingifier.domain.dsl.relationship;


public final class AndCall {


    private final String nameToCallIt;

    public AndCall(final String theNameToCallIt) {
        this.nameToCallIt = theNameToCallIt;
    }

    public static AndCall it(final String nameToCallIt) {
        return new AndCall(nameToCallIt);
    }

    public String isCalled() {
        return nameToCallIt;
    }
}
