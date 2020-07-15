# REST API Testing Checklist

## Verbs

- `GET`
    - is cacheable?
- `HEAD`
    - identical to `GET` without a body?    

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
- [Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content](https://tools.ietf.org/html/rfc7231)
- [IANA Media Types Registry](https://www.iana.org/assignments/media-types/media-types.xhtml)
- [restfulapi.net](https://restfulapi.net/)
- [httpstatuses.com](https://httpstatuses.com)