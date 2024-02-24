package uk.co.compendiumdev.thingifier.core.domain.instances;

public class AutoIncrement {
    private int incrementBy;
    private final String name;
    int nextInt;

    public AutoIncrement(String aName, int firstValue){
        name = aName;
        nextInt = firstValue;
        incrementBy = 1;
    }

    public AutoIncrement by(int inc){
        incrementBy = inc;
        return this;
    }

    public String getName(){
        return name;
    }

    public int getCurrentValue(){
        return nextInt;
    }

    public void update(){
        nextInt = nextInt + incrementBy;
    }

    public void setNextValue(int integer) {

    }

    public void incrementToNextAbove(Integer integer) {
        nextInt = integer;
        update();
    }
}
