package uk.co.compendiumdev.thingifier.generic.dsl.relationship;


import uk.co.compendiumdev.thingifier.Thing;

public class Between {
    private final Thing from;
    private final Thing to;

    private Between(Thing from, Thing to) {
        this.from= from;
        this.to = to;
    }

    public static Between things(Thing from, Thing to) {
        return new Between(from, to);
    }

    public Thing from() {
        return this.from;
    }

    public Thing to() {
        return this.to;
    }
}
