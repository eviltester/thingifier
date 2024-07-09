package uk.co.compendiumdev.challenge.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.*;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.challenge.persistence.PersistenceResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

import java.time.LocalDate;
import java.util.*;

import static spark.Spark.*;

public class ChallengerWebGUI {

    private final PageNotFoundResponse pageNotFoundHtmlResponse;
    Logger logger = LoggerFactory.getLogger(ChallengerWebGUI.class);
    private final DefaultGUIHTML guiManagement;
    private final boolean guiStayAlive;

    public ChallengerWebGUI(final DefaultGUIHTML defaultGui, final boolean guiStayAlive) {
        this.guiManagement = defaultGui;
        this.guiStayAlive = guiStayAlive;
        this.pageNotFoundHtmlResponse = new PageNotFoundResponse(guiManagement);
    }

    String getChallengesPageHtmlHeader(){
        return guiManagement.getPageStart(
                "API Challenges - Improve your API Skills",
                "<script src='/js/challengerui.js'></script>" +
                "<meta name='description' content='A free online set of gamified REST API Challenges to practice and improve your API Testing Skills'>",
                "/gui/challenges");
    }

    public void setup(final Challengers challengers, 
                      final ChallengeDefinitions challengeDefinitions, 
                      final PersistenceLayer persistenceLayer, 
                      final boolean single_player_mode) {

        guiManagement.appendMenuItem("Home", "/");
        guiManagement.appendMenuItem("Entities Explorer", "/gui/entities");
        guiManagement.appendMenuItem("Challenges", "/gui/challenges");
        guiManagement.appendMenuItem("API documentation","/docs");
        guiManagement.appendMenuItem("Learning", "/learning");


        String actualMenu = """
                <script>
                    function setMenuNavBasedOnUrl(){
                        const pathToCheck = window.location.pathname;
                        urlMapping = [
                            ['/simpleapi/', 'simple-api-root-menu'],
                            ['/practice-modes/simpleapi', 'simple-api-root-menu'],
                            ['/gui/', 'api-challenges-root-menu'],
                            ['/docs', 'api-challenges-root-menu'],
                            ['/apichallenges', 'api-challenges-root-menu'],
                            ['/sim/docs', 'sim-api-root-menu'],
                            ['/practice-modes/simulation', 'sim-api-root-menu'],
                            ['/mirror/', 'mirror-api-root-menu'],
                            ['/practice-modes/mirror', 'mirror-api-root-menu']
                        ];
                        foundMapping = false;
                        for(const mapping of urlMapping){
                            if(pathToCheck.startsWith(mapping[0])){
                                document.getElementById(mapping[1]).classList.add('dropped');
                                foundMapping=true;
                                break;
                            }
                        }
                        if(!foundMapping && pathToCheck=='/'){
                            document.getElementById('home-root-menu').classList.add('dropped');
                            foundMapping=true;
                        }
                        if(!foundMapping){
                            // assume it is a learning page
                            document.getElementById('learning-root-menu').classList.add('dropped');
                        }
                    }
                    document.addEventListener("DOMContentLoaded", setMenuNavBasedOnUrl);
                </script>
                <div class="container cssmenu">
                <nav aria-label="Site menu">
                    <div class="css-menu">
                        <ul class="sub-menu">
                            <li id='home-root-menu'><a class="brand-link" href="/">Home</a></li>

                            <li id='learning-root-menu'><a href="/learning">Learning Zone</a>
                                <!-- TODO include a sample of learning information -->
                            </li>
                            
                            <li id='simple-api-root-menu'><a href="/practice-modes/simpleapi">Simple API</a>
                                <ul>
                                    <li><a href="/practice-modes/simpleapi">About Simple API</a>
                                    <li><a href="/simpleapi/docs">API Docs</a>
                                    <li><a href="/simpleapi/gui/entities">Data Explorer</a></li>
                                    <li><a href="/simpleapi/docs/swagger">[Download Open API File]</a>
                                </ul>
                            </li>
                        
                            <li id='api-challenges-root-menu'><a href="/gui/challenges">API Challenges</a>
                                <ul>
                                    <li><a href="/apichallenges">About API Challenges</a></li>
                                    <li><a href="/docs">API Docs</a></li>
                                    <li><a href="/gui/challenges">Progress</a></li>
                                    <li><a href="/gui/entities">Data Explorer</a></li>
                                    <li><a href="/apichallenges/solutions">Solutions</a></li>
                                    <li><a href="/docs/swagger">[Download Open API File]</a>
                                </ul>
                            </li>
                       
                            <li id='sim-api-root-menu'><a href="/practice-modes/simulation">API Simulator</a>
                                <ul>
                                    <li><a href="/practice-modes/simulation">About API Simulator</a></li>
                                    <li><a href="/sim/docs">API Docs</a></li>
                                    <li><a href="/sim/docs/swagger">[Download Open API File]</a></li>
                                    
                                </ul>
                            </li>
                        
                            <li id='mirror-api-root-menu'><a href="/practice-modes/mirror">HTTP Mirror</a>
                                <ul>
                                    <li><a href="/practice-modes/mirror">About HTTP Mirror</a></li>
                                    <li><a href="/mirror/docs">Mirror API Docs</a></li>
                                    <li><a href="/mirror/docs/swagger">[Download Open API File]</a></li>
                                    
                                </ul>
                            </li>
                        </ul>
                    </div>
                </nav>
                </div>
        """.stripIndent();

        guiManagement.setActualMenuHtml(actualMenu);

        // Add the Default GUI Endpoiints for entity exploration


        guiManagement.setHomePageContent("""
                    <h2 id="challenges">Challenges</h2>
                    <p>The challenges can be completed by issuing API requests to the API.</p>
                    <p>e.g. <code>GET http://localhost:4567/todos</code> would complete the challenge to &quot;GET the list of todos&quot;</p>
                    <p>You can also <code>GET http://localhost:4567/challenges</code> to get the list of challenges and their status as an API call. </p>
                """
        );

        guiManagement.setFooter(getChallengesFooter());



        // could redirect to eviltester.com if the canonical doesn't change the search indexing from heroku to eviltester
//        before((request, response) -> {
//
//                    if (request.requestMethod().equals("GET")) {
//                        if (request.url().startsWith("http://apichallenges.herokuapp.com") ||
//                                request.url().startsWith("https://apichallenges.herokuapp.com")
//                        ) {
//                            // and it is a browser request
//                            if (request.headers("accept").contains("text/html")) {
//                                // then redirect
//                                response.header("location", "https://apichallenges.eviltester.com" + request.uri());
//                                halt(301);
//                            }
//                        } else {
//                            System.out.println("nothing to see here");
//                        }
//                    }
//                }
//        );

        ResourceContentScanner contentScanner = new ResourceContentScanner();
        List<String> pathsToFileContent = contentScanner.scanForFullPathsOfExtensionsIn("content/", "md");
        contentScanner.addPathsToAvailableContent(pathsToFileContent);

        // add an endpoint for each markdown content file
        MarkdownContentManager contentManager = new MarkdownContentManager(pathsToFileContent, guiManagement);
        for(String pathToMarkdownFile : pathsToFileContent){
            String endPointForMarkdownFile = pathToMarkdownFile.replaceFirst("content/","/").replace(".md","");
            get(endPointForMarkdownFile, ((request, response) -> {
                try {
                    String responseBody = contentManager.getResourceMarkdownFileAsHtml(
                                        "content", request.pathInfo(),
                                                    getMarkdownParamsFromRequest(request));
                    response.body(responseBody);
                    response.type("text/html");
                    if(response.raw().containsHeader("x-robots-tag")){
                        // we want it indexed because it is content
                        response.raw().setHeader("x-robots-tag", "all");
                    }
                    response.status(200);
                }catch (IllegalArgumentException e){
                    // in theory this will never happen because we are only creating endpoints for existing resources
                    pageNotFoundHtmlResponse.amendResponse(response,"");
                }
                return "";
            }));
        }

        // using the ResourceContentScanner, we can build the sitemap.xml automatically
        SiteMapXml siteMap = new SiteMapXml();
        Map<String, LocalDate> contentUrls = contentScanner.scanForUrlsWithDates("content/", "md");
        for(String pathToMarkdownFile : contentUrls.keySet()){
            siteMap.addUrl("https://apichallenges.eviltester.com/"+pathToMarkdownFile,contentUrls.get(pathToMarkdownFile).toString());
        }
        siteMap.addUrl("https://apichallenges.eviltester.com", LocalDate.now().toString());
        siteMap.addUrl("https://apichallenges.eviltester.com/docs", LocalDate.now().toString());
        siteMap.addUrl("https://apichallenges.eviltester.com/gui/challenges", LocalDate.now().toString());

        get("/sitemap.xml",(request, response) -> {
            response.type("application/xml");
            response.status(200);
            return siteMap.asSitemapXml();
        });

        
        // use the site/index.md to allow easier creation of landing page, rather than public/index.hmlt
        get("/", (request, response) -> {
            String responseBody = contentManager.getHtmlVersionOfMarkdownContent("site", "/index", getMarkdownParamsFromRequest(request));
            response.body(responseBody);
            response.type("text/html");
            if(response.raw().containsHeader("x-robots-tag")){
                // we want it indexed because it is content
                response.raw().setHeader("x-robots-tag", "all");
            }
            response.status(200);
            return "";
        });

        // single user / default session
        get("/gui/challenges", (request, result) -> {

            if (request.cookie("X-THINGIFIER-DATABASE-NAME") != null) {
                // we didn't add a challenger in the URL but we do have one in the cookie
                result.header("location", "/gui/challenges/" + request.cookie("X-THINGIFIER-DATABASE-NAME"));
                result.status(302);
                return "";
            }

            result.type("text/html");
            result.status(200);

            StringBuilder html = new StringBuilder();
            html.append(getChallengesPageHtmlHeader());
            html.append(guiManagement.getMenuAsHTML());
            html.append("<h1>API Challenges Progress</h1>");
            html.append(guiManagement.getStartOfMainContentMarker());

            // todo explain challenges - single user mode


            //List<ChallengeData> reportOn = new ArrayList<>();

            if (single_player_mode) {
                html.append(playerChallengesIntro());
                //reportOn = new ChallengesPayload(challengeDefinitions, challengers.SINGLE_PLAYER).getAsChallenges();
                String json = "{}";
                if(challengers.getErModel().getDatabaseNames().contains(EntityRelModel.DEFAULT_DATABASE_NAME)){
                    json = challengers.getErModel().getInstanceData(EntityRelModel.DEFAULT_DATABASE_NAME).asJson();
                }
                html.append(outputChallengeDataAsJS(challengers.SINGLE_PLAYER, json));
                html.append(renderChallengeData(challengeDefinitions, challengers.SINGLE_PLAYER));
                html.append(injectCookieFunctions());
                html.append(storeThingifierDatabaseNameCookie(challengers.SINGLE_PLAYER.getXChallenger()));
            } else {
                html.append("<div style='clear:both'><p><strong>Unknown Challenger ID</strong></p></div>");
                html.append(outputChallengeDataAsJS(challengers.SINGLE_PLAYER, "{}"));
                html.append(
                        multiUserShortHelp(
                                persistenceLayer.willAutoSaveChallengerStatusToPersistenceLayer(),
                                persistenceLayer.willAutoLoadChallengerStatusFromPersistenceLayer()
                    )
                );
                html.append(injectCookieFunctions());
                html.append(showPreviousGuids());
                html.append(inputAChallengeGuidScript());

                //reportOn = new ChallengesPayload(challengeDefinitions, challengers.DEFAULT_PLAYER_DATA).getAsChallenges();
                html.append(renderChallengeData(challengeDefinitions, challengers.DEFAULT_PLAYER_DATA));
            }

            //html.append(renderChallengeData(reportOn));

            html.append(guiManagement.getEndOfMainContentMarker());
            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });

        // multi user
        get("/gui/challenges/*", (request, result) -> {

            result.type("text/html");
            result.status(200);

            StringBuilder html = new StringBuilder();
            html.append(getChallengesPageHtmlHeader());
            html.append(guiManagement.getMenuAsHTML());
            html.append("<h1>API Challenges Progress</h1>");
            html.append(guiManagement.getStartOfMainContentMarker());
            html.append(playerChallengesIntro());


            //List<ChallengeData> reportOn = null;

            String xChallenger = null;

            try {
                xChallenger = request.splat()[0];
            } catch (Exception e) {
                logger.warn("No challenger id to render");
            }


            // is there an in memory challenger with this id?
            ChallengerAuthData challenger = null;
            PersistenceResponse persistence = null;

            // only check if an xchallenger was passed in
            if (xChallenger != null && !xChallenger.trim().isEmpty()) {
                xChallenger = santitizeChallengerGuid(xChallenger);
                challenger = challengers.getChallenger(xChallenger);

                persistence = new PersistenceResponse();

                // if no inmemory challenger then ask the persistence layer
                if (challenger == null) {
                    persistence = persistenceLayer.tryToLoadChallenger(challengers, xChallenger);
                }
            }

            if (challenger == null) {
                String persistenceReason = "";
                if (persistence != null) {
                    persistenceReason = persistence.getErrorMessage();
                }
                html.append("<div class='standoutblock'>");
                html.append(String.format("<p><strong>Unknown Challenger ID %s</strong></p>",
                        persistenceReason));
                html.append(showCurrentStatus());
                html.append("</div>");
                html.append(
                    multiUserShortHelp(
                        persistenceLayer.willAutoSaveChallengerStatusToPersistenceLayer(),
                        persistenceLayer.willAutoLoadChallengerStatusFromPersistenceLayer()
                    )
                );
                html.append(injectCookieFunctions());
                html.append(showPreviousGuids());
                html.append(inputAChallengeGuidScript());
                html.append(outputChallengeDataAsJS(challengers.DEFAULT_PLAYER_DATA, "{}"));
                html.append(renderChallengeData(challengeDefinitions, challengers.DEFAULT_PLAYER_DATA));
            } else {
                html.append(injectCookieFunctions());

                String json = "{}";
                if(challengers.getErModel().getDatabaseNames().contains(xChallenger)){
                    json = challengers.getErModel().getInstanceData(xChallenger).asJson();
                }
                html.append(outputChallengeDataAsJS(challenger, json));

                if (!single_player_mode) {
                    html.append(storeThingifierDatabaseNameCookie(xChallenger));
                    html.append(storeCurrentGuidInLocalStorage(xChallenger));

                    html.append("<div class='standoutblock'>");
                    html.append(String.format("<p><strong>Progress For Challenger ID %s</strong></p>", xChallenger));
                    // keep challenge session alive when refresh
                    challenger.touch();
                    html.append(showCurrentStatus());
                    html.append(showPreviousGuids());
                    html.append(inputAChallengeGuidScript());
                    html.append("</div>");
                }else {
                    html.append(storeThingifierDatabaseNameCookie(xChallenger));
                    html.append(storeCurrentGuidInLocalStorage(xChallenger));
                    html.append("<div class='standoutblock'>");
                    html.append(showCurrentStatus());
                    html.append("</div>");
                }

                html.append(renderChallengeData(challengeDefinitions, challenger));

                html.append(refreshScriptFor(challenger.getXChallenger()));
            }

            //html.append(renderChallengeData(reportOn));

            html.append(guiManagement.getEndOfMainContentMarker());
            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });

        get("/gui/404", (request, result) -> {
            pageNotFoundHtmlResponse.amendResponse(result, "");
            return "";
        });

        get("/gui/404/*", (request, result) -> {
            result.status(404);
            result.type("text/html");

            String urltoshow = "";

            try {
                urltoshow = request.splat()[0];
            } catch (Exception e) {
               logger.error("No url to pretend to be on 404", e);
            }

            pageNotFoundHtmlResponse.amendResponse(result, "<script>window.history.pushState({id:\"404sim\"},\"\",\"/" + urltoshow + "\");</script>");
            return "";
        });

        after((request, response)->{


            // Since we already scanned for static content we can just htmlise a 404 if necessary
            if(response.status()==404 && request.headers("accept")!=null && request.headers("accept").contains("html")){

                logger.info("An HTML 404");
                pageNotFoundHtmlResponse.amendResponse(response, "");
            }
        });

    }

