---
title: HTTP Basics - Tutorial
description: Basic HTTP tutorial and overview of key HTTP terminology and status codes.
---

# Introduction to HTTP Requests AND Responses

---

## Overview of Section - HTTP Requests and Responses

- HTTP Verbs - GET, POST, DELETE
- Headers
- Responses
    - Status Codes - e.g. 200, 404, 500
- This is the foundation for most web, HTTP, REST testing and automating.

---

## HTTP GET Request sent from cURL

Command:

~~~~~~~~
curl {{<ORIGIN_URL>}}/heartbeat ^
-H "accept: application/xml" ^
--proxy 127.0.0.1:8888
~~~~~~~~

Sends a GET Request:

~~~~~~~~
GET {{<ORIGIN_URL>}}/heartbeat HTTP/1.1
User-Agent: curl/7.39.0
Host: {{<HOST_URL>}}
Connection: Keep-Alive
accept: application/xml
~~~~~~~~

- GET requests retrieve information from the Web Application
- important stuff:
   - Verb (GET),
   - Http version (1.1),
   - User-Agent,
   - Accept,
   - Host,
   - endpoint

---

## HTTP Response to GET /heartbeat request

~~~~~~~~
HTTP/1.1 204 OK
Date: Thu, 17 Aug 2017 10:34:32 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Server: Jetty(9.4.4.v20170414)
~~~~~~~~

- cURL response was same but content-type was `application/xml`
- important stuff: Status Code (204 OK), Http version (1.1), Date, Content-Type

---

## Raw HTTP Requests and Responses

- we need to be able to read them
- we will rarely have to create them by hand
- lookup headers you don't know
    - https://en.wikipedia.org/wiki/List_of_HTTP_header_fields
- some fields are for the server some are for the application some are documentation

---

## Basic HTTP Verbs

