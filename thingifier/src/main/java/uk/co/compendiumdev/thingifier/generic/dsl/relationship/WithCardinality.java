package uk.co.compendiumdev.thingifier.generic.dsl.relationship;

import uk.co.compendiumdev.thingifier.generic.definitions.Cardinality;

public class WithCardinality {
    public static Cardinality of(String from, String to) {
        return new Cardinality(from, to);
    }
}
