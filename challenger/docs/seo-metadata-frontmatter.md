# SEO Front Matter Guide

Use these front-matter fields for markdown pages under `resources/content/**`.

## Recommended minimum

```yaml
title: Human-friendly page title
seo_title: Search-focused page title
description: Meta description for search snippets
lastmod: 2026-02-18
```

## Optional SEO and social fields

```yaml
seo_description: Optional override for meta description text
meta_robots: index,follow   # e.g. noindex,nofollow for non-public pages
canonical: https://apichallenges.eviltester.com/path
og_image: /images/social/apichallenges-og-1200x630.png
og_image_alt: Image alt text for social previews
og_type: article            # use website for home/index style pages
twitter_card: summary_large_image
twitter_site: @your_handle
schema_type: Article          # defaults to Article, WebPage for index template
schema_author: Your Name
schema_publisher: API Challenges
schema_image: /images/social/apichallenges-og-1200x630.png
schema_breadcrumb_enabled: true  # set false to suppress BreadcrumbList JSON-LD
schema_howto_enabled: true       # enable/disable HowTo generation for this page
schema_howto_steps: Step one||Step two||Step three
schema_video_enabled: true       # set false to suppress VideoObject when a YouTube video is present
schema_video_id: OpisB0UZq0c     # force specific YouTube id when needed
```

## Notes

- If `seo_description` is not set, `description` is used.
- `lastmod` is used as the page update date for sitemap `<lastmod>` and schema `dateModified`.
- If `lastmod` is not set, schema `dateModified` falls back to `date`.
- If `og_image` is not set, the global default social image is used.
- If `meta_robots` is not set, `index,follow` is used.
- `seo_title` is used for the HTML `<title>` tag; `title` remains the human-facing page label.
- If `schema_type` is not set, content pages default to `Article` and index pages default to `WebPage`.
- Default schema author and publisher are read from resource files:
  - `src/main/resources/seo/schema-author.properties`
  - `src/main/resources/seo/schema-publisher.properties`
- `schema_author` and `schema_publisher` in front matter override the resource defaults on a per-page basis.
- Page-level schema overrides are available for content-specific schema behavior:
  - `schema_breadcrumb_enabled: false` disables `BreadcrumbList`
  - `schema_howto_enabled: true|false` forces `HowTo` on/off
  - `schema_howto_steps` provides explicit `HowToStep` names separated by `||` (required to emit HowTo)
  - `schema_video_enabled: true|false` forces `VideoObject` on/off
  - `schema_video_id` sets the YouTube id used in `VideoObject`
- Entity depth can be configured in resource files, e.g.:
  - author: `jobTitle`, `sameAs` (comma separated)
  - publisher: `legalName`, `contactType`, `email`, `telephone`, `sameAs` (comma separated)
- Content-specific schema is added automatically:
  - `BreadcrumbList` for content pages
  - `HowTo` only when `schema_howto_steps` is provided (recommended on solutions/tutorials)
  - `VideoObject` when a YouTube video is detected in page content
