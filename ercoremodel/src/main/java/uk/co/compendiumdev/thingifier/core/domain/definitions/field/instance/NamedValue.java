package uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance;

public class NamedValue {

    public final String name;
    public final String value;

    public NamedValue(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String asString() {
        return value;
    }
}
