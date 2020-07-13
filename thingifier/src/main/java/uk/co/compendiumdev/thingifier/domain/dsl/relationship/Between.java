package uk.co.compendiumdev.thingifier.domain.dsl.relationship;


import uk.co.compendiumdev.thingifier.Thing;

public final class Between {
    private final Thing from;
    private final Thing to;

    private Between(final Thing fromThing, final Thing toThing) {
        this.from = fromThing;
        this.to = toThing;
    }

    public static Between things(final Thing from, final Thing to) {
        return new Between(from, to);
    }

    public Thing from() {
        return this.from;
    }

    public Thing to() {
        return this.to;
    }
}
