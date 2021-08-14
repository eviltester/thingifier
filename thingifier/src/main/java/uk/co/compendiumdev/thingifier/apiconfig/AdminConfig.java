package uk.co.compendiumdev.thingifier.apiconfig;

public class AdminConfig {

    private boolean allowAdminSearch = false;
    private String adminSearchUrl = "/admin/query/*";
    private boolean allowAdminClearData = false;
    private String adminClearDataUrl = "/admin/data/thingifier";

    public void setFrom(final AdminConfig adminConfig) {
        this.allowAdminSearch=adminConfig.isAdminSearchAllowed();
        this.allowAdminClearData=adminConfig.isAdminDataClearAllowed();
        this.adminSearchUrl=adminConfig.getAdminSearchUrl();
        this.adminClearDataUrl = adminConfig.getAdminDataClearUrl();
    }

    public String getAdminDataClearUrl() {
        return adminClearDataUrl;
    }

    public String getAdminSearchUrl() {
        return adminSearchUrl;
    }

    public boolean isAdminDataClearAllowed() {
        return allowAdminClearData;
    }

    public boolean isAdminSearchAllowed() {
        return allowAdminSearch;
    }

    public void enableAdminSearch() {
        allowAdminSearch=true;
    }

    public void enableAdminDataClear() {
        allowAdminClearData=true;
    }
}
