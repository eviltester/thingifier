# SEO Front Matter Guide

Use these front-matter fields for markdown pages under `resources/content/**`.

## Recommended minimum

```yaml
title: Human-friendly page title
seo_title: Search-focused page title
description: Meta description for search snippets
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
```

## Notes

- If `seo_description` is not set, `description` is used.
- If `og_image` is not set, the global default social image is used.
- If `meta_robots` is not set, `index,follow` is used.
- `seo_title` is used for the HTML `<title>` tag; `title` remains the human-facing page label.
