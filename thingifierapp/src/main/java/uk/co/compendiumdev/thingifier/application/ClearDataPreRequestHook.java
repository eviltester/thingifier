package uk.co.compendiumdev.thingifier.application;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

public class ClearDataPreRequestHook implements PreRequestHook{
    private long lastReset;
    private final int minutes;
    private final Thingifier thingifier;
    private final long maxgap;

    public ClearDataPreRequestHook(final int minutes, Thingifier thingifier) {
        this.minutes = minutes;
        this.lastReset = System.currentTimeMillis();
        this.thingifier = thingifier;
        this.maxgap = minutes*60*1000;
    }

    @Override
    public boolean run(final Request request, final Response response) {
        long currentTime = System.currentTimeMillis();
        long gap = currentTime-lastReset;
        if(gap>maxgap){
            // reset the thingifier data
            this.lastReset=currentTime;
            System.out.println("Clearing all data");
            thingifier.clearAllData();
            System.out.println("Cleared all data");

            System.out.println("Adding test data");
            final Thing todo = thingifier.getThingNamed("todo");
            ThingInstance paperwork = todo.createInstance().setValue("title", "Scan my paperwork");
            todo.addInstance(paperwork);

            ThingInstance filework = todo.createInstance().setValue("title", "File my paperwork");
            todo.addInstance(filework);
        }
        return false;
    }
}
