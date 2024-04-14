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

    @Deprecated // we probably want to use getNextValueAndUpdate
    public int getCurrentValue(){
        //TODO: have a list of free items, used prior to the nextInt
        // e.g. on DELETE, or if we do not create an item, or if we skip items during an increment on PUT
        return nextInt;
    }

    private synchronized void  update(){
        nextInt = nextInt + incrementBy;
    }

    public synchronized int getNextValueAndUpdate(){
        int curr = getCurrentValue();
        update();
        return curr;
    }

    public synchronized void incrementToNextAbove(Integer integer) {
        nextInt = integer;
        update();
    }
}