    private Map<String, String> getMarkdownParamsFromRequest(Request request){
        String originUrl = request.scheme() + "://" + request.host();
        Map<String, String> params = new HashMap<>();
        params.put("ORIGIN_URL", originUrl);
        params.put("HOST_URL", request.host());
        return params;
    }

    private String showCurrentStatus() {
        return "<script>showCurrentStatus()</script>";
    }

    private String santitizeChallengerGuid(String xChallenger) {
        return xChallenger.replaceAll("[^\\-a-zA-Z0-9]","");
    }

    private String injectCookieFunctions(){
        return "";
    }
    private String storeThingifierDatabaseNameCookie(String xChallenger) {
        return "<script>" +
                "setCookie('X-THINGIFIER-DATABASE-NAME','" + xChallenger +"',365);"+
                "</script>";
    }


    private String storeCurrentGuidInLocalStorage(final String xChallenger) {
        return "<script>" +
                "var guids = localStorage.getItem('challenges-guids') || '';" +
                String.format("if(guids==null || !guids.includes('|%s|')){", xChallenger) +
                String.format("localStorage.setItem('challenges-guids',guids + '|%s|');",xChallenger) +
                "}" +
                "</script>";
    }

    private String inputAChallengeGuidScript() {
        return "<p><button onclick=inputChallengeGuid()>Input a Challenger GUID to use</button></p>";
    }

