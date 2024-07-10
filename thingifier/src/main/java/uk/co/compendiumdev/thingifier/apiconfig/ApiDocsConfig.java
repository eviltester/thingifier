package uk.co.compendiumdev.thingifier.apiconfig;

import java.util.Map;

public class ApiDocsConfig {
    private String headerSectionAppend;
    private String headerSectionOverride;
    private String apiIntroductionParaOverride;
    private boolean hideOptionsVerb;

    public ApiDocsConfig(){
        headerSectionAppend = "";
        headerSectionOverride= "";
        apiIntroductionParaOverride = "";
        hideOptionsVerb=false;

    }

    public ApiDocsConfig setHeaderSectionAppend(String append){
        headerSectionAppend = append;
        return this;
    }

    public String headerSectionAppend() {
        return headerSectionAppend;
    }

    public String headerSectionOverride() {
        return headerSectionOverride;
    }

    public ApiDocsConfig setHeaderSectionOverride(String headerSection) {
        this.headerSectionOverride = headerSection;
        return this;
    }

    public ApiDocsConfig showOptionsVerb(boolean show) {
        this.hideOptionsVerb = !show;
        return this;
    }


    public String apiIntroductionParaOverride() {
        return "";
    }

    public ApiDocsConfig setApiIntroductionParaOverride(String introductionParaOverride) {
        apiIntroductionParaOverride = introductionParaOverride;
        return this;
    }

    public boolean ignoreOptionsVerb() {
        return hideOptionsVerb;
    }
}
