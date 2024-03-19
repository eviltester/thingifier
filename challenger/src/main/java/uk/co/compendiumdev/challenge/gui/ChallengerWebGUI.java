package uk.co.compendiumdev.challenge.gui;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.*;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.challenge.persistence.PersistenceResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static spark.Spark.after;
import static spark.Spark.get;

public class ChallengerWebGUI {

    Logger logger = LoggerFactory.getLogger(ChallengerWebGUI.class);
    private final DefaultGUIHTML guiManagement;
    private final boolean guiStayAlive;

    public ChallengerWebGUI(final DefaultGUIHTML defaultGui, final boolean guiStayAlive) {
        this.guiManagement = defaultGui;
        this.guiStayAlive = guiStayAlive;
    }

    public void setup(final Challengers challengers, 
                      final ChallengeDefinitions challengeDefinitions, 
                      final PersistenceLayer persistenceLayer, 
                      final boolean single_player_mode) {

        guiManagement.appendMenuItem("Challenges", "/gui/challenges");
        guiManagement.removeMenuItem("Home");
        guiManagement.prefixMenuItem("Home", "/");
        guiManagement.appendMenuItem("API documentation","/docs");
        guiManagement.appendMenuItem("Learning", "/learning");

        guiManagement.setHomePageContent("    <h2 id=\"challenges\">Challenges</h2>\n" +
                "    <p>The challenges can be completed by issuing API requests to the API.</p>\n" +
                "    <p>e.g. <code>GET http://localhost:4567/todos</code> would complete the challenge to &quot;GET the list of todos&quot;</p>\n" +
                "    <p>You can also <code>GET http://localhost:4567/challenges</code> to get the list of challenges and their status as an API call. </p>\n"
        );

        guiManagement.setFooter(getChallengesFooter());

        // use the index.html to allow easier creation of docs and landing page
//        get("/", (request, result) -> {
//            result.redirect("/gui");
//            return "";
//        });

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
            html.append(guiManagement.getPageStart("Challenges", "<script src='/js/challengerui.js'></script>"));
            html.append(guiManagement.getMenuAsHTML());

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

            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });

        // multi user
        get("/gui/challenges/*", (request, result) -> {

            result.type("text/html");
            result.status(200);

            StringBuilder html = new StringBuilder();
            html.append(guiManagement.getPageStart("Challenges", "<script src='/js/challengerui.js'></script>"));
            html.append(guiManagement.getMenuAsHTML());

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

            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });

        get("/gui/404", (request, result) -> {
            pageNotFoundHtmlResponse(result, "");
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

            pageNotFoundHtmlResponse(result,"<script>window.history.pushState({id:\"404sim\"},\"\",\"/" + urltoshow + "\");</script>");
            return "";
        });

        after((request, response)->{

            // TODO: this is currently a hacked in solution for experimenting, pull it out into classes and create state enum

            if(response.status()==404 && request.headers("accept")!=null && request.headers("accept").contains("html")){
                logger.info("Double check that this is a 404");
                // all html content that is parsed will be in content folder in resources so we don't need to add that in the url
                String contentFolder = "content";
                ClassLoader classLoader = getClass().getClassLoader();

                String contentPath = request.pathInfo();
                if(contentPath.endsWith(".html")){
                    contentPath = contentPath.replace(".html", "");
                }

                InputStream inputStream = classLoader.getResourceAsStream(contentFolder + contentPath + ".md");
                if(inputStream==null) {
                    pageNotFoundHtmlResponse(response, "");
                }else{


                    String[] breadcrumbs = Arrays.stream(
                                                contentPath.split("/")).
                                                filter(item -> item != null && !"".equals(item)
                                                ).toArray(String[]::new);

                    StringBuilder bcHeader = new StringBuilder();
                    bcHeader.append("\n");
                    String bcPath ="";
                    int linksInBreadcrumb=0;
                    if(breadcrumbs.length>0){
                        bcHeader.append("> ");

                        for(String bc : breadcrumbs){
                            bcPath = bcPath + bc;
                            if(!bc.isEmpty()) {
                                if(contentPath.endsWith(bc)){
                                    bcHeader.append( bc );
                                }else {
                                    // if there is an index file then show the breadcrumb
                                    if(getResourceAsStream(contentFolder + "/" + bcPath + ".md")!=null) {
                                        linksInBreadcrumb++;
                                        bcHeader.append(String.format(" [%s](%s) > ", bc, "/" + bcPath));
                                    }
                                }
                            }
                            bcPath = bcPath + "/";
                        }
                        bcHeader.append("\n");
                    }

                    if(linksInBreadcrumb==0){
                        // do not output the breadcrumb
                        bcHeader = new StringBuilder();
                    }





                    List<Extension> extensions = Arrays.asList(TablesExtension.create());
                    // parse this html and output
                    Parser parser = Parser.builder().extensions(extensions).build();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line="";

                    List<String> mdheaders = new ArrayList<>();

                    StringBuilder mdcontent = new StringBuilder();

                    mdcontent.append(bcHeader);

                    String state = "EXPECTING_HEADER";

                    while ((line = reader.readLine()) != null)   {

                        if(line.equals("---") && state.equals("EXPECTING_HEADER")){
                            state="READING_HEADER"; // start of headers
                            continue;
                        }

                        if(line.equals("---") && state.equals("READING_HEADER")){
                            state="READING_CONTENT"; // end of headers
                            continue;
                        }

                        if(line.contains(": ") && state.equals("READING_HEADER")){
                            mdheaders.add(line);
                            continue;
                        }

                        if(state.equals("READING_HEADER") && line.trim().isEmpty()){
                            // ignore empty lines in the header
                            continue;
                        }

                        if(state.equals("READING_HEADER") && !line.trim().isEmpty()){
                            // probably shouldn't be reading headers we found a non-empty line
                            state="READING_CONTENT";
                        }

                        // process any macros
                        line = processMacrosInContentLine(line);

                        mdcontent.append(line + "\n");

                        // Print the content on the console
                        //System.out.println (line);
                    }

                    String markdownFromResource = mdcontent.toString();
                    Node document = parser.parse(markdownFromResource);

                    HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

                    String pageTitle = "Page " + URLEncoder.encode(request.pathInfo(),
                            java.nio.charset.StandardCharsets.UTF_8.toString());

                    for(String aHeader : mdheaders){
                        if(aHeader.startsWith("title: ")){
                            pageTitle = aHeader.replace("title: " , "");
                        }
                    }

                    StringBuilder html = new StringBuilder();
                    html.append(guiManagement.getPageStart(pageTitle,""));
                    html.append(guiManagement.getMenuAsHTML());
                    html.append("<style> .doc-columns{ display: grid; grid-template-columns: 20% 70%; grid-auto-flow: column; } </style>");
                    html.append("<style> .left-column{ font-size: smaller; } </style>");
                    html.append("<section class='doc-columns'>");
                    html.append("<div class='left-column'>");
                    html.append(renderer.render(parser.parse(dropDownMenuAsMarkdown())));
                    html.append("</div>");
                    html.append("<div class='right-column'>");
                    html.append(renderer.render(document));
                    html.append("</div>");
                    html.append("</section>");
                    html.append(guiManagement.getPageFooter());
                    html.append(guiManagement.getPageEnd());

                    response.body(html.toString());
                    response.type("text/html");
                    response.status(200);
                }

            }
        });

    }

    private String dropDownMenuAsSummary(){
        return
                """
                  <ul>
                    <li><a href="/apichallenges/solutions">Challenge Solutions</a></li>
                     <li><a href="/learning">How to Learn APIs</a></li>
                     <li>Reference:
                        <ul>
                            <li><a href="/tutorials/web-basics">Web Applications</a></li>
                            <li><a href="/tutorials/http-basics">HTTP Basics</a></li>
                            <li><a href="/tutorials/http-verbs">HTTP Verbs</a></li>
                        </ul>
                     </li>
                     <li><a href="/practice-modes/mirror">Mirror Mode</a></li>
                     <li><a href="/practice-modes/simulation">Simulation Mode</a></li>
                     <li><a href="/sponsors">Our Sponsors</a></li>
                  </ul>
                """;
    }

    private String dropDownMenuAsMarkdown(){
        return
                """
- [Challenge Solutions](/apichallenges/solutions)
- [How to learn APIs](/learning)
- Reference:
   - [Web Applications](/tutorials/web-basics)
   - [HTTP Basics](/tutorials/http-basics)
   - [HTTP Verbs](/tutorials/http-verbs)
   - [REST API Basics](/tutorials/rest-api-basics)
   - [Testing APIs](/tutorials/testing-apis)
   - [Summary](/tutorials/summary)
- Practice Modes
   - [Mirror Mode](/practice-modes/mirror)
   - [Simulation Mode](/practice-modes/simulation)
- Tools
   - [Proxies](/tools/proxies)
- Practice Sites
   - [Swapi](/example-sites/swapi)
- [Sponsors](/sponsors)
                """;
    }

    private String processMacrosInContentLine(String line) {

        if(!line.contains("{{<"))
            return line;


        String youTubeHtmlBlock = """
<div class="video-container">
    <iframe width="560" height="315" src="https://www.youtube.com/embed/$1" allow="autoplay; encrypted-media" allowfullscreen></iframe>
</div>
<div><p class="center-text"><a href="https://www.youtube.com/watch?v=$1" target="_blank">Watch on YouTube</a></p></div>

        """;

        line = line.replaceAll("\\{\\{<youtube-embed key=\"([a-zA-Z0-9_-]+)\">}}", youTubeHtmlBlock);

        return line;
    }

    private InputStream getResourceAsStream(String fileName) {

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);


        // the stream holding the file content
        if (inputStream == null) {
            logger.error("content file not found: " + fileName);
            return null;
        } else {
            return inputStream;
        }
    }

    private void pageNotFoundHtmlResponse(Response response, String bodyStringAppend) {
        response.status(404);
        response.type("text/html");
        StringBuilder html = new StringBuilder();
        html.append(guiManagement.getPageStart("404 Not Found",""));
        html.append(guiManagement.getMenuAsHTML());
        html.append("<h1>Page Not Found</h1>");
        html.append(bodyStringAppend);
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());
        response.type("text/html");
        response.body(html.toString());
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
        return "<p>&nbsp;</p><hr/><div class='footer'><p>Copyright Compendium Developments Ltd 2020 </p>\n" +
                "<ul class='footerlinks'><li><a href='https://eviltester.com/apichallenges'>API Challenges Info</a></li>\n" +
                "<li><a href='https://eviltester.com'>EvilTester.com</a></li>\n" +
                "</ul></div>";
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
        html.append("<p>You can find more information about this on the <a href='multiuser.html'>Multi User Help Page</a><p>");
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

        html.append("<style>.statustrue{background:palegreen}</style>");
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
                    if(hint.hintLink!=null && hint.hintLink.length()>0){
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
        html.append("<p id='toc'><strong>Sections</strong></p>");
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
