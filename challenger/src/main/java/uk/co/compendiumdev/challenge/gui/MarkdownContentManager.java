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
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: consider adding caching for generated markdown pages

public class MarkdownContentManager {

    private static final String DEFAULT_CANONICAL_HOST = "https://apichallenges.eviltester.com";
    private static final String DEFAULT_SITE_NAME = "API Challenges";
    private static final String DEFAULT_OG_IMAGE_PATH = "/images/social/apichallenges-og-1200x630.png";
    private static final String DEFAULT_OG_TYPE_CONTENT = "article";
    private static final String DEFAULT_OG_TYPE_WEBSITE = "website";
    private static final String DEFAULT_TWITTER_CARD = "summary_large_image";
    private static final String DEFAULT_META_ROBOTS = "index,follow";
    private static final String DEFAULT_SCHEMA_TYPE_CONTENT = "Article";
    private static final String DEFAULT_SCHEMA_TYPE_INDEX = "WebPage";
    private static final String DEFAULT_SCHEMA_AUTHOR_NAME = "alan-richardson";
    private static final String DEFAULT_SCHEMA_PUBLISHER_NAME = "eviltester.com";
    private static final String DEFAULT_SCHEMA_AUTHOR_RESOURCE = "seo/schema-author.properties";
    private static final String DEFAULT_SCHEMA_PUBLISHER_RESOURCE = "seo/schema-publisher.properties";
    private static final String DEFAULT_AUTHOR_BIO_PATH = "/author/alan-richardson";
    private static final String DEFAULT_AUTHOR_BIO_SNIPPET_RESOURCE = "partials/author-bio-snippet.html";
    private static final String DEFAULT_NEXT_CHALLENGE_CTA_RESOURCE = "partials/next-challenge-cta.html";

    private final DefaultGUIHTML guiManagement;
    Logger logger = LoggerFactory.getLogger(MarkdownContentManager.class);
    private final Set<String> markdownContentPaths;
    private final Properties schemaAuthorDefaults;
    private final Properties schemaPublisherDefaults;
    private String sideMenuText;

