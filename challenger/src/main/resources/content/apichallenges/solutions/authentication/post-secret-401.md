---
date:  2021-07-24T08:15:00Z
title: API Challenges Solution For - authentication failed 401
description: How to solve API challenge 29 - authentication failed with username and password.
---

# How to complete the authentication failed challenge

How to complete the authentication failed with username and password challenge by adding a Basic Auth header with
the wrong details. In response the API returns a status code of 401.

## 	Authentication Challenge

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password.

## Challenge 29 Authentication Failed

> Issue a POST request on the `/secret/token` end point and receive 401 when Basic auth username/password is not admin/password

- `POST` request means use the HTTP Verb POST
    - e.g. `POST /secret/token` sends to the secret token endpoint
- `Basic auth` means include the [Basic Authorization header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)
- `username/password is not admin/password` the authorisation header value is base 64 encoded, and the details should not match `admin` as the username, and `password` for the password
- add the `X-CHALLENGER` header to track progress and because the authentication code we need is asociated with the `X-challenger` session
- Receive a 401 response


## Basic Instructions

- Create a new request for the `/secret/token` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/secret/token`
- The verb should be a `POST`
- Add a Basic Auth header by selecting "Basic" from the "Auth" tab and entering a username and password but make sure it is not admin/password e.g. use username "Admin1", password "Pa55word" (or anything else you want)
- There should be no payload in the message
- You should receive a 401 response - meaning "Unauthorized" because you entered the wrong username or password
- The request should have an `X-CHALLENGER` header to track challenge completion

## Example Request

~~~~~~~~
> POST /secret/token HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Authorization: Basic YWRtaW46cGFzc3dvcmRk
> Accept: */*
> Content-Length: 0
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 401 Unauthorized
< Connection: close
< Date: Sat, 24 Jul 2021 11:13:04 GMT
< Www-Authenticate: Basic realm="User Visible Realm"
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Content-Type: text/html;charset=utf-8
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

The `Authorization` header does not send the username and password in plain text, it uses Base64 to obscure the details.

You could see what username and password I used by typing the Base64 string `YWRtaW46cGFzc3dvcmRk` into a Base64 decoder like https://www.base64decode.org/

Or you could decode it in the browser dev console by typing:

```javascript
atob('YWRtaW46cGFzc3dvcmRk')
```

The command to encode a string as base64 is `btoa`

Although we add an "Authorization" header, really we are trying to "authenticate" with a set of user details.

- Authorization is "do you have the right permissions"
- Authentication is "are you who you say you are"

## Additional Exercises

- try creating a base64 Authorization header by hand, without using the "Auth" tab in Insomnia


## Overview Video

{{<youtube-embed key="RSQGADU3SLA" title="Solution to failed basic auth challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/54057993)




