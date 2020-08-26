package uk.co.compendiumdev.challenge.gui;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.ChallengesPayload;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.ChallengeData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.challenges.ChallengeSection;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.challenge.persistence.PersistenceResponse;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static spark.Spark.get;

public class ChallengerWebGUI {
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
            result.type("text/html");
            result.status(200);

            StringBuilder html = new StringBuilder();
            html.append(guiManagement.getPageStart("Challenges"));
            html.append(guiManagement.getMenuAsHTML());

            // todo explain challenges - single user mode


            //List<ChallengeData> reportOn = new ArrayList<>();

            if(single_player_mode){
                html.append(playerChallengesIntro());
                //reportOn = new ChallengesPayload(challengeDefinitions, challengers.SINGLE_PLAYER).getAsChallenges();
                html.append(renderChallengeData(challengeDefinitions, challengers.SINGLE_PLAYER));
            }else{
                html.append("<div style='clear:both'><p><strong>Unknown Challenger ID</strong></p></div>");
                html.append(multiUserShortHelp());
                html.append(showPreviousGuids());

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
            html.append(guiManagement.getPageStart("Challenges"));
            html.append(guiManagement.getMenuAsHTML());

            html.append(playerChallengesIntro());


            //List<ChallengeData> reportOn = null;

            String xChallenger = request.splat()[0];
            ChallengerAuthData challenger = challengers.getChallenger(xChallenger);
            PersistenceResponse persistence = new PersistenceResponse();

            if(challenger==null){
                persistence = persistenceLayer.tryToLoadChallenger(challengers, xChallenger);
            }

            if(challenger==null){
                html.append(String.format("<p><strong>Unknown Challenger ID %s</strong></p>",
                                persistence.getErrorMessage()));
                html.append(multiUserShortHelp());
                html.append(showPreviousGuids());
                //reportOn = new ChallengesPayload(challengeDefinitions, challengers.DEFAULT_PLAYER_DATA).getAsChallenges();
                html.append(renderChallengeData(challengeDefinitions, challengers.DEFAULT_PLAYER_DATA));
            }else{
                html.append(storeCurrentGuidInLocalStorage(xChallenger));
                //reportOn = new ChallengesPayload(challengeDefinitions, challenger).getAsChallenges();
                html.append(renderChallengeData(challengeDefinitions, challenger));
                html.append(refreshScriptFor(challenger.getXChallenger()));
            }

            //html.append(renderChallengeData(reportOn));

            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });
    }



    private String storeCurrentGuidInLocalStorage(final String xChallenger) {
        return "<script>" +
                "var guids = localStorage.getItem('challenges-guids') || '';" +
                String.format("if(guids==null || !guids.includes('|%s|')){", xChallenger) +
                String.format("localStorage.setItem('challenges-guids',guids + '|%s|');",xChallenger) +
                "}" +
                "</script>";
    }

    private String showPreviousGuids() {
        // todo: show a delete button to delete from local storage - not delete from persistent storage
        return "<script>" +
                "var guids = localStorage.getItem('challenges-guids') || '';" +
                "if(guids.length>0){document.writeln('<p><strong>Previously Used</strong></p>')}" +
                "var guidsArray = guids.match(/\\|([^|]*)\\|/g);" +
                "for(guidItem in guidsArray){" +
                "var myguid = guidsArray[guidItem].replace(/\\|/g,'');" +
                "document.writeln(\"<p><a href='/gui/challenges/\"+myguid+\"'>\"+myguid+\"</a></p>\")" +
                "}" +
                "</script>";
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
        html.append("<p>Use the Descriptions of the challenges below to explore the API and solve the challenges. Remember to use the API documentation to see the format of POST requests.</p>");
        html.append("</div>");
        return html.toString();
    }

    private String multiUserShortHelp() {
        final StringBuilder html = new StringBuilder();
        html.append("<div style='clear:both' class='headertextblock'>");
        html.append("<p>To view your challenges status in multi-user mode, make sure you have registered as a challenger using a `POST` request to `/challenger` and are including an `X-CHALLENGER` header in all your requests.</p>");
        html.append("<p>Then view the challenges in the GUI by visiting `/gui/challenges/{GUID}`, where `{GUID}` is the value in the `X-CHALLENGER` header.<p>");
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

    // todo: save challenge status in local storage
    // todo: post challenge status from local storage to current X-CHALLENGER session
    // todo: clear local storage challenge status

    private String renderChallengeData(final List<ChallengeData> reportOn) {
        StringBuilder html = new StringBuilder();

        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");

        html.append("<style>.statustrue{background:palegreen}</style>");
        html.append("<th>Challenge</th>");
        html.append("<th>Done</th>");
        html.append("<th>Description</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for(ChallengeData challenge : reportOn){
            html.append(String.format("<tr class='status%b'>", challenge.status));
            html.append(String.format("<td>%s</td>", challenge.name));
            html.append(String.format("<td>%b</td>", challenge.status));
            html.append(String.format("<td>%s</td>", challenge.description));
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        return html.toString();
    }

    private String renderChallengeData(final ChallengeDefinitions challengeDefinitions, final ChallengerAuthData challenger) {
        StringBuilder html = new StringBuilder();

        final Collection<ChallengeSection> sections = challengeDefinitions.getChallengeSections();

        for(ChallengeSection section : sections){

            html.append("<h2>" + section.getTitle() + "</h2>");
            html.append("<p class='challengesectiondescription'>" + section.getDescription() + "</p>");

            List<ChallengeData> sectionData = new ArrayList<>();
            for(ChallengeData challenge : section.getChallenges()){
                final ChallengeData data = new ChallengeData(challenge.name, challenge.description);
                CHALLENGE challengeKey = challengeDefinitions.getChallenge(challenge.name);
                if(challenge!=null) {
                    data.status = challenger.statusOfChallenge(challengeKey);
                }
                sectionData.add(data);
            }

            html.append(renderChallengeData(sectionData));
        }

        // todo: collate challenges and find any missing challenges to add as an EXTRAs section

        return html.toString();
    }

}
