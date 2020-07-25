# REST API Testing Checklist

## URLs

- `/nouns` - URLs are normally 'plural' nouns, if 'noun' does not exist then [404](https://httpstatuses.com/404) will result
- `/nouns/` - a trailing slash means `nouns` with (`/`) 'missing value', so will usually result in a [404](https://httpstatuses.com/404)
- `/nouns/{guid}` - `{guid}` is some sort of unique id or GUID, varies per system, if item is not found then status [404](https://httpstatuses.com/404) returned


Query Arguments:

- `?id=1` query arguments are often supported for filtering e.g. `/nouns?status=true` would return all `noun` with a status of true
- query arguments are separated by `&` e.g. `?id=1&status=true`
- since this only allows exact matching, some systems have custom query argument approaches
   - e.g. `?id=[>=]1[and][<=]4`, `?id=1-4` this will be system specific
- also `&` is not 'mandatory' it is 'customary', anything after `?` is the query string so could be parsed however the API defines

## Status Codes

All responses contain a [status code](https://httpstatuses.com/).

- We expect the status code to match the type of response e.g. if there was an error processing a request then we would expect a 4xx or 5xx response, we would not expect a 2xx response.

General Status Code Ranges:

- 1xx - Information response (100, 101, 102)
- 2xx - Success response (200 - 226)
- 3xx - Redirection response (300 - 308)
- 4xx - Client Error in Request (400 - 499)
- 5xx - Server Error (500 - 599)

## Verbs

Verbs use resources e.g. URLs. Some general status code rules apply:

- Non-Existant - [404](https://httpstatuses.com/404)
    - if a request is made for a resource that does not exist then a [404](https://httpstatuses.com/404) status code is returned
- Not-Authenticated - [401](https://httpstatuses.com/401) 
    - if a request is made for a resource that requires Authentication, and the request is not authenticated then [401](https://httpstatuses.com/401) status code is returned
- Not-Authorised - [403](https://httpstatuses.com/403) 
    - if a request is made for a resource that requires an Authenticated request with a level of authorisation, and the request is not authorised then [403](https://httpstatuses.com/403) status code is returned
- Method Not Allowed - [405](https://httpstatuses.com/405)
    - if a resource exists, but the method (e.g. `GET`, `PUT`) does not apply, then a [405](https://httpstatuses.com/405) status code is returned
    - the `OPTIONS` request can usually be issued on a resource to find out what methods apply


- `GET`
    - is cacheable
    - when resource is found
        - then a [200](https://httpstatuses.com/200) status code is returned
        - the body of the response contains the details fetched about the resource
        - the format of the response (e.g JSON, XML) is based on the MIME type in the Accept header of the request
        - if no Accept header was sent in the request then the default for the system will be used
    - when resource is not found [404](https://httpstatuses.com/404)
- `HEAD`
    - identical to `GET` without a body
- `PUT`
    - is idempotent (same result each time)
    - for an Update that means 'replace', and all fields presented
    - for a Create that means 'insert', and all fields presented (it may not be possible to create items with PUT if GUIDs or IDs are used in the entity)
- `POST`
    - is not idempotent (could have a different result, e.g. partial payloads, concurrent updates)
    - for an update that means 'update', and partial payloads can be presented, with unmentioned fields not updated
    - for a create that means 'insert', and partial payloads can be presented, with unmentioned fields using defaults or auto-generated values        

### PUT vs POST

Not all systems support PUT and POST in the same way.

So the 'rules' for PUT and POST tend to be contextual in applications.

If it is to be REST compliant then the main difference is Idemopotent (PUT) vs Not Necessarily Idempotent (POST). But sometimes systems do not comply with this.

Remember the REST descriptions are 'guidelines'. APIs can still 'work' when they don't follow the guidelines exactly , the API documentation just has to be clear on how the API operates.



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
    - `415` status code if content-type not supported ([415](https://httpstatuses.com/415)) e.g. XML supplied, but system only accepts JSON
    - If the `Content-Type` header is not present then systems might not issue a `415` they may instead, try to derive the content-type from the payload, and potentially throw a `400` error if they can't parse it.

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
    - [PATCH Method for HTTP](https://tools.ietf.org/html/rfc5789)
- [IANA Media Types Registry](https://www.iana.org/assignments/media-types/media-types.xhtml)
- [restfulapi.net](https://restfulapi.net/)
- [httpstatuses.com](https://httpstatuses.com)
- [Mozilla HTTP MDN Web Docs](https://developer.mozilla.org/en-US/docs/Web/HTTP)
- [Open API Spec Group](https://www.openapis.org/)
- [Open API Spec](https://github.com/OAI/OpenAPI-Specification)

Security:

- [OWASP REST API Security Cheatsheet](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html)