    public MarkdownContentManager(final List<String> pathsToFileContent, final DefaultGUIHTML defaultGui) {
        markdownContentPaths = new HashSet<>();
        markdownContentPaths.addAll(pathsToFileContent);
        this.guiManagement = defaultGui;
        this.schemaAuthorDefaults = loadPropertiesFromResource(DEFAULT_SCHEMA_AUTHOR_RESOURCE);
        this.schemaPublisherDefaults = loadPropertiesFromResource(DEFAULT_SCHEMA_PUBLISHER_RESOURCE);
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

        StringBuilder bcHtmlHeader = new StringBuilder();
        StringBuilder bcHeader = new StringBuilder();
        bcHeader.append("\n");
        String bcPath ="";
        int linksInBreadcrumb=0;
        if(breadcrumbs.length>0){
            // https://spec.commonmark.org/0.29/#html-blocks
            bcHtmlHeader.append("<div class=\"breadcrumb\">\n\n");
            bcHtmlHeader.append("<blockquote>");
            bcHeader.append("> ");

            for(String bc : breadcrumbs){
                bcPath = bcPath + bc;


                if(!bc.isEmpty()) {

                    if(contentPath.endsWith(bc)){
                        bcHeader.append( bc );
                        bcHtmlHeader.append(String.format(" %s",  bc));
                    }else {
                        // if there is an index file then show the breadcrumb
                        if(markdownContentPaths.contains(contentFolder + "/" + bcPath + ".md")) {
                            linksInBreadcrumb++;
                            bcHeader.append(String.format(" [%s](%s) > ", bc, "/" + bcPath));
                            bcHtmlHeader.append(String.format("<a href=\"%s\">%s</a> &gt;","/" + bcPath,  bc));
                        }
                    }

                }
                bcPath = bcPath + "/";
            }
            bcHeader.append("\n");
            bcHtmlHeader.append("</blockquote>");
            bcHtmlHeader.append("</div >\n\n");
        }

        if(linksInBreadcrumb==0){
            // do not output the breadcrumb
            bcHeader = new StringBuilder();
            bcHtmlHeader = new StringBuilder();
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

        //mdcontent.append(bcHeader);

        String state = "EXPECTING_HEADER";
        boolean addedToc = false;
        String firstYouTubeVideoId = "";

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

                if(firstYouTubeVideoId.isEmpty()){
                    firstYouTubeVideoId = extractYouTubeVideoId(line);
                }

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

        if(mdheaders.contains("showads: true")) {
            // this did render google ads
//            headerInject = headerInject +
//                    "<script async src=\"https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7132305589272099\"" +
//                    " crossorigin=\"anonymous\"></script>";
        }

        headerInject = headerInject + youtubeHeaderInject;

        String markdownFromResource = mdcontent.toString();
        Node document = parser.parse(markdownFromResource);

        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();


        String pageTitle = "Content Page";
        String seoTitle = "";
        String pageDescription = "";
        String seoDescription = "";
        String metaRobots = "";
        String ogImage = "";
        String ogImageAlt = "";
        String ogType = "";
        String twitterCard = "";
        String twitterSite = "";
        String schemaType = "";
        String schemaAuthor = "";
        String schemaPublisher = "";
        String schemaImage = "";
        String schemaBreadcrumbEnabledRaw = "";
        String schemaHowToEnabledRaw = "";
        String schemaHowToStepsRaw = "";
        String schemaVideoEnabledRaw = "";
        String schemaVideoId = "";
        String nextChallengePath = "";
        String pageDatePublished = "";
        String pageLastModified = "";
        String canonicalUrl = DEFAULT_CANONICAL_HOST + contentPath;

        for(String aHeader : mdheaders){
            if(aHeader.startsWith("title: ")){
                pageTitle = aHeader.replace("title: " , "");
            }
            if(aHeader.startsWith("seo_title: ")){
                seoTitle = aHeader.replace("seo_title: " , "");
            }
            if(aHeader.startsWith("description: ")){
                pageDescription = aHeader.replace("description: " , "");
            }
            if(aHeader.startsWith("seo_description: ")){
                seoDescription = aHeader.replace("seo_description: " , "");
            }
            if(aHeader.startsWith("meta_robots: ")){
                metaRobots = aHeader.replace("meta_robots: " , "");
            }
            if(aHeader.startsWith("canonical: ")){
                canonicalUrl = aHeader.replace("canonical: " , "");
            }
            if(aHeader.startsWith("og_image: ")){
                ogImage = aHeader.replace("og_image: " , "");
            }
            if(aHeader.startsWith("og_image_alt: ")){
                ogImageAlt = aHeader.replace("og_image_alt: " , "");
            }
            if(aHeader.startsWith("og_type: ")){
                ogType = aHeader.replace("og_type: " , "");
            }
            if(aHeader.startsWith("twitter_card: ")){
                twitterCard = aHeader.replace("twitter_card: " , "");
            }
            if(aHeader.startsWith("twitter_site: ")){
                twitterSite = aHeader.replace("twitter_site: " , "");
            }
            if(aHeader.startsWith("schema_type: ")){
                schemaType = aHeader.replace("schema_type: " , "");
            }
            if(aHeader.startsWith("schema_author: ")){
                schemaAuthor = aHeader.replace("schema_author: " , "");
            }
            if(aHeader.startsWith("schema_publisher: ")){
                schemaPublisher = aHeader.replace("schema_publisher: " , "");
            }
            if(aHeader.startsWith("schema_image: ")){
                schemaImage = aHeader.replace("schema_image: " , "");
            }
            if(aHeader.startsWith("schema_breadcrumb_enabled: ")){
                schemaBreadcrumbEnabledRaw = aHeader.replace("schema_breadcrumb_enabled: " , "");
            }
            if(aHeader.startsWith("schema_howto_enabled: ")){
                schemaHowToEnabledRaw = aHeader.replace("schema_howto_enabled: " , "");
            }
            if(aHeader.startsWith("schema_howto_steps: ")){
                schemaHowToStepsRaw = aHeader.replace("schema_howto_steps: " , "");
            }
            if(aHeader.startsWith("schema_video_enabled: ")){
                schemaVideoEnabledRaw = aHeader.replace("schema_video_enabled: " , "");
            }
            if(aHeader.startsWith("schema_video_id: ")){
                schemaVideoId = aHeader.replace("schema_video_id: " , "");
            }
            if(aHeader.startsWith("next_challenge: ")){
                nextChallengePath = aHeader.replace("next_challenge: " , "");
            }
            if(aHeader.startsWith("date:")){
                pageDatePublished = aHeader.replaceFirst("^date:\\s*", "");
            }
            if(aHeader.startsWith("lastmod:")){
                pageLastModified = aHeader.replaceFirst("^lastmod:\\s*", "");
            }
        }

        final String htmlTitle = seoTitle.isEmpty() ? pageTitle : seoTitle;
        final String htmlDescription = seoDescription.isEmpty() ? pageDescription : seoDescription;
        final String robotsValue = metaRobots.isEmpty() ? DEFAULT_META_ROBOTS : metaRobots;

        final String canonicalHost = getEnvironmentOrDefault("SEO_CANONICAL_HOST", DEFAULT_CANONICAL_HOST);
        final String canonicalAbsoluteUrl = absolutizeUrl(canonicalUrl, canonicalHost);
        final String defaultOgImagePath = getEnvironmentOrDefault("SEO_DEFAULT_OG_IMAGE", DEFAULT_OG_IMAGE_PATH);
        final String ogImageAbsoluteUrl = absolutizeUrl(ogImage.isEmpty() ? defaultOgImagePath : ogImage, canonicalHost);
        final String ogImageAltValue = ogImageAlt.isEmpty() ? htmlTitle : ogImageAlt;
        final boolean indexTemplate = mdheaders.contains("template: index");
        final String ogTypeValue = ogType.isEmpty() ? (indexTemplate ? DEFAULT_OG_TYPE_WEBSITE : DEFAULT_OG_TYPE_CONTENT) : ogType;
        final String twitterCardValue = twitterCard.isEmpty() ? DEFAULT_TWITTER_CARD : twitterCard;
        final String twitterSiteValue = twitterSite.isEmpty() ? getEnvironmentOrDefault("SEO_TWITTER_SITE", "") : twitterSite;

        if(!htmlDescription.isEmpty()){
            headerInject = headerInject + "<meta name='description' content='" + escapeHtmlAttribute(htmlDescription) + "'>";
        }
        headerInject = headerInject + "<meta name='robots' content='" + escapeHtmlAttribute(robotsValue) + "'>";
        headerInject = headerInject + "<meta property='og:title' content='" + escapeHtmlAttribute(htmlTitle) + "'>";
        headerInject = headerInject + "<meta property='og:description' content='" + escapeHtmlAttribute(htmlDescription) + "'>";
        headerInject = headerInject + "<meta property='og:type' content='" + escapeHtmlAttribute(ogTypeValue) + "'>";
        headerInject = headerInject + "<meta property='og:url' content='" + escapeHtmlAttribute(canonicalAbsoluteUrl) + "'>";
        headerInject = headerInject + "<meta property='og:site_name' content='" + escapeHtmlAttribute(DEFAULT_SITE_NAME) + "'>";
        headerInject = headerInject + "<meta property='og:image' content='" + escapeHtmlAttribute(ogImageAbsoluteUrl) + "'>";
        headerInject = headerInject + "<meta property='og:image:alt' content='" + escapeHtmlAttribute(ogImageAltValue) + "'>";
        headerInject = headerInject + "<meta name='twitter:card' content='" + escapeHtmlAttribute(twitterCardValue) + "'>";
        headerInject = headerInject + "<meta name='twitter:title' content='" + escapeHtmlAttribute(htmlTitle) + "'>";
        headerInject = headerInject + "<meta name='twitter:description' content='" + escapeHtmlAttribute(htmlDescription) + "'>";
        headerInject = headerInject + "<meta name='twitter:image' content='" + escapeHtmlAttribute(ogImageAbsoluteUrl) + "'>";
        if(!twitterSiteValue.isEmpty()){
            headerInject = headerInject + "<meta name='twitter:site' content='" + escapeHtmlAttribute(twitterSiteValue) + "'>";
        }

        final String schemaTypeValue = schemaType.isEmpty() ?
                (indexTemplate ? DEFAULT_SCHEMA_TYPE_INDEX : DEFAULT_SCHEMA_TYPE_CONTENT) : schemaType;
        final String schemaImageAbsoluteUrl = absolutizeUrl(schemaImage.isEmpty() ? ogImageAbsoluteUrl : schemaImage, canonicalHost);
        final String defaultSchemaAuthor = getSchemaAuthorDefaultName();
        final String schemaAuthorValue = schemaAuthor.isEmpty() ? defaultSchemaAuthor : schemaAuthor;
        final String defaultSchemaPublisher = getSchemaPublisherDefaultName();
        final String schemaPublisherValue = schemaPublisher.isEmpty() ? defaultSchemaPublisher : schemaPublisher;
        final String authorJobTitle = schemaAuthorDefaults.getProperty("jobTitle", "").trim();
        final String authorBioSnippet = buildAuthorBioSnippet(
                contentFolder,
                contentPath,
                schemaAuthorValue,
                authorJobTitle,
                DEFAULT_AUTHOR_BIO_PATH);
        final String nextChallengeCtaSnippet = buildNextChallengeCtaSnippet(contentFolder, contentPath, nextChallengePath);
        final Boolean schemaBreadcrumbEnabled = parseOptionalBoolean(schemaBreadcrumbEnabledRaw);
        final Boolean schemaHowToEnabled = parseOptionalBoolean(schemaHowToEnabledRaw);
        final List<String> schemaHowToSteps = parseHowToSteps(schemaHowToStepsRaw);
        final Boolean schemaVideoEnabled = parseOptionalBoolean(schemaVideoEnabledRaw);
        final String pageDateModified = resolveDateModified(pageLastModified, pageDatePublished);
        final String schemaJsonLd = buildSchemaJsonLd(
                canonicalHost,
                canonicalAbsoluteUrl,
                contentPath,
                breadcrumbs,
                htmlTitle,
                htmlDescription,
                schemaTypeValue,
                schemaImageAbsoluteUrl,
                schemaAuthorValue,
                schemaPublisherValue,
                pageDatePublished,
                pageDateModified,
                firstYouTubeVideoId,
                schemaBreadcrumbEnabled,
                schemaHowToEnabled,
                schemaHowToSteps,
                schemaVideoEnabled,
                schemaVideoId);
        if(!schemaJsonLd.isEmpty()){
            headerInject = headerInject + schemaJsonLd;
        }

        StringBuilder html = new StringBuilder();
        html.append(guiManagement.getPageStart(htmlTitle,
                """
        <script src='/js/toc.js'></script>
        <script src='/js/externalize-links.js'></script>
        """+headerInject, canonicalAbsoluteUrl));

        html.append(guiManagement.getMenuAsHTML());
        // todo: create proper templates
        if(!mdheaders.contains("template: index")) {
            html.append("<section class='doc-columns'>");
            html.append("<div class='left-column'>");
            html.append("<div class='side-toc'>");
            html.append(renderer.render(parser.parse(dropDownMenuAsMarkdown())));
            html.append("</div>");
            html.append("</div>");
            html.append("<div class='right-column'>");
        }
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append(bcHtmlHeader.toString());
        html.append("<div class=\"main-text-content\">\n");
        html.append(renderer.render(document));
        html.append("</div>\n");
        html.append(nextChallengeCtaSnippet);
        html.append(authorBioSnippet);
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
    /*
        Macros are added to the markdown with the following syntax {{<macro_name>}}
        e.g. {{<HOST_URL>}}
        Some of these macros are direct string replacement injection from the params map
        Others like youtube-embed have been hard coded here

     */
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


        if(line.contains("{{<PARTIAL_SNIPPET")){
            String partialMacroRegex = "\\{\\{<PARTIAL_SNIPPET filename=\"(.+)\">}}";
            Pattern r = Pattern.compile(partialMacroRegex);
            Matcher m = r.matcher(line);
            if(m.find()) {
                String filename = m.group(1);
                String partialContent = getResourceAsString(filename);
                line = line.replaceAll(partialMacroRegex, partialContent);
            }
        }

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

    private String getEnvironmentOrDefault(final String envName, final String defaultValue){
        final String envValue = System.getenv(envName);
        if(envValue==null || envValue.trim().isEmpty()){
            return defaultValue;
        }
        return envValue.trim();
    }

    private String absolutizeUrl(final String url, final String host){
        if(url==null || url.isEmpty()){
            return "";
        }
        final String trimmed = url.trim();
        if(trimmed.startsWith("http://") || trimmed.startsWith("https://")){
            return trimmed;
        }
        if(trimmed.startsWith("/")){
            return host + trimmed;
        }
        return host + "/" + trimmed;
    }

    private String escapeHtmlAttribute(final String value){
        if(value==null){
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String buildSchemaJsonLd(final String canonicalHost,
                                     final String canonicalAbsoluteUrl,
                                     final String contentPath,
                                     final String[] breadcrumbs,
                                     final String htmlTitle,
                                     final String htmlDescription,
                                     final String schemaTypeValue,
                                     final String schemaImageAbsoluteUrl,
                                     final String schemaAuthor,
                                     final String schemaPublisherValue,
                                     final String pageDatePublished,
                                     final String pageDateModified,
                                     final String firstYouTubeVideoId,
                                     final Boolean schemaBreadcrumbEnabled,
                                     final Boolean schemaHowToEnabled,
                                     final List<String> schemaHowToSteps,
                                     final Boolean schemaVideoEnabled,
                                     final String schemaVideoId){

        final String orgName = schemaPublisherValue.isEmpty() ?
                getEnvironmentOrDefault("SEO_SCHEMA_ORG_NAME", DEFAULT_SITE_NAME) : schemaPublisherValue;
        final String websiteName = getEnvironmentOrDefault("SEO_SCHEMA_WEBSITE_NAME", DEFAULT_SITE_NAME);
        final String logoUrl = absolutizeUrl(
                getEnvironmentOrDefault("SEO_SCHEMA_LOGO_URL", DEFAULT_OG_IMAGE_PATH), canonicalHost);

        final List<String> sameAsLinks = parseCommaSeparatedUrls(System.getenv("SEO_SCHEMA_SAME_AS"));
        final String searchActionTemplate = System.getenv("SEO_SCHEMA_SEARCH_URL_TEMPLATE");
        final String authorUrl = getSchemaAuthorDefaultUrl();
        final List<String> authorSameAs = parseCommaSeparatedUrls(schemaAuthorDefaults.getProperty("sameAs", ""));
        final String authorJobTitle = schemaAuthorDefaults.getProperty("jobTitle", "").trim();
        final String publisherLegalName = schemaPublisherDefaults.getProperty("legalName", "").trim();
        final List<String> publisherSameAs = parseCommaSeparatedUrls(schemaPublisherDefaults.getProperty("sameAs", ""));
        final String publisherContactType = schemaPublisherDefaults.getProperty("contactType", "").trim();
        final String publisherEmail = schemaPublisherDefaults.getProperty("email", "").trim();
        final String publisherPhone = schemaPublisherDefaults.getProperty("telephone", "").trim();
        final String orgId = canonicalHost + "#organization";
        final String websiteId = canonicalHost + "#website";
        final String personId = canonicalHost + "#author";
        final String pageId = canonicalAbsoluteUrl + "#webpage";

        final StringBuilder scripts = new StringBuilder();
        scripts.append(toJsonLdScript(buildOrganizationJson(
                orgId, orgName, canonicalHost, logoUrl,
                sameAsLinks, publisherLegalName, publisherSameAs, publisherContactType, publisherEmail, publisherPhone)));
        scripts.append(toJsonLdScript(buildPersonJson(
                personId, schemaAuthor, authorUrl, authorSameAs, authorJobTitle, orgId)));
        scripts.append(toJsonLdScript(buildWebsiteJson(
                websiteId, websiteName, canonicalHost, orgId, searchActionTemplate)));
        scripts.append(toJsonLdScript(buildPageJson(
                schemaTypeValue,
                pageId,
                htmlTitle,
                htmlDescription,
                canonicalAbsoluteUrl,
                schemaImageAbsoluteUrl,
                personId,
                orgId,
                pageDatePublished,
                pageDateModified
        )));

        final boolean includeBreadcrumb = schemaBreadcrumbEnabled == null || schemaBreadcrumbEnabled;
        if(includeBreadcrumb){
            final String breadcrumbJson = buildBreadcrumbListJson(canonicalAbsoluteUrl, canonicalHost, breadcrumbs);
            scripts.append(toJsonLdScript(breadcrumbJson));
        }

        final String howToJson = buildHowToJson(
                contentPath,
                canonicalAbsoluteUrl,
                htmlTitle,
                htmlDescription,
                schemaImageAbsoluteUrl,
                schemaHowToEnabled,
                schemaHowToSteps);
        scripts.append(toJsonLdScript(howToJson));

        final String videoJson = buildVideoObjectJson(
                canonicalAbsoluteUrl,
                htmlTitle,
                htmlDescription,
                schemaImageAbsoluteUrl,
                firstYouTubeVideoId,
                orgId,
                schemaVideoEnabled,
                schemaVideoId);
        scripts.append(toJsonLdScript(videoJson));

        return scripts.toString();
    }

    private String toJsonLdScript(final String json){
        if(json==null || json.isEmpty()){
            return "";
        }
        // Prevent accidental script close within inline JSON.
        final String safeJson = json.replace("</", "<\\/");
        return "<script type='application/ld+json'>" + safeJson + "</script>";
    }

    private List<String> parseCommaSeparatedUrls(final String csv){
        if(csv==null || csv.trim().isEmpty()){
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String buildOrganizationJson(final String organizationId,
                                         final String organizationName,
                                         final String canonicalHost,
                                         final String logoUrl,
                                         final List<String> sameAsLinks,
                                         final String legalName,
                                         final List<String> publisherSameAs,
                                         final String contactType,
                                         final String email,
                                         final String telephone){
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"Organization\",");
        json.append("\"@id\":\"").append(escapeJsonValue(organizationId)).append("\",");
        json.append("\"name\":\"").append(escapeJsonValue(organizationName)).append("\",");
        json.append("\"url\":\"").append(escapeJsonValue(canonicalHost)).append("\"");
        if(!legalName.isEmpty()){
            json.append(",\"legalName\":\"").append(escapeJsonValue(legalName)).append("\"");
        }

        if(!logoUrl.isEmpty()){
            json.append(",\"logo\":{\"@type\":\"ImageObject\",\"url\":\"")
                    .append(escapeJsonValue(logoUrl))
                    .append("\"}");
        }

        final List<String> mergedSameAs = new ArrayList<>();
        mergedSameAs.addAll(sameAsLinks);
        for(String aLink : publisherSameAs){
            if(!mergedSameAs.contains(aLink)){
                mergedSameAs.add(aLink);
            }
        }

        if(!mergedSameAs.isEmpty()){
            json.append(",\"sameAs\":[");
            for(int i=0; i<mergedSameAs.size(); i++){
                if(i>0){
                    json.append(",");
                }
                json.append("\"").append(escapeJsonValue(mergedSameAs.get(i))).append("\"");
            }
            json.append("]");
        }

        if(!contactType.isEmpty() || !email.isEmpty() || !telephone.isEmpty()){
            json.append(",\"contactPoint\":{\"@type\":\"ContactPoint\"");
            if(!contactType.isEmpty()){
                json.append(",\"contactType\":\"").append(escapeJsonValue(contactType)).append("\"");
            }
            if(!email.isEmpty()){
                json.append(",\"email\":\"").append(escapeJsonValue(email)).append("\"");
            }
            if(!telephone.isEmpty()){
                json.append(",\"telephone\":\"").append(escapeJsonValue(telephone)).append("\"");
            }
            json.append("}");
        }

        json.append("}");
        return json.toString();
    }

    private String buildPersonJson(final String personId,
                                   final String authorName,
                                   final String authorUrl,
                                   final List<String> sameAsLinks,
                                   final String jobTitle,
                                   final String worksForOrgId){
        if(authorName==null || authorName.trim().isEmpty()){
            return "";
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"Person\",");
        json.append("\"@id\":\"").append(escapeJsonValue(personId)).append("\",");
        json.append("\"name\":\"").append(escapeJsonValue(authorName)).append("\"");
        if(authorUrl!=null && !authorUrl.isEmpty()){
            json.append(",\"url\":\"").append(escapeJsonValue(authorUrl)).append("\"");
        }
        if(jobTitle!=null && !jobTitle.isEmpty()){
            json.append(",\"jobTitle\":\"").append(escapeJsonValue(jobTitle)).append("\"");
        }
        if(!sameAsLinks.isEmpty()){
            json.append(",\"sameAs\":[");
            for(int i=0; i<sameAsLinks.size(); i++){
                if(i>0){
                    json.append(",");
                }
                json.append("\"").append(escapeJsonValue(sameAsLinks.get(i))).append("\"");
            }
            json.append("]");
        }
        if(worksForOrgId!=null && !worksForOrgId.isEmpty()){
            json.append(",\"worksFor\":{\"@id\":\"").append(escapeJsonValue(worksForOrgId)).append("\"}");
        }
        json.append("}");
        return json.toString();
    }

    private String buildWebsiteJson(final String websiteId,
                                    final String websiteName,
                                    final String canonicalHost,
                                    final String organizationId,
                                    final String searchActionTemplate){
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"WebSite\",");
        json.append("\"@id\":\"").append(escapeJsonValue(websiteId)).append("\",");
        json.append("\"name\":\"").append(escapeJsonValue(websiteName)).append("\",");
        json.append("\"url\":\"").append(escapeJsonValue(canonicalHost)).append("\"");
        json.append(",\"publisher\":{\"@id\":\"").append(escapeJsonValue(organizationId)).append("\"}");

        if(searchActionTemplate!=null && !searchActionTemplate.trim().isEmpty()){
            json.append(",\"potentialAction\":{");
            json.append("\"@type\":\"SearchAction\",");
            json.append("\"target\":\"").append(escapeJsonValue(searchActionTemplate.trim())).append("\",");
            json.append("\"query-input\":\"required name=search_term_string\"");
            json.append("}");
        }

        json.append("}");
        return json.toString();
    }

    private String buildPageJson(final String schemaTypeValue,
                                 final String pageId,
                                 final String htmlTitle,
                                 final String htmlDescription,
                                 final String canonicalAbsoluteUrl,
                                 final String schemaImageAbsoluteUrl,
                                 final String personId,
                                 final String organizationId,
                                 final String pageDatePublished,
                                 final String pageDateModified){
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"").append(escapeJsonValue(schemaTypeValue)).append("\",");
        json.append("\"@id\":\"").append(escapeJsonValue(pageId)).append("\",");
        json.append("\"name\":\"").append(escapeJsonValue(htmlTitle)).append("\",");
        if("Article".equalsIgnoreCase(schemaTypeValue)){
            json.append("\"headline\":\"").append(escapeJsonValue(htmlTitle)).append("\",");
        }
        if(!htmlDescription.isEmpty()){
            json.append("\"description\":\"").append(escapeJsonValue(htmlDescription)).append("\",");
        }
        json.append("\"url\":\"").append(escapeJsonValue(canonicalAbsoluteUrl)).append("\",");
        json.append("\"mainEntityOfPage\":\"").append(escapeJsonValue(canonicalAbsoluteUrl)).append("\"");

        if(!schemaImageAbsoluteUrl.isEmpty()){
            json.append(",\"image\":\"").append(escapeJsonValue(schemaImageAbsoluteUrl)).append("\"");
        }
        if(personId!=null && !personId.isEmpty()){
            json.append(",\"author\":{\"@id\":\"").append(escapeJsonValue(personId)).append("\"}");
        }
        if(organizationId!=null && !organizationId.isEmpty()){
            json.append(",\"publisher\":{\"@id\":\"").append(escapeJsonValue(organizationId)).append("\"}");
        }
        if(!pageDatePublished.isEmpty()){
            json.append(",\"datePublished\":\"")
                    .append(escapeJsonValue(pageDatePublished))
                    .append("\"");
        }
        if(!pageDateModified.isEmpty()){
            json.append(",\"dateModified\":\"")
                    .append(escapeJsonValue(pageDateModified))
                    .append("\"");
        }

        json.append("}");
        return json.toString();
    }

    private String buildBreadcrumbListJson(final String canonicalAbsoluteUrl,
                                           final String canonicalHost,
                                           final String[] breadcrumbs){
        if(breadcrumbs==null || breadcrumbs.length==0){
            return "";
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"BreadcrumbList\",");
        json.append("\"itemListElement\":[");

        int position = 1;
        json.append("{\"@type\":\"ListItem\",\"position\":")
                .append(position++)
                .append(",\"name\":\"Home\",\"item\":\"")
                .append(escapeJsonValue(canonicalHost))
                .append("\"}");

        String path = "";
        for(String crumb : breadcrumbs){
            if(crumb==null || crumb.isEmpty()){
                continue;
            }
            path = path + "/" + crumb;
            json.append(",{\"@type\":\"ListItem\",\"position\":")
                    .append(position++)
                    .append(",\"name\":\"")
                    .append(escapeJsonValue(humanizeSlug(crumb)))
                    .append("\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + path))
                    .append("\"}");
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private String buildHowToJson(final String contentPath,
                                  final String canonicalAbsoluteUrl,
                                  final String htmlTitle,
                                  final String htmlDescription,
                                  final String schemaImageAbsoluteUrl,
                                  final Boolean schemaHowToEnabled,
                                  final List<String> schemaHowToSteps){
        final boolean isHowToSection = contentPath.startsWith("/apichallenges/solutions/") || contentPath.startsWith("/tutorials/");
        final boolean includeHowTo = schemaHowToEnabled == null ? isHowToSection : schemaHowToEnabled;
        if(!includeHowTo){
            return "";
        }

        final List<String> steps = new ArrayList<>();
        if(schemaHowToSteps!=null && !schemaHowToSteps.isEmpty()){
            steps.addAll(schemaHowToSteps);
        }
        if(steps.size()<2){
            return "";
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"HowTo\",");
        json.append("\"name\":\"").append(escapeJsonValue(htmlTitle)).append("\",");
        json.append("\"description\":\"").append(escapeJsonValue(htmlDescription)).append("\",");
        json.append("\"url\":\"").append(escapeJsonValue(canonicalAbsoluteUrl)).append("\"");
        if(!schemaImageAbsoluteUrl.isEmpty()){
            json.append(",\"image\":\"").append(escapeJsonValue(schemaImageAbsoluteUrl)).append("\"");
        }
        json.append(",\"step\":[");
        for(int i=0; i<steps.size(); i++){
            if(i>0){
                json.append(",");
            }
            json.append("{\"@type\":\"HowToStep\",\"name\":\"")
                    .append(escapeJsonValue(steps.get(i)))
                    .append("\"}");
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private String buildVideoObjectJson(final String canonicalAbsoluteUrl,
                                        final String htmlTitle,
                                        final String htmlDescription,
                                        final String schemaImageAbsoluteUrl,
                                        final String firstYouTubeVideoId,
                                        final String organizationId,
                                        final Boolean schemaVideoEnabled,
                                        final String schemaVideoId){
        final String videoIdToUse = (schemaVideoId!=null && !schemaVideoId.trim().isEmpty()) ?
                schemaVideoId.trim() : firstYouTubeVideoId;
        final boolean includeVideoObject = schemaVideoEnabled == null ? videoIdToUse!=null && !videoIdToUse.isEmpty() : schemaVideoEnabled;
        if(!includeVideoObject || videoIdToUse==null || videoIdToUse.isEmpty()){
            return "";
        }
        final String videoWatchUrl = "https://www.youtube.com/watch?v=" + videoIdToUse;
        final String embedUrl = "https://www.youtube.com/embed/" + videoIdToUse;

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"VideoObject\",");
        json.append("\"name\":\"").append(escapeJsonValue(htmlTitle)).append("\",");
        json.append("\"description\":\"").append(escapeJsonValue(htmlDescription)).append("\",");
        json.append("\"url\":\"").append(escapeJsonValue(canonicalAbsoluteUrl)).append("\",");
        json.append("\"contentUrl\":\"").append(escapeJsonValue(videoWatchUrl)).append("\",");
        json.append("\"embedUrl\":\"").append(escapeJsonValue(embedUrl)).append("\"");
        if(!schemaImageAbsoluteUrl.isEmpty()){
            json.append(",\"thumbnailUrl\":\"").append(escapeJsonValue(schemaImageAbsoluteUrl)).append("\"");
        }
        if(organizationId!=null && !organizationId.isEmpty()){
            json.append(",\"publisher\":{\"@id\":\"").append(escapeJsonValue(organizationId)).append("\"}");
        }
        json.append("}");
        return json.toString();
    }

    private String humanizeSlug(final String slug){
        return slug.replace("-", " ").trim();
    }

    private String extractYouTubeVideoId(final String line){
        if(line==null || line.isEmpty()){
            return "";
        }
        Pattern macroPattern = Pattern.compile("youtube-embed key=\"([a-zA-Z0-9_-]+)\"");
        Matcher macroMatcher = macroPattern.matcher(line);
        if(macroMatcher.find()){
            return macroMatcher.group(1);
        }

        Pattern watchPattern = Pattern.compile("youtube\\.com/watch\\?v=([a-zA-Z0-9_-]+)");
        Matcher watchMatcher = watchPattern.matcher(line);
        if(watchMatcher.find()){
            return watchMatcher.group(1);
        }
        return "";
    }

    private String escapeJsonValue(final String value){
        if(value==null){
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private Properties loadPropertiesFromResource(final String resourcePath){
        final Properties properties = new Properties();
        try(InputStream inputStream = getResourceAsStream(resourcePath)){
            if(inputStream != null){
                properties.load(inputStream);
            }
        }catch (IOException e){
            logger.warn("Could not load schema properties from {}", resourcePath, e);
        }
        return properties;
    }

    private String getSchemaAuthorDefaultName(){
        final String authorFromResource = schemaAuthorDefaults.getProperty("name", "").trim();
        if(!authorFromResource.isEmpty()){
            return authorFromResource;
        }
        return DEFAULT_SCHEMA_AUTHOR_NAME;
    }

    private String getSchemaAuthorDefaultUrl(){
        return schemaAuthorDefaults.getProperty("url", "").trim();
    }

    private String getSchemaPublisherDefaultName(){
        final String publisherFromResource = schemaPublisherDefaults.getProperty("name", "").trim();
        if(!publisherFromResource.isEmpty()){
            return publisherFromResource;
        }
        return DEFAULT_SCHEMA_PUBLISHER_NAME;
    }

    private String getSchemaPublisherDefaultUrl(){
        return schemaPublisherDefaults.getProperty("url", "").trim();
    }

    static String resolveDateModified(final String pageLastModified, final String pageDatePublished){
        if(pageLastModified!=null && !pageLastModified.trim().isEmpty()){
            return pageLastModified.trim();
        }
        if(pageDatePublished!=null && !pageDatePublished.trim().isEmpty()){
            return pageDatePublished.trim();
        }
        return "";
    }

    private Boolean parseOptionalBoolean(final String rawValue){
        if(rawValue==null || rawValue.trim().isEmpty()){
            return null;
        }
        final String value = rawValue.trim().toLowerCase();
        if(value.equals("true") || value.equals("yes") || value.equals("on")){
            return true;
        }
        if(value.equals("false") || value.equals("no") || value.equals("off")){
            return false;
        }
        return null;
    }

    private List<String> parseHowToSteps(final String rawValue){
        if(rawValue==null || rawValue.trim().isEmpty()){
            return Collections.emptyList();
        }
        return Arrays.stream(rawValue.split("\\|\\|"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String buildAuthorBioSnippet(final String contentFolder,
                                         final String contentPath,
                                         final String authorName,
                                         final String authorJobTitle,
                                         final String authorBioPath){
        final boolean isContentPage = "content".equalsIgnoreCase(contentFolder);
        final boolean isAuthorPage = contentPath != null && contentPath.equalsIgnoreCase(authorBioPath);
        if(!isContentPage || isAuthorPage){
            return "";
        }

        final String safeAuthorName = escapeHtmlAttribute(authorName);
        final String safeAuthorJobTitle = escapeHtmlAttribute(authorJobTitle);
        final String safeAuthorJobTitleWithPrefix = safeAuthorJobTitle.isEmpty() ? "" : ", " + safeAuthorJobTitle;
        final String safeAuthorBioPath = escapeHtmlAttribute(authorBioPath);

        try{
            return getResourceAsString(DEFAULT_AUTHOR_BIO_SNIPPET_RESOURCE)
                    .replace("{{AUTHOR_NAME}}", safeAuthorName)
                    .replace("{{AUTHOR_JOB_TITLE_WITH_PREFIX}}", safeAuthorJobTitleWithPrefix)
                    .replace("{{AUTHOR_BIO_PATH}}", safeAuthorBioPath);
        }catch(Exception e){
            logger.warn("Could not load author snippet resource {}, using fallback html", DEFAULT_AUTHOR_BIO_SNIPPET_RESOURCE, e);
            return buildInlineAuthorBioSnippetFallback(
                    safeAuthorName,
                    safeAuthorJobTitle,
                    safeAuthorBioPath);
        }
    }

    private String buildInlineAuthorBioSnippetFallback(final String safeAuthorName,
                                                       final String safeAuthorJobTitle,
                                                       final String safeAuthorBioPath){
        StringBuilder snippet = new StringBuilder();
        snippet.append("<aside class='author-bio-snippet' aria-label='About the author'>");
        snippet.append("<p><strong>Written by ")
                .append(safeAuthorName)
                .append("</strong>");
        if(!safeAuthorJobTitle.isEmpty()){
            snippet.append(", ").append(safeAuthorJobTitle);
        }
        snippet.append(".</p>");
        snippet.append("<p><a href='")
                .append(safeAuthorBioPath)
                .append("'>Read the full author bio and credentials</a>")
                .append(".</p>");
        snippet.append("</aside>");
        return snippet.toString();
    }

    private String buildNextChallengeCtaSnippet(final String contentFolder,
                                                final String contentPath,
                                                final String nextChallengePath){
        if(!"content".equalsIgnoreCase(contentFolder) || contentPath == null){
            return "";
        }
        if(!contentPath.startsWith("/apichallenges/solutions/")){
            return "";
        }
        if(nextChallengePath == null || nextChallengePath.trim().isEmpty()){
            return "";
        }

        final String trimmedPath = nextChallengePath.trim();
        final String ctaUrl = trimmedPath.startsWith("/") ? trimmedPath : "/" + trimmedPath;
        final String ctaLabel = "Try the next challenge walkthrough";

        try{
            return getResourceAsString(DEFAULT_NEXT_CHALLENGE_CTA_RESOURCE)
                    .replace("{{NEXT_URL}}", escapeHtmlAttribute(ctaUrl))
                    .replace("{{NEXT_LABEL}}", escapeHtmlAttribute(ctaLabel));
        }catch(Exception e){
            logger.warn("Could not load next challenge cta resource {}, using fallback html", DEFAULT_NEXT_CHALLENGE_CTA_RESOURCE, e);
            return "<aside class='next-challenge-cta'><a class='next-challenge-cta-link' href='" +
                    escapeHtmlAttribute(ctaUrl) + "'>" + escapeHtmlAttribute(ctaLabel) + "</a></aside>";
        }
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