    private String showPreviousGuids() {
        return "<script>displayLocalGuids()</script>";// +
    }

    private String getChallengesFooter() {
        return """
                <p>&nbsp;</p><hr/><div class='footer'><p>Copyright Compendium Developments Ltd 2020 </p>
                <ul class='footerlinks'><li><a href='https://eviltester.com/apichallenges'>API Challenges Info</a></li>
                <li><a href='https://eviltester.com'>EvilTester.com</a></li>
                </ul></div>
                """;
    }

    private String playerChallengesIntro() {
        final StringBuilder html = new StringBuilder();
        html.append("<div style='clear:both'>");
        html.append("<p>Use the Descriptions of the challenges below to explore the API and solve the challenges." +
                    " Remember to use the API documentation to see the format of POST requests.</p>" +
                    "<p>Progress, and the TODOs database content can be saved to, and restored from, LocalStorage in the browser - or managed via the API." +
                    "</p>");
        html.append("</div>");
        return html.toString();
    }

    private String multiUserShortHelp(Boolean canSaveToPersistence, boolean canRestoreFromPersistence) {
        final StringBuilder html = new StringBuilder();
        html.append("<div style='clear:both' class='headertextblock'>");
        html.append("<p>To view your challenges status in multi-user mode, make sure you have registered as a challenger using a `POST` request to `/challenger` and are including an `X-CHALLENGER` header in all your requests.</p>");
        html.append("<p>Then view the challenges in the GUI by visiting `/gui/challenges/{GUID}`, where `{GUID}` is the value in the `X-CHALLENGER` header.<p>");
        html.append("<p>Challenger sessions are purged from the server memory after 10 minutes of inactivity.</p>");
        if(canSaveToPersistence) {
            html.append("Challenger progress is configured to saved on the server.<p>");
        }else{
            html.append("Challenger progress is not configured to automatically save on the server. Use the GUI or UI to save progress locally.<p>");
        }
        if(canRestoreFromPersistence) {
            html.append("To restore a previously saved session progress from the server, issue an API request with the X-CHALLENGER header (note this will restore the completion state of challenges, but not the data you were using).<p>");
        }
        html.append("<p>Session state and current todo list can be stored to local storage, and later restored using the GUI buttons or via API.</p>");
        html.append("<p>You can find more information about this on the <a href='/gui/multiuser'>Multi User Help Page</a><p>");
        html.append("</div>");
        return html.toString();
    }

