package uk.co.compendiumdev.thingifier.apiconfig;

public class ParamConfig {

    // willAllowFilteringThroughUrlParams  true/false (default: true)
    private boolean allowFilteringThroughUrlParams;

    // willEnforceFilteringThroughUrlParams true/false ie. 404 error if params when not supported (default: true)
    private boolean enforceFilteringThroughUrlParams;

    public ParamConfig(){
        allowFilteringThroughUrlParams=true;
        enforceFilteringThroughUrlParams=true;
    }

    public void setFrom(final ParamConfig forParams) {
        this.allowFilteringThroughUrlParams = forParams.willAllowFilteringThroughUrlParams();
        this.enforceFilteringThroughUrlParams = forParams.willEnforceFilteringThroughUrlParams();
    }


    public boolean setAllowFilteringThroughUrlParams(boolean allow){
        return allowFilteringThroughUrlParams=allow;
    }

    public boolean setEnforceFilteringThroughUrlParams(boolean enforce){
        return enforceFilteringThroughUrlParams=enforce;
    }

    public boolean willAllowFilteringThroughUrlParams(){
        return allowFilteringThroughUrlParams;
    }

    public boolean willEnforceFilteringThroughUrlParams(){
        return enforceFilteringThroughUrlParams;
    }

}
