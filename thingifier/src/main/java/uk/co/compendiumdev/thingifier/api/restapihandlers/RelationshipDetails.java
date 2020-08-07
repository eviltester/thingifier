package uk.co.compendiumdev.thingifier.api.restapihandlers;

class RelationshipDetails {

    public final String relationshipName;
    public final String toType;
    public final String guidName;
    public final String guidValue;

    public RelationshipDetails(final String relationshipName, final String toType, final String keyName, final String keyValue) {
        this.relationshipName = relationshipName;
        this.toType = toType;
        this.guidName = keyName;
        this.guidValue = keyValue;
    }
}
