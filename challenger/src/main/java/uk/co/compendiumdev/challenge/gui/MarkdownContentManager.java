package uk.co.compendiumdev.challenge.gui;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

// TODO: consider adding caching for generated markdown pages

public class MarkdownContentManager {

    private final DefaultGUIHTML guiManagement;
    Logger logger = LoggerFactory.getLogger(MarkdownContentManager.class);
    private final Set<String> markdownContentPaths;
    private String sideMenuText;

    public MarkdownContentManager(final List<String> pathsToFileContent, final DefaultGUIHTML defaultGui) {
        markdownContentPaths = new HashSet<>();
        markdownContentPaths.addAll(pathsToFileContent);
        this.guiManagement = defaultGui;
        sideMenuText="";
    }


    // TODO: this is currently a hacked in solution for experimenting, pull it out into classes and create state enum
    public String getResourceMarkdownFileAsHtml(String contentFolder, String contentPath, Map<String,String> params) {

        if(contentPath.endsWith(".html")){
            contentPath = contentPath.replace(".html", "");
        }

        if(contentPath.endsWith(".md")){
            contentPath = contentPath.replace(".md", "");
        }

        String contentToFind = contentFolder + contentPath + ".md";

        // if content does not exist in the list then exit
        if(!markdownContentPaths.contains(contentToFind)){
            throw new IllegalArgumentException("Resource not found %s.md".formatted(contentPath));
        }

        return getHtmlVersionOfMarkdownContent(contentFolder, contentPath, params);

    }

