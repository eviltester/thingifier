package uk.co.compendiumdev.thingifier.domain.dsl.relationship;

import uk.co.compendiumdev.thingifier.domain.definitions.Cardinality;

public class WithCardinality {
    public static Cardinality of(String from, String to) {
        return new Cardinality(from, to);
    }
}
