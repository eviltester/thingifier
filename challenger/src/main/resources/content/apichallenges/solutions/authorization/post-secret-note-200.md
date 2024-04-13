---
date:  2021-07-25T09:00:00Z
title: API Challenges Solution For - POST amend secret note 200
description: How to solve API challenge 34 - amend the secret note with a POST request and receive 200 status code
---

# How to complete the POST secret note challenge

When we are authenticated and authorized we can amend secured user information.
In the API Challenges we do this by POST request to the user's secrete note endpoint which returns a status code of 200 and amends the secret note.

## 	POST Amend Secret Note Challenge

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password. This value is obtained when completing [challenge post secret 201](/apichallenges/solutions/authentication/post-secret-201).

The `X-CHALLENGER` header authenticates you to access a specific set of secret notes, and the `X-AUTH-TOKEN` authorizes you to gain access.

- Authentication is "are you who you say you are" (`X-CHALLENGER`)
- Authorization is "do you have the right permissions" (`X-AUTH-TOKEN`)


## Challenge 34 POST Amend

> Issue a POST request on the `/secret/note` end point with a note payload e.g. {"note":"my note"} and receive 200 when valid X-AUTH-TOKEN used. Note is maximum length 100 chars and will be truncated when stored.

- `POST` request means use the HTTP Verb POST
  - e.g. `POST /secret/note` sends to the secret note endpoint
- `with a note payload` include a `JSON` formatted object as the payload    
- `valid X-AUTH-TOKEN used` means a custom header named `X-AUTH-TOKEN` should be added to the message with the value received from the `/secret/token` response in Challenge 30
- add the `X-CHALLENGER` header to track progress and authenticate the request
- Receive a 200 response because both `X-CHALLENGER` and `X-AUTH-TOKEN` are for the same user and the payload was well formatted.


## Basic Instructions

- Create a new request for the `/secret/note` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/secret/note`
- The verb should be a `POST`
- Ensure there is a custom header with the name `X-AUTH-TOKEN` and the value is the same as received in the `/secret/token` response
- The request should have an `X-CHALLENGER` header to track challenge completion
- Add a JSON Payload of the format `{"note":"my note"}`
- Include header for `Content-type` value `application/json`  
- If the text is too long it will be truncated  
- You should receive a 200 response
- The body of the response will contain the secret note


## Example Request

~~~~~~~~
> POST /secret/note HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> X-AUTH-TOKEN: x-auth-token-value
> Content-Type: application/json
> Authorization: Basic YWRtaW46cGFzc3dvcmQ=
> Accept: */*
> Content-Length: 31

| {
|   "note": "my note is here"
| }
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sun, 25 Jul 2021 11:47:36 GMT
< X-Challenger: x-challenger-guid
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example body of the response:

```javascript
{
    "note": "my note is here"
}
```

## Extras

- Try varying the length of the note... does the system truncate as expected?

## Overview Video

{{<youtube-embed key="A9T9yjzEOEE" title="Solution to POST authorization challenge using header">}}

[Patreon ad free version](https://www.patreon.com/posts/54090441)




