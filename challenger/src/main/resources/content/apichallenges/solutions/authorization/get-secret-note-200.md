---
date:  2021-07-25T08:45:00Z
title: API Challenges Solution For - GET authorized secret note 200
description: How to solve API challenge 33 - authorized to access secret note 200
---

# How to complete the GET Authorized secret note challenge

To access the secret note we need to be Authenticated and Authorized, only then can we GET protected information.
The API Challenge returns a status code of 200 and the secret note when we are authorized to do so.

## 	Authorization Challenge

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password. This value is obtained when completing [challenge post secret 201](/apichallenges/solutions/authentication/post-secret-201).

The `X-CHALLENGER` header authenticates you to access a specific set of secret notes, and the `X-AUTH-TOKEN` authorizes you to gain access.

- Authentication is "are you who you say you are" (`X-CHALLENGER`)
- Authorization is "do you have the right permissions" (`X-AUTH-TOKEN`)


## Challenge 33 Authorized

> Issue a GET request on the `/secret/note` end point receive 200 when valid X-AUTH-TOKEN used - response body should contain the note

- `GET` request means use the HTTP Verb GET
    - e.g. `GET /secret/note` sends to the secret note endpoint
- `valid X-AUTH-TOKEN used` means a custom header named `X-AUTH-TOKEN` should be added to the message with the value received from the `/secret/token` response in Challenge 30
- add the `X-CHALLENGER` header to track progress
- Receive a 200 response because both `X-CHALLENGER` and `X-AUTH-TOKEN` are for the same user. The Response should contain the content of the secret note.

## Basic Instructions

- Create a new request for the `/secret/note` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/secret/note`
- The verb should be a `GET`
- Ensure there is a custom header with the name `X-AUTH-TOKEN` and the value is the same as received in the `/secret/token` response
- The request should have an `X-CHALLENGER` header to track challenge completion
- You should receive a 200 response and the body of the response will contain the secret note

## Example Request

~~~~~~~~
> GET /secret/note HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> X-AUTH-TOKEN: x-auth-token-value
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sun, 25 Jul 2021 11:02:17 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example body of the response:

```javascript
{
  "note": "my note edited"
}
```

## Overview Video

{{<youtube-embed key="2uRpzr2OmEY" title="Solution to Get authorization challenge using header">}}

[Patreon ad free version](https://www.patreon.com/posts/54089625)