- [GET](https://tools.ietf.org/html/rfc7231#section-4.3.1)  - retrieve data
- [POST](https://tools.ietf.org/html/rfc7231#section-4.3.3)  amend/create from partial information
- [PUT](https://tools.ietf.org/html/rfc7231#section-4.3.4) - create or replace from full information
- [DELETE](https://tools.ietf.org/html/rfc7231#section-4.3.5) - delete items
- [OPTIONS](https://tools.ietf.org/html/rfc7231#section-4.3.7) - verbs available on this url

---

## References


- [W3c Standard](https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html)
- [IETF standard](https://tools.ietf.org/html/rfc7231)
- [httpstatuses.com](https://httpstatuses.com)
- http://www.restapitutorial.com/lessons/httpmethods.html

---

## HTTP Status Codes

- 1xx Informational
    - 100 Continue
- 2xx Success
    - e.g. 200 OK
- 3xx Redirection
    - e.g. 301 Moved Permanently
- 4xx Client Error
    - e.g. 404 Not Found
- 5xx Server Error
    - e.g. 500 Internal Server Error


---

## Common HTTP Status Codes

| **Status Code**            | **Status Code** |
|----------------------------|-----------------|
|  200 OK | 405 Method Not Allowed    |
| 201 Created                            |   409 Conflict              |
| 301 Moved Permanently | 500 Internal Server Error |
| 307 Temporary Redirect | 501 Not Implemented |
| 400 Bad Request | 502 Bad Gateway |
| 401 Unauthorized| 503 Service Unavailable |
| 403 Forbidden | 504 Gateway Timeout |
| 404 Not Found | |

---

## HTTP Status code references

- https://httpstatuses.com/
- https://moz.com/blog/response-codes-explained-with-pictures
- https://http.cat/
- https://httpstatusdogs.com/

---

## HTTP Message Body Format - JSON

- JSON - JavaScript Object Notation
- an actual Object in JavaScript
- common data transfer and marshalling format for other languages
- https://en.wikipedia.org/wiki/JSON
- http://json.org
- http://countwordsfree.com/jsonviewer
- schema exists for JSON http://json-schema.org/

---

### JSON Example Explained

~~~~~~~~
{
 "lists":
  [
   {
    "guid":"f8134dd6-a573-4cf5-a6c6-9d556118ed0b",
    "title":"a list title",
    "description":"",
    "createdDate":"2017-08-17-13-11-12",
    "amendedDate":"2017-08-17-13-11-12"
   }
  ]
}
~~~~~~~~

- An object, which has an array called "lists".
- the lists array contains an object with fields: `guid`, `title`, `description`, `createdDate`, `amendedDate` - all String fields.

---

## HTTP Message Body Format - XML

- XML - eXtended Markup Language
- HTML is often XML
- another common marshalling format
- can be validated against XML schema
- http://countwordsfree.com/xmlviewer

---

## XML Example Explained

~~~~~~~~
<?xml version="1.0" encoding="UTF-8" ?>
<lists>
  <list>
    <guid>f8134dd6-a573-4cf5-a6c6-9d556118ed0b</guid>
    <title>a list title</title>
    <description></description>
    <createdDate>2017-08-17-13-11-12</createdDate>
    <amendedDate>2017-08-17-13-11-12</amendedDate>
  </list>
</lists>
~~~~~~~~

- elements, nested elements
- tags, values

---

## URI - Universal Resource Identifier

`scheme:[//[user[:password]@]host[:port]][/path][?query][#fragment]`

- `http://compendiumdev.co.uk/apps/api/mock/reflect`
    - scheme = `http`
    - host = `compendiumdev.co.uk`
    - path = `apps/api/mock/reflect`

[wikipedia.org/wiki/Uniform_Resource_Identifier](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier)

A URL is a URI

---

### URI vs URL vs URN

- URI - Universal Resource Identifier
    - 'generic' representation - might not include the 'scheme'
    - `http://compendiumdev.co.uk/apps/api/mock/reflect`
    - `compendiumdev.co.uk/apps/api/mock/reflect`
    - `/apps/api/mock/reflect`
- URL - Universal Resource Locator
    - `http://compendiumdev.co.uk/apps/api/mock/reflect`
    - defines how to locate the identified resource
- URN - [Universal Resource Name](https://en.wikipedia.org/wiki/Uniform_Resource_Name)
    - not often used - uses scheme `urn`

---

### Scheme(s)

- http
- https
- ftp
- mailto
- file

---

### Query Strings

~~~~~~~~
GET /lists/{guid}?without=title,description
GET {{<ORIGIN_URL>}}/lists/f13?without=title,description
~~~~~~~~

Query String:

~~~~~~~~
?without=title,description
~~~~~~~~

- starts with `?`
- params separated with `&`

---

### More About Query Strings

~~~~~~~~
GET /lists/{guid}?without=title,description
~~~~~~~~

- usually `name=value` pairs separate by '&'
    - convention since anything after the `?` is the Query string
    - app then parses as required
- can be used with any verb
- `GET` request - all params are send as query strings

https://en.wikipedia.org/wiki/Query_string

---

## HTTP Standards?

- rfc7231 [(HTTP/1.1): Semantics and Content](https://tools.ietf.org/html/rfc7231)
- rfc7230 [(HTTP/1.1): Message Syntax and Routing](https://tools.ietf.org/html/rfc7230)

---

## How to test with this information

- Read the standards for the verbs and the status codes.
- Projects often argue about interpretations.
- Some of the standards are exact enough that it is possible to say "I observed X" it does not match the standard - include links and quotes to the standards.


## HTTP Headers

- Headers are `Key: value` pair attributes in the request
- Headers are not counted as the payload
- Headers help configure the response from the server and tell the server how to process this request
- e.g. a `GET` request with an `accept: application/xml` header is asking the server to respond with the information in XML format


### User-Agent Header

- Often not sent when accessing an API
- Marks request as coming from a browser

~~~~~~~~
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
AppleWebKit/537.36 (KHTML, like Gecko)
Chrome/60.0.3112.90 Safari/537.36
~~~~~~~~

---

### Accept Header

- Defines the payload types that the receiver will accept
- If this was an API call it would likely return XML

~~~~~~~~
Accept: text/html,application/xhtml+xml,application/xml;
q=0.9,image/webp,image/apng,*/*;q=0.8
~~~~~~~~

Common values:

- `text/html`
- `application/json`
- `application/xml`

---

### Basic Auth Header

- An application might use Basic Auth Authentication to control access to the API
- `Authorization` Header

e.g. `Authorization: Basic dXNlcjpwYXNzd29yZA==`

`dXNlcjpwYXNzd29yZA==` is base64 encoded "user:password"

see [base64decode.org](https://www.base64decode.org)

- cURL you need to add the header
- Postman & Insomnia use the Authorization and Auth tabs