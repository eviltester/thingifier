package uk.co.compendiumdev.thingifier.application;

import spark.Spark;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfiles;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.ClearDataPreSparkRequestHook;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.LogTheSparkRequestHook;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.LogTheResponseHook;
import uk.co.compendiumdev.thingifier.application.routehandlers.ShutdownRouteHandler;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUI;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    common main implementation details,
    but still flexible enough to have custom
    processing per api Instance
 */
public class MainImplementation {

    Integer proxyport;
    private Map<String,Thingifier> thingifierModels;
    private String defaultModelName;
    private String staticFilePath;
    private ThingifierApiDefn apiDefn;
    private Thingifier thingifier;
    private ThingifierApiConfigProfile profileToUse;
    ThingifierRestServer restServer;
    private String[] args;
    // prevent shutdown verb as configurable through arguments e.g. -noshutdown
    boolean allowShutdown;
    // clear data every 10 minutes configuragle through arguments e.g. -autocleardown
    // -autocleardown=15
    boolean clearDataPeriodically;
    int clearDownMinutes;
    // impact the logging detail -verbose
    boolean verboseMode;

    int desiredVersionNumber;
    String desiredVersionName;

    DefaultGUIHTML guiManagement;

    public MainImplementation(){

        proxyport = 4567; // default for spark

        // added to support heroku as per https://sparktutorials.github.io/2015/08/24/spark-heroku.html
        // environment can override config for port
        if (hasHerokuAssignedPort()) {
            proxyport = getHerokuAssignedPort();
        }

        apiDefn =  new ThingifierApiDefn();
        thingifierModels = new HashMap<>();
        defaultModelName="";


        staticFilePath = "/public"; // for built in styles
        allowShutdown = true;
        clearDataPeriodically = false;
        clearDownMinutes=10;
        verboseMode=false;
        profileToUse = null;

        desiredVersionNumber=-1;
        desiredVersionName=null;

        guiManagement = new DefaultGUIHTML();
    }


