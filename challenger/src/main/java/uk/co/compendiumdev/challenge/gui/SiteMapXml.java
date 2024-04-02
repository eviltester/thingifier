package uk.co.compendiumdev.challenge.gui;

import java.util.ArrayList;
import java.util.List;

public class SiteMapXml {

    List<SiteMapUrl> urls = new ArrayList<>();
    String cache = "";

    public void addUrl(String aUrl, String lastModified){
        urls.add(new SiteMapUrl(aUrl, lastModified));
    }

    public boolean contains(String aUrl) {
        for(SiteMapUrl url : urls){
            if(aUrl.equals(url.aUrl())){
                return true;
            }
        }
        return false;
    }

    public String asSitemapXml() {

        if(cache.isEmpty()) {
            StringBuilder sitemapxml = new StringBuilder();
            sitemapxml.append("""
                    <?xml version="1.0" encoding="utf-8"?>
                    <urlset xmlns="https://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml">
                    """.stripIndent().stripLeading());
            for (SiteMapUrl aUrl : urls) {
                sitemapxml.append("""
                        <url>
                        <loc>%s</loc>
                        <lastmod>%s</lastmod>
                        <changefreq>weekly</changefreq>
                        <priority>0.5</priority>
                        </url>
                        """.formatted(aUrl.aUrl(), aUrl.lastModified()).stripIndent().stripLeading());
            }
            sitemapxml.append("""
                    </urlset>
                    """.stripIndent().stripLeading());
            cache = sitemapxml.toString();
        }

        return cache;
    }
}

record SiteMapUrl(String aUrl, String lastModified) {};