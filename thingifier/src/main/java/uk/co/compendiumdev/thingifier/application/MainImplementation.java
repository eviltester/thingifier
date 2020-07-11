package uk.co.compendiumdev.thingifier.application;

import spark.Spark;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.application.httprequestHooks.ClearDataPreRequestHook;
import uk.co.compendiumdev.thingifier.application.httprequestHooks.LogTheRequestHook;
import uk.co.compendiumdev.thingifier.application.httprequestHooks.LogTheResponseHook;
import uk.co.compendiumdev.thingifier.application.routehandlers.ShutdownRouteHandler;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUI;

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
    private List<RoutingDefinition> additionalRoutes;
    private Thingifier thingifier;
    ThingifierRestServer restServer;
    private String[] args;
    // prevent shutdown verb as configurable through arguments e.g. -noshutdown
    Boolean allowShutdown;
    // clear data every 10 minutes configuragle through arguments e.g. -autocleardown
    // -autocleardown=15
    Boolean clearDataPeriodically;
    int clearDownMinutes;
    // impact the logging detail -verbose
    boolean verboseMode;

    public MainImplementation(){

        // added to support heroku as per https://sparktutorials.github.io/2015/08/24/spark-heroku.html
        // environment can override config for port
        if (hasHerokuAssignedPort()) {
            proxyport = getHerokuAssignedPort();
        }

        additionalRoutes =  new ArrayList<>();
        thingifierModels = new HashMap<>();
        defaultModelName="";
        proxyport = 4567; // default for spark

        staticFilePath = "/public"; // for built in styles
        allowShutdown = true;
        clearDataPeriodically = false;
        clearDownMinutes=10;
        verboseMode=false;
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
            additionalRoutes.addAll(
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
        additionalRoutes.addAll(routes);
    }

    public void setupDefaultGui() {

        additionalRoutes.addAll(
            new DefaultGUI(thingifier).
                configureRoutes().
                getRoutes()
        );
    }

    public ThingifierRestServer startRestServer() {

        if(thingifier==null){
            throw new RuntimeException("No Thingifier Model Setup");
        }

        restServer = new ThingifierRestServer(
                args, "",
                thingifier,
                additionalRoutes);

        System.out.println("Running on " + Spark.port());
        System.out.println(" e.g. http://localhost:" + Spark.port());

        return restServer;
    }

    public void addBuiltInArgConfiguredHooks() {

        if(clearDataPeriodically) {
            restServer.registerPreRequestHook(
                    new ClearDataPreRequestHook(clearDownMinutes, thingifier));
        }

        if(verboseMode){
            restServer.registerPreRequestHook(
                    new LogTheRequestHook());
            restServer.registerPreRequestHook(
                    new LogTheResponseHook());
        }
    }
}