    private boolean hasHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        return (processBuilder.environment().get("PORT") != null);
    }

    private int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (hasHerokuAssignedPort()) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return -1; //return default port if heroku-port isn't set (i.e. on localhost)
    }


    public void registerModel(final String modelName, final Thingifier thingifier) {
        thingifierModels.put(modelName, thingifier);
        if(defaultModelName.length()==0){
            // set this as the default
            defaultModelName = modelName;
        }
    }

    public void registerModel(final String modelName, final Thingifier thingifier,
                              final boolean asDefault) {
        registerModel(modelName, thingifier);
        // overwrite the model name
        setDefaultModelName(modelName);
    }

    public void setDefaultModelName(final String modelName){
        defaultModelName=modelName;
    }

    public void setDefaultsFromArgs(final String[] args) {

        String modelName=defaultModelName;
        this.args = args;

        System.out.println("Valid Model Names -model=");
        for(String aModelName : thingifierModels.keySet()){
            System.out.println(aModelName);
        }

        for(Map.Entry<String, Thingifier> models: thingifierModels.entrySet()){
            outputVersionDetailsFor(models.getKey(), models.getValue());
        }

        for (String arg : args) {
            System.out.println("Args: " + arg);

            if (arg.startsWith("-port")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    proxyport = Integer.parseInt(details[1].trim());
                    System.out.println("Will configure web server to use port " + proxyport);
                }
            }

            if (arg.startsWith("-model")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    String argModelName = details[1].trim();
                    if(thingifierModels.keySet().contains(argModelName)){
                        modelName = argModelName;
                        System.out.println("Will use model named " + modelName);
                    }else{
                        System.out.println(
                                String.format("Invalid model name %s, using %s",
                                        argModelName, modelName));
                    }
                }
            }

            if (arg.startsWith("-noshutdown")) {
                allowShutdown = false;
            }

            if (arg.startsWith("-verbose")) {
                verboseMode = true;
            }

            if (arg.startsWith("-autocleardown")) {
                clearDataPeriodically = true;
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    String minutes = details[1].trim();
                    try{
                        clearDownMinutes = Integer.valueOf(minutes);
                    }catch(Exception e){
                        System.out.println("Invalid minutes " + minutes + " " +e.getMessage());
                    }
                    System.out.println(String.format("Will clear down every %d minutes", clearDownMinutes));
                }
            }

            if (arg.startsWith("-version")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    desiredVersionNumber = Integer.parseInt(details[1].trim());
                    System.out.println("Argument version number provided: "+ desiredVersionNumber);
                }
            }
            if (arg.startsWith("-versionName")) {
                String[] details = arg.split("=");
                if (details != null && details.length > 1) {
                    desiredVersionName = details[1].trim();
                    System.out.println("Argument version number provided: "+ desiredVersionName);
                }
            }

        }

    }

    private void outputVersionDetailsFor(String modelName, final Thingifier aThingifier) {

        final ThingifierApiConfigProfiles profiles = aThingifier.apiConfigProfiles();

        if(profiles.countOfProfiles()>=1) {
            int version = profiles.countOfProfiles();
            System.out.println(
                    String.format(
                            "Model %s : Number of app versions available (e.g. -version=2, -versionName=profile1) are: %d",
                            modelName, version));
            final List<ThingifierApiConfigProfile> actualProfiles = profiles.getProfiles();
            int profileCount = 1;
            for(ThingifierApiConfigProfile profile : actualProfiles){
                System.out.println(
                        String.format(
                                "%d - %s : %s",
                                profileCount, profile.getName(),
                                profile.getDescription()
                        )
                );
                profileCount++;
            }
        }
    }


    public void setPort(final int port) {
        this.proxyport=port;
    }

    public void setStaticFileLocation(final String filePath) {
        this.staticFilePath = filePath;
    }

    public void setVerboseMode(boolean config){
        verboseMode=config;
    }

    public void setAutoShutdown(boolean config){
        allowShutdown=config;
    }

    public void setClearDataPeriodically(boolean config, int minutes){
        clearDataPeriodically=config;
        clearDownMinutes=minutes;
    }

    public void configurePortAndDefaultRoutes() {
        Spark.port(proxyport);
        Spark.staticFileLocation(staticFilePath);
    }

    public void setupBuiltInConfigurableRoutes() {
        if(allowShutdown) {
            apiDefn.addAdditionalRoutes(
                new ShutdownRouteHandler().
                    configureRoutes().
                    getRoutes());
        }
    }

    public Thingifier chooseThingifier() {
        if(defaultModelName!=null && defaultModelName.length()>0) {
            return chooseThingifier(defaultModelName);
        }else{
            throw new RuntimeException("No Thingifier Model Specified");
        }
    }

    public Thingifier chooseThingifier(final String thingifierName) {
        if(!thingifierModels.containsKey(defaultModelName)){
            throw new RuntimeException(
                    "Specified Thingifier Model Has Not Been Registered " +
                            defaultModelName);
        }
        System.out.println("Using model " + defaultModelName);
        this.thingifier = thingifierModels.get(defaultModelName);
        return this.thingifier;
    }

    public void addAdditionalRoutes(final List<RoutingDefinition> routes) {
        apiDefn.addAdditionalRoutes(routes);
    }

    public void setupDefaultGui() {

            new DefaultGUI(thingifier, guiManagement).
                configureRoutes();
    }

    public ThingifierRestServer startRestServer() {

        if(thingifier==null){
            throw new RuntimeException("No Thingifier Model Setup");
        }

        apiDefn.setThingifier(thingifier);

        restServer = new ThingifierRestServer( "",
                                    thingifier,
                                    apiDefn,
                                    guiManagement);

        System.out.println("Running on " + Spark.port());
        System.out.println(" e.g. http://localhost:" + Spark.port());

        return restServer;
    }

    public void addBuiltInArgConfiguredHooks() {

        if(clearDataPeriodically) {
            restServer.registerPreRequestHook(
                    new ClearDataPreSparkRequestHook(clearDownMinutes, thingifier));
        }

        if(verboseMode){
            restServer.registerPreRequestHook(
                    new LogTheSparkRequestHook());
            restServer.registerPreRequestHook(
                    new LogTheResponseHook());
        }
    }

    public void configureThingifierWithProfile() {

        // we hard coded a profile
        if(profileToUse!=null){
            thingifier.configureWithProfile(profileToUse);
            return;
        }

        final ThingifierApiConfigProfiles profiles = thingifier.apiConfigProfiles();

        // try to use the arg versions
        if(desiredVersionNumber!=-1) {
            if (desiredVersionNumber > 0 && desiredVersionNumber <= profiles.countOfProfiles()) {
                profileToUse = profiles.getProfiles().get(desiredVersionNumber - 1);
            } else {
                System.out.println("Invalid version number provided: " + desiredVersionNumber);
            }
        }

        if(desiredVersionName!=null && desiredVersionName.length()>0) {
            profileToUse = profiles.getProfile(desiredVersionName);
            if (profileToUse == null) {
                System.out.println("Invalid version name provided: " + desiredVersionName);
            }
        }

        if(profileToUse!=null) {
            System.out.println(
                    String.format(
                            "Will configure app as release version %s : %s ",
                                    profileToUse.getName(), profileToUse.getDescription()));
        }else{
            profileToUse = profiles.getDefault();
            if(profileToUse!=null){
                System.out.println(
                        String.format(
                                "Will configure app as release version %s : %s ",
                                        profileToUse.getName(), profileToUse.getDescription()));
            }
        }

        if(profileToUse!=null){
            thingifier.configureWithProfile(profileToUse);
        }else{
            String.format("Will configure app as default profile");
        }
    }

    public void setProfileToUse(ThingifierApiConfigProfile aProfile){
        profileToUse = aProfile;
    }

    public DefaultGUIHTML getGuiManagement() {
        return guiManagement;
    }

    public ThingifierApiDefn getApiDefn() {
        return apiDefn;
    }
}
