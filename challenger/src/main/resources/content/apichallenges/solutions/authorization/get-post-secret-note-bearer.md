---
date:  2021-07-25T09:30:00Z
title: API Challenges Solution For - Use Bearer Tokens
description: How to solve API challenge - Use bearer token as authorization mechanism
---

# How to complete the Bearer Token challenges

Another authentication mechanism is the Bearer Token. Each API Challenger has a secret note bearer token that can be used to authenticate and GET or POST the secret note.

## 	Bearer Token Secret Note Challenges

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password. This value is obtained when completing [challenge post secret 201](/apichallenges/solutions/authentication/post-secret-201).

The `X-CHALLENGER` header authenticates you to access a specific set of secret notes, and the `X-AUTH-TOKEN` authorizes you to gain access.

- Authentication is "are you who you say you are" (`X-CHALLENGER`)
- Authorization is "do you have the right permissions" (`X-AUTH-TOKEN`)

Both Challenges 37 and 38 use the Bearer authentication mechanism and are so similar that we have covered them in one post.

Rather than use the `X-AUTH-TOKEN` header, we use the value returned in Challenge 30 for the `X-AUTH-TOKEN` but we add it as `Bearer` token authentication.

In Insomnia, use the "Auth" tab and select "Bearer" authentication. Then the toke value is the value of the `X-AUTH-TOKEN` from Challenge 30.

## Challenge 37 GET /secret/note (Bearer)

> Issue a GET request on the `/secret/note` end point receive 200 when using the X-AUTH-TOKEN value as an Authorization Bearer token - response body should contain the note

This challenge is almost a duplicate of the request used in challenge 33

- Create a new request for the `/secret/note` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/secret/note`
- The verb should be a `GET`
- Ensure there is no custom header with the name `X-AUTH-TOKEN`
- Ensure there is an Auth Bearer header and the value is the same as received in the `/secret/token` response `X-AUTH-TOKEN`
- The request should have an `X-CHALLENGER` header to track challenge completion
- You should receive a 200 response and the body of the response will contain the secret note

## Example Request

```
> GET /secret/note HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> Authorization: Bearer x-auth-token-value
> Accept: */*
```

## Example Response

```
< HTTP/1.1 200 OK
< Connection: close
< Date: Sun, 25 Jul 2021 13:20:04 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
```

The response body would contain a "note":

```angular2html
{
  "note": "my note is here"
}
```

## Challenge 38 POST /secret/note (Bearer)

> Issue a POST request on the `/secret/note` end point with a note payload e.g. {"note":"my note"} and receive 200 when valid X-AUTH-TOKEN value used as an Authorization Bearer token. Status code 200 received. Note is maximum length 100 chars and will be truncated when stored.

This is almost a duplicate of challenge 34.

- Create a new request for the `/secret/note` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/secret/note`
- The verb should be a `POST`
- Ensure there is no custom header with the name `X-AUTH-TOKEN`
- Add Bearer Token Auth where the token value is the same as received in the `/secret/token` response for the `X-AUTH-TOKEN`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Add a JSON Payload of the format `{"note":"my note"}`
- Include header for `Content-type` value `application/json`
- If the text is too long it will be truncated
- You should receive a 200 response
- The body of the response will contain the secret note

## Example Request

```
> POST /secret/note HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Authorization: Bearer x-auth-token-value
> Accept: */*
> Content-Length: 37

| {
|   "note": "my note edited bearer"
| }
```

## Example Response

```
< HTTP/1.1 200 OK
< Connection: close
< Date: Sun, 25 Jul 2021 13:24:20 GMT
< X-Challenger: x-challenger-guid
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
```

Sample response body:

```json
{
  "note": "my note edited bearer"
}
```


## Overview Video

{{<youtube-embed key="8GsMTZxEItw" title="Solution to Bearer Token Challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/54091910)




