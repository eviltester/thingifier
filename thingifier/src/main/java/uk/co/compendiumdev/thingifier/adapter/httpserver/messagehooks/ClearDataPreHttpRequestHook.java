package uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;

public class ClearDataPreHttpRequestHook implements HttpRequestResponseHook {
    private long lastReset;
    private final Thingifier thingifier;
    private final long maxgap;

    public ClearDataPreHttpRequestHook(final int minutes, Thingifier thingifier) {
        this.lastReset = System.currentTimeMillis();
        this.thingifier = thingifier;
        this.maxgap = minutes * 60 * 1000;
    }

    @Override
    public void run(final HttpServerRequest request, final HttpServerResponse response) {
        long currentTime = System.currentTimeMillis();
        long gap = currentTime - lastReset;
        if (gap > maxgap) {
            // reset the thingifier data
            this.lastReset = currentTime;
            System.out.println("Clearing all data");
            thingifier.clearAllData();
            System.out.println("Cleared all data");
            System.out.println("Adding test data");
            thingifier.generateData(EntityRelModel.DEFAULT_DATABASE_NAME);
            System.out.println("Added test data");
        }
    }
}