    public String getHtmlVersionOfMarkdownContent(String contentFolder, String contentPath, Map<String,String> params) {

        InputStream inputStream = getResourceAsStream(contentFolder + contentPath + ".md");

        String[] breadcrumbs = Arrays.stream(
                        contentPath.split("/")).
                filter(item -> item != null && !item.isEmpty()
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
                        if(markdownContentPaths.contains(contentFolder + "/" + bcPath + ".md")) {
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


        String headerInject = "";
        String youtubeHeaderInject = "";


        List<Extension> extensions = List.of(TablesExtension.create());
        // parse this html and output
        Parser parser = Parser.builder().extensions(extensions).build();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line="";

        List<String> mdheaders = new ArrayList<>();

        StringBuilder mdcontent = new StringBuilder();

        mdcontent.append(bcHeader);

        String state = "EXPECTING_HEADER";
        boolean addedToc = false;

        try {
            while ((line = reader.readLine()) != null) {

                if (line.equals("---") && state.equals("EXPECTING_HEADER")) {
                    state = "READING_HEADER"; // start of headers
                    continue;
                }

                if (line.equals("---") && state.equals("READING_HEADER")) {
                    state = "READING_CONTENT"; // end of headers
                    continue;
                }

                if (line.contains(": ") && state.equals("READING_HEADER")) {
                    mdheaders.add(line);
                    continue;
                }

                if (state.equals("READING_HEADER") && line.trim().isEmpty()) {
                    // ignore empty lines in the header
                    continue;
                }

                if (state.equals("READING_HEADER") && !line.trim().isEmpty()) {
                    // probably shouldn't be reading headers we found a non-empty line
                    state = "READING_CONTENT";
                }

                // process any macros
                line = processMacrosInContentLine(line, params);

                if(line.contains("youtube.com/watch")){
                    if(youtubeHeaderInject.isEmpty()){
                        // only import the facade if we are rendering youtube
                        youtubeHeaderInject = "<script type=\"module\" src=\"https://cdn.jsdelivr.net/npm/@justinribeiro/lite-youtube@1.5.0/lite-youtube.js\"></script>";
                    }
                }

                mdcontent.append(line + "\n");

                // todo: better header parsing, and parse headers separate from the main body
                if(!mdheaders.contains("template: index")) {
                    // inject table of contents
                    if (line.startsWith("# ") && !addedToc) {
                        addedToc = true;
                        mdcontent.append("\n<div id='toc'></div>\n");
                    }
                }

            }
        }catch(Exception e){
            logger.error("Markdown parsing error", e);
        }


        headerInject = headerInject + youtubeHeaderInject;

        String markdownFromResource = mdcontent.toString();
        Node document = parser.parse(markdownFromResource);

        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();


        String pageTitle = "Content Page";
        String pageDescription = "";
        String canonicalUrl = "https://apichallenges.eviltester.com"+contentPath;

        for(String aHeader : mdheaders){
            if(aHeader.startsWith("title: ")){
                pageTitle = aHeader.replace("title: " , "");
            }
            if(aHeader.startsWith("description: ")){
                pageDescription = aHeader.replace("description: " , "");
            }
            if(aHeader.startsWith("canonical: ")){
                canonicalUrl = aHeader.replace("canonical: " , "");
            }
        }


        if(!pageDescription.isEmpty()){
            headerInject = headerInject + "<meta name='description' content ='" + pageDescription + "'>";
        }

        StringBuilder html = new StringBuilder();
        html.append(guiManagement.getPageStart(pageTitle,
                """
        <script src='/js/toc.js'></script>
        <script src='/js/externalize-links.js'></script>
        """+headerInject, canonicalUrl));

        html.append(guiManagement.getMenuAsHTML());
        // todo: create proper templates
        if(!mdheaders.contains("template: index")) {
            html.append("<section class='doc-columns'>");
            html.append("<div class='left-column'>");
            html.append(renderer.render(parser.parse(dropDownMenuAsMarkdown())));
            html.append("</div>");
            html.append("<div class='right-column'>");
        }
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append(renderer.render(document));
        html.append(guiManagement.getEndOfMainContentMarker());
        if(!mdheaders.contains("template: index")) {
            html.append("</div>");
            html.append("</section>");
        }
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());

        return html.toString();
    }

    // TODO: move this into a markdown file so it can be cached and amended easily
    private String dropDownMenuAsMarkdown(){
        if(sideMenuText.isEmpty()){
            sideMenuText = getResourceAsString("partials/content-index.md");
        }

        return sideMenuText;
    }

    // TODO: improve the macro parsing
    // TODO: add a variables macro so we can set variables like schemeHost (http://localhost:4567) and replace variables in the docs - should add a 'default.varname': parsing in the markdown, would allow showing the 'proper url' regardless of environment hosting
    private String processMacrosInContentLine(String line, Map<String, String> params) {

        if(!line.contains("{{<"))
            return line;


//        String youTubeHtmlBlock = """
//<div class="video-container">
//    <iframe class='youtube-video' title='Watch Video - $2' loading='lazy' src="https://www.youtube.com/embed/$1" allow="autoplay; encrypted-media" allowfullscreen></iframe>
//</div>
//<div><p class="center-text"><a href="https://www.youtube.com/watch?v=$1" target="_blank">Watch on YouTube - $2</a></p></div>
//        """;

        // use YoutubeFacade https://github.com/justinribeiro/lite-youtube
        String youTubeHtmlBlock = """
<lite-youtube videoid="$1">
  <a class="lite-youtube-fallback" href="https://www.youtube.com/watch?v=$1">Watch on YouTube: "$2"</a>
</lite-youtube>
        """;

        String youtubeMacroRegex = "\\{\\{<youtube-embed key=\"([a-zA-Z0-9_-]+)\" title=\"(.+)\">}}";
        line = line.replaceAll(youtubeMacroRegex, youTubeHtmlBlock);

        for(String paramReplace : params.keySet()){
            String macroRegex = "\\{\\{<%s>}}".formatted(paramReplace);
            line = line.replaceAll(macroRegex, params.get(paramReplace));
        }
        return line;
    }

    private String getResourceAsString(String fileName){
        return new BufferedReader(new InputStreamReader(getResourceAsStream(fileName)))
                .lines().collect(Collectors.joining("\n"));
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


}
