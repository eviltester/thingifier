package uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;

public class ClearDataPreSparkRequestHook implements SparkRequestResponseHook {
    private long lastReset;
    private final Thingifier thingifier;
    private final long maxgap;

    public ClearDataPreSparkRequestHook(final int minutes, Thingifier thingifier) {
        this.lastReset = System.currentTimeMillis();
        this.thingifier = thingifier;
        this.maxgap = minutes*60*1000;
    }

    @Override
    public void run(final Request request, final Response response) {
        long currentTime = System.currentTimeMillis();
        long gap = currentTime-lastReset;
        if(gap>maxgap){
            // reset the thingifier data
            this.lastReset=currentTime;
            System.out.println("Clearing all data");
            thingifier.clearAllData();
            System.out.println("Cleared all data");
            System.out.println("Adding test data");
            thingifier.generateData();
            System.out.println("Added test data");
        }
    }
}
