package uk.co.compendiumdev.thingifier.apiconfig;

import java.util.Map;

public class ApiDocsConfig {
    private String headerSectionAppend;
    private String headerSectionOverride;
    private String apiIntroductionParaOverride;

    public ApiDocsConfig(){
        headerSectionAppend = "";
        headerSectionOverride= "";
        apiIntroductionParaOverride = "";

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

    public String apiIntroductionParaOverride() {
        return "";
    }

    public ApiDocsConfig setApiIntroductionParaOverride(String introductionParaOverride) {
        apiIntroductionParaOverride = introductionParaOverride;
        return this;
    }
}
