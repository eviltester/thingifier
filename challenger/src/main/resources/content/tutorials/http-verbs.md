---
title: HTTP Verbs - Tutorial
description: Basic HTTP Verbs and Methods tutorial what they do and how to use them.
---

# HTTP Verbs Overview

---

## HTTP GET Verb

- [GET](https://tools.ietf.org/html/rfc7231#section-4.3.1)  - retrieve data
- GET verbs can be issued by a browser
    - click on link
    - visit a site
- GET `http://compendiumdev.co.uk/apps/api/mock/reflect`
- Important Headers
    - User-Agent - tells server app type
    - Accept - what format response you prefer


---

### HTTP GET Verb Example

~~~~~~~~
curl {{<ORIGIN_URL>}}/heartbeat ^
-H "accept: application/xml" ^
--proxy 127.0.0.1:8888
~~~~~~~~

~~~~~~~~
GET {{<ORIGIN_URL>}}/heartbeat HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Connection: Keep-Alive
accept: application/xml
~~~~~~~~

---

### Common HTTP Status codes in response to a GET

- **200** - OK, found the url, returned contents
- **301, 307, 308** - content has moved, new url in `location` header
- **404** - url not found
- **401** - you need to give me authorisation details see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it


---

## HTTP POST Verb

- [POST](https://tools.ietf.org/html/rfc7231#section-4.3.3)  amend/create from partial information

- send a 'body' format of content in the 'content-type' header
- usually used to create or amend data
- browser will usually send a POST request when submitting a form

---

### HTTP POST Verb Send Example

~~~~~~~~
curl -X POST {{<ORIGIN_URL>}}/lists ^
-H "accept: application/xml" ^
-H content-type:application/json ^
-H "Authorization: Basic dXNlcjpwYXNzd29yZA==" ^
-d "{title:'a list title'}" ^
--proxy 127.0.0.1:8888
~~~~~~~~

---

### HTTP POST Verb Request Example

~~~~~~~~
POST {{<ORIGIN_URL>}}/lists HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Connection: Keep-Alive
accept: application/json
content-type: application/json
Authorization: Basic dXNlcjpwYXNzd29yZA==
Content-Length: 22

{title:'a list title'}

~~~~~~~~

---

### HTTP POST Verb Response Example

~~~~~~~~
HTTP/1.1 201 Created
Date: Thu, 17 Aug 2017 12:11:12 GMT
Content-Type: application/json
Location: /lists/f8134dd6-a573-4cf5-a6c6-9d556118ed0b
Server: Jetty(9.4.4.v20170414)
Content-Length: 171

{"lists":[{
"guid":"f8134dd6-a573-4cf5-a6c6-9d556118ed0b",
"title":"a list title",
"description":"",
"createdDate":"2017-08-17-13-11-12",
"amendedDate":"2017-08-17-13-11-12"}]}
~~~~~~~~

---

### Common HTTP Status codes in response to a POST

- **200** - OK, did whatever I was supposed to
- **201** - OK created new items
- **202** - OK, I'll do that later
- **204** - OK, I have no more information to give you
- **400** - what? that request made no sense
- **404** - I can't post to that url it is not found
- **401** - need authorisation see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it
- **409** - can't do that, already exists
- **500** - your request made me crash


---

## HTTP PUT Verb

- [PUT](https://tools.ietf.org/html/rfc7231#section-4.3.4) - create or replace from full information

Full information means it should be idempotent - send it again and get exactly the same request

Demo

---

### HTTP PUT Send Example

~~~~~~~~
curl -X PUT {{<ORIGIN_URL>}}/lists ^
-H "Authorization: Basic dXNlcjpwYXNzd29yZA==" ^
--proxy 127.0.0.1:8888 ^
-d @createlistwithput.txt
~~~~~~~~

where `createlistwithput.txt` file contains

~~~~~~~~
{"title":"title added with put",
"description":"list description",
"guid": "guidcreatedwithput201708171440",
"createdDate": "2017-08-17-14-40-34",
"amendedDate": "2017-08-17-14-40-34"}
~~~~~~~~

---

### HTTP PUT Request Example

~~~~~~~~
PUT {{<ORIGIN_URL>}}/lists HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Accept: */*
Connection: Keep-Alive
Authorization: Basic dXNlcjpwYXNzd29yZA==
Content-Length: 180
Content-Type: application/json

{"title":"title added with put",
"description":"list description",
"guid": "guidcreatedwithput201708171440",
"createdDate": "2017-08-17-14-40-34",
"amendedDate": "2017-08-17-14-40-34"}
~~~~~~~~

---

### HTTP PUT Response Example

~~~~~~~~
HTTP/1.1 201 Created
Date: Thu, 17 Aug 2017 13:41:46 GMT
Content-Type: application/json
Server: Jetty(9.4.4.v20170414)
Content-Length: 0
~~~~~~~~

---

## HTTP DELETE Verb

- [DELETE](https://tools.ietf.org/html/rfc7231#section-4.3.5) - delete items

Demo

---

### HTTP DELETE Send Example

~~~~~~~~
curl -X DELETE {{<ORIGIN_URL>}}/lists/{guid} ^
-H "Authorization: Basic YWRtaW46cGFzc3dvcmQ=" ^
--proxy 127.0.0.1:8888
~~~~~~~~

---

### HTTP DELETE Request Example

~~~~~~~~
DELETE {{<ORIGIN_URL>}}/lists/{guid} HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Accept: */*
Connection: Keep-Alive
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
~~~~~~~~

---

### HTTP DELETE Response Example

~~~~~~~~
HTTP/1.1 204 No Content
Date: Thu, 17 Aug 2017 12:20:35 GMT
Content-Type: application/json
Server: Jetty(9.4.4.v20170414)
~~~~~~~~


---

### Common HTTP Status codes in response to a DELETE

- **200** - OK, did whatever I was supposed to
- **202** - OK, I'll do that later
- **204** - OK, I have no more information to give you
- **404** - I can't post to that url it is not found
- **401** - you need to give me authorisation details see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it
- **500** - your request made me crash


---

## HTTP OPTIONS Verb

- [OPTIONS](https://tools.ietf.org/html/rfc7231#section-4.3.7) - shows the verbs available on this url
- returns an `Allow` header describing the allowed HTTP Verbs

---

### HTTP OPTIONS Send Example

~~~~~~~~
curl -X OPTIONS {{<ORIGIN_URL>}}/lists ^
--proxy 127.0.0.1:8888
~~~~~~~~

Demo

---

### HTTP OPTIONS Request Example

~~~~~~~~
OPTIONS {{<ORIGIN_URL>}}/lists HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Accept: */*
Connection: Keep-Alive
~~~~~~~~

---

### HTTP OPTIONS Response Example

~~~~~~~~
HTTP/1.1 200 OK
Date: Thu, 17 Aug 2017 12:24:39 GMT
Allow: GET, POST, PUT
Content-Type: text/html;charset=utf-8
Server: Jetty(9.4.4.v20170414)
Content-Length: 0
~~~~~~~~

---

### Common HTTP Status codes in response to a OPTIONS

- **200** - OK, did whatever I was supposed to
- **404** - I can't post to that url it is not found




