# REST API Testing Checklist

## URLs

- `/nouns` - URLs are normally 'plural' nouns, if 'noun' does not exist then `404` will result
- `/nouns/` - a trailing slash means `nouns` with (`/`) 'missing value', so will usually result in a `404`
- `/nouns/{guid}` - `{guid}` is some sort of unique id or GUID, varies per system, if item is not found then status `404` returned

Query Arguments:

- `?id=1` query arguments are often supported for filtering e.g. `/nouns?status=true` would return all `noun` with a status of true
- query arguments are separated by `&` e.g. `?id=1&status=true`
- since this only allows exact matching, some systems have custom query argument approaches
   - e.g. `?id=[>=]1[and][<=]4`, `?id=1-4` this will be system specific
- also `&` is not 'mandatory' it is 'customary', anything after `?` is the query string so could be parsed however the API defines


## Verbs

- `GET`
    - is cacheable
- `HEAD`
    - identical to `GET` without a body?
- `PUT`
    - is idempotent (same result each time)
    - for an Update that means 'replace', and all fields presented
    - for a Create that means 'insert', and all fields presented (it may not be possible to create items with PUT if GUIDs or IDs are used in the entity)
- `POST`
    - is not idempotent (could have a different result, e.g. partial payloads, concurrent updates)
    - for an update that means 'update', and partial payloads can be presented, with unmentioned fields not updated
    - for a create that means 'insert', and partial payloads can be presented, with unmentioned fields using defaults or auto-generated values        

## Content Negotiation Headers

- `Accept`
   - responses represented using the Media specified
       - `application/json`
       - `application/xml`
       - `*/*` - default provided
   - Media types processed in preference order in accept header
       - `application/xml, application/json, */*`
       - are `;q=0.9` priorities supported?
   - when no accept header in request then default is provided in response
   - `406` status code if cannot supply an accepted type ([406](https://httpstatuses.com/406))

- `Content-Type`
    - `415` status code if content-type not supported  ([415](https://httpstatuses.com/415))

## References

- Roy Thomas Fielding [REST Thesis](https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm)
- (https://tools.ietf.org/html/rfc7230)
- Hypertext Transfer Protocol (HTTP/1.1) - [original](https://tools.ietf.org/html/rfc2616)
    - [(HTTP/1.1): Message Syntax and Routing](https://tools.ietf.org/html/rfc7230)
    - [(HTTP/1.1): Semantics and Content](https://tools.ietf.org/html/rfc7231)
    - [(HTTP/1.1): Conditional Requests](https://tools.ietf.org/html/rfc7232)
    - [(HTTP/1.1): Range Requests](https://tools.ietf.org/html/rfc7233)
    - [(HTTP/1.1): Caching](https://tools.ietf.org/html/rfc7234)
    - [(HTTP/1.1): Authentication](https://tools.ietf.org/html/rfc7235)
- [IANA Media Types Registry](https://www.iana.org/assignments/media-types/media-types.xhtml)
- [restfulapi.net](https://restfulapi.net/)
- [httpstatuses.com](https://httpstatuses.com)