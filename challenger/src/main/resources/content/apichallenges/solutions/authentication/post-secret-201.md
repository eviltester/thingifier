---
date:  2021-07-24T08:30:00Z
title: API Challenges Solution For - authentication passed 201
description: How to solve API challenge 30 - authenticate with username and password for basic auth.
---

# How to complete the basic auth authentication challenge

One way of authenticating a user is through Basic Auth which requires a username and password in the Auth header.

## 	Authentication Challenge

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password.

## Challenge 30 Authentication Passed

> Issue a POST request on the `/secret/token` end point and receive 201 when Basic auth username/password is admin/password

- `POST` request means use the HTTP Verb POST
    - e.g. `POST /secret/token` sends to the secret token endpoint
- `Basic auth` means include the [Basic Authorization header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)
- `username/password is admin/password` the authorisation header value is base 64 encoded, and the details should  match `admin` as the username, and `password` for the password
- add the `X-CHALLENGER` header to track progress and because the authentication code we need is asociated with the `X-challenger` session
- Receive a 201 response because the session token has been created to allow authorization to access the secret notes


## Basic Instructions

- Create a new request for the `/secret/token` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/secret/token`
- The verb should be a `POST`
- Add a Basic Auth header by selecting "Basic" from the "Auth" tab and entering a username and password of admin/password i.e. use username "admin", password "password"
- There should be no payload in the message
- You should receive a 201 response - meaning the token has been created
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response should have an `X-AUTH-TOKEN` header which you will include in the messages for challenges 33, 34, 37 and 38

## Example Request

~~~~~~~~
> POST /secret/token HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Authorization: Basic YWRtaW46cGFzc3dvcmQ=
> Accept: */*
> Content-Length: 0
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 201 Created
< Connection: close
< Date: Sat, 24 Jul 2021 12:06:09 GMT
< X-Auth-Token: d432f0a3-a81b-4fc8-8e89-24848cc27f34
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Content-Type: text/html;charset=utf-8
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Basic Auth uses Base64 Encoding

The `Authorization` header does not send the username and password in plain text, it uses Base64 to obscure the details.

You could see that "admin:password" converts to the Base64 string `YWRtaW46cGFzc3dvcmQ=` by using a Base64 decoder/encoder like https://www.base64decode.org/

Or you could decode it in the browser dev console by typing:

```javascript
atob('YWRtaW46cGFzc3dvcmQ=')
```

The command to encode a string as base64 is `btoa`

## Extras

- try creating a base64 Authorization header by hand, without using the "Auth" tab in Insomnia


## Overview Video

{{<youtube-embed key="J2GQiuEfHkI" title="Solution to Basic Auth update challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/54058810)