    private String refreshScriptFor(final String xChallenger) {

        if(!guiStayAlive)
            return "";

        StringBuilder html = new StringBuilder();

        html.append("<script>");
        html.append("/* keep session alive */");
        html.append("setInterval(function(){");
        html.append("var oReq = new XMLHttpRequest();\n" +
                "oReq.open('GET', '/challenger/" + xChallenger +"');\n" +
                "oReq.send();");
        html.append("},300000);");
        html.append("</script>");
        return html.toString();
    }


    private String renderChallengeData(final List<ChallengeDefinitionData> reportOn) {
        StringBuilder html = new StringBuilder();

        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>ID</th>");
        html.append("<th>Challenge</th>");
        html.append("<th>Done</th>");
        html.append("<th>Description</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for(ChallengeDefinitionData challenge : reportOn){
            html.append(String.format("<tr class='status%b'>", challenge.status));
            html.append(String.format("<td>%s</td>", challenge.id));
            html.append(String.format("<td>%s</td>", challenge.name));
            html.append(String.format("<td>%b</td>", challenge.status));

            String descriptionHTML = String.format("<p>%s</p>",challenge.description);
            if(challenge.hasHints() || challenge.hasSolutionLinks()){
                descriptionHTML = descriptionHTML + "<br/>";
            }
            if(challenge.hasHints()){
                descriptionHTML = descriptionHTML + "<details><summary>Hints</summary>";
                descriptionHTML = descriptionHTML + "<ul>";
                String hintHtml = "";
                for(ChallengeHint hint : challenge.hints){
                    hintHtml = hintHtml + "<li>" + hint.hintText;
                    if(hint.hintLink!=null && !hint.hintLink.isEmpty()){
                        String target="target='_blank'";
                        if(!hint.hintLink.startsWith("http")){
                            target="";
                        }
                        hintHtml = hintHtml +
                                String.format(" <a href='%s' %s>Learn More</a>",
                                        hint.hintLink, target);
                    }
                    hintHtml = hintHtml + "</li>";
                }
                descriptionHTML = descriptionHTML + hintHtml + "</ul>";
                descriptionHTML = descriptionHTML + "</details>";
            }
            if(challenge.hasSolutionLinks()){

                descriptionHTML = descriptionHTML + "<details><summary>Solution</summary>";
                descriptionHTML = descriptionHTML + "<ul>";
                String solutionsHtml = "";
                for(ChallengeSolutionLink solution : challenge.solutions){
                    solutionsHtml = solutionsHtml + "<li>" + solution.asHtmlAHref() + "</li>";
                }
                descriptionHTML = descriptionHTML + solutionsHtml + "</ul>";
                descriptionHTML = descriptionHTML + "</details>";
            }

            html.append(String.format("<td>%s</td>", descriptionHTML));
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        return html.toString();
    }

    private String outputChallengeDataAsJS(final ChallengerAuthData challenger, String json){

        StringBuilder html = new StringBuilder();

        // add the challenge data as JSON
        final String dataString = challenger.asJson();
        html.append("<script>document.challengerData=" + dataString + ";</script>");
        // add the current todos as JSON
        html.append("<script>document.databaseData=" + json + ";</script>");

        return html.toString();

    }

    private String renderChallengeData(final ChallengeDefinitions challengeDefinitions, final ChallengerAuthData challenger) {
        StringBuilder html = new StringBuilder();



        final Collection<ChallengeSection> sections = challengeDefinitions.getChallengeSections();

        // add a toc
        html.append("<h2 id='toc'>Challenge Sections</h2>");
        html.append("<ul>");
        for(ChallengeSection section : sections){
            html.append(String.format("<li><a href='#%s'>%s</a></li>", section.getTitle().replaceAll(" ", "").toLowerCase(), section.getTitle()));
        }
        html.append("</ul>");

        for(ChallengeSection section : sections){

            html.append(String.format("<h2 id='%s'>", section.getTitle().replaceAll(" ", "").toLowerCase()) + section.getTitle() + "</h2>");
            html.append("<p class='challengesectiondescription'>" + section.getDescription() + "</p>");

            List<ChallengeDefinitionData> sectionData = new ArrayList<>();
            for(ChallengeDefinitionData challenge : section.getChallenges()){
                final ChallengeDefinitionData data = new ChallengeDefinitionData(challenge.id, challenge.name, challenge.description);
                CHALLENGE challengeKey = challengeDefinitions.getChallenge(challenge.name);
                if(challenge!=null) {
                    data.status = challenger.statusOfChallenge(challengeKey);
                    data.addHints(challenge.hints);
                    data.addSolutions(challenge.solutions);
                }
                sectionData.add(data);
            }

            html.append(renderChallengeData(sectionData));
            html.append("<p><a href='#toc'>Back to Section List</a></p>");
        }

        return html.toString();
    }

}
