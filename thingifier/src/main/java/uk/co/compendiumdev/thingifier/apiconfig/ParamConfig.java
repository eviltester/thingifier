package uk.co.compendiumdev.thingifier.apiconfig;

public class ParamConfig {

    // willAllowFilteringThroughUrlParams  true/false (default: true)
    private boolean allowFilteringThroughUrlParams;

    // willEnforceFilteringThroughUrlParams true/false ie. 404 error if params when not supported
    // (default: true)
    private boolean enforceFilteringThroughUrlParams;

    private boolean allowPagingThroughUrlParams;
    private int defaultPagingLimit;
    private int maxPagingLimit;

    public ParamConfig() {
        allowFilteringThroughUrlParams = true;
        enforceFilteringThroughUrlParams = true;
        allowPagingThroughUrlParams = true;
        defaultPagingLimit = 10;
        maxPagingLimit = 20;
    }

    public void setFrom(final ParamConfig forParams) {
        this.allowFilteringThroughUrlParams = forParams.willAllowFilteringThroughUrlParams();
        this.enforceFilteringThroughUrlParams = forParams.willEnforceFilteringThroughUrlParams();
        this.allowPagingThroughUrlParams = forParams.willAllowPagingThroughUrlParams();
        this.defaultPagingLimit = forParams.defaultPagingLimit();
        this.maxPagingLimit = forParams.maxPagingLimit();
    }

    public boolean setAllowFilteringThroughUrlParams(boolean allow) {
        return allowFilteringThroughUrlParams = allow;
    }

    public boolean setEnforceFilteringThroughUrlParams(boolean enforce) {
        return enforceFilteringThroughUrlParams = enforce;
    }

    public boolean willAllowFilteringThroughUrlParams() {
        return allowFilteringThroughUrlParams;
    }

    public boolean willEnforceFilteringThroughUrlParams() {
        return enforceFilteringThroughUrlParams;
    }

    public boolean setAllowPagingThroughUrlParams(boolean allow) {
        return allowPagingThroughUrlParams = allow;
    }

    public int setDefaultPagingLimit(final int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Default paging limit must be non-negative");
        }
        return defaultPagingLimit = limit;
    }

    public int setMaxPagingLimit(final int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Max paging limit must be non-negative");
        }
        return maxPagingLimit = limit;
    }

    public boolean willAllowPagingThroughUrlParams() {
        return allowPagingThroughUrlParams;
    }

    public int defaultPagingLimit() {
        return defaultPagingLimit;
    }

    public int maxPagingLimit() {
        return maxPagingLimit;
    }
}
