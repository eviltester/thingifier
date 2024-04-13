---
date:  2021-07-25T09:15:00Z
title: API Challenges Solution For - POST Unauthorised 401 403
description: How to solve API challenge - fail to amend the secret note with a POST request and receive 401 and 403 status codes
---

# How to complete the Unauthorised POST secret note challenges

When unauthorized, the API Challenge API will return status codes of 401 and 403 and fail to amend the secret note.

## 	POST Amend Secret Note Challenge

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password. This value is obtained when completing [challenge post secret 201](/apichallenges/solutions/authentication/post-secret-201).

The `X-CHALLENGER` header authenticates you to access a specific set of secret notes, and the `X-AUTH-TOKEN` authorizes you to gain access.

- Authentication is "are you who you say you are" (`X-CHALLENGER`)
- Authorization is "do you have the right permissions" (`X-AUTH-TOKEN`)

Both Challenge 35 and 36 are so similar that we have covered them in one post.

Following on from challenge 35 were we successfully amended a post. Now we try to repeat the same requests but

- Challenge POST Amend no AUTH TOKEN - remove the X-AUTH-TOKEN header
- Challenge POST Amend Invalid AUTH TOKEN - the X-AUTH-TOKEN header has the wrong value

## Challenge - POST Amend no AUTH TOKEN

> Issue a POST request on the `/secret/note` end point with a note payload {"note":"my note"} and receive 401 when no X-AUTH-TOKEN present

- `POST` request means use the HTTP Verb POST
    - e.g. `POST /secret/note` sends to the secret note endpoint
- `with a note payload` include a `JSON` formatted object as the payload
- `no X-AUTH-TOKEN present` means do not inlude a custom header named `X-AUTH-TOKEN`
- add the `X-CHALLENGER` header to track progress and authenticate the request
- Receive a 401 response because the `X-AUTH-TOKEN` is missing.

## Example Request

```
> POST /secret/note HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 23

| {
|   "note": "my note"
| }
```

## Example Response

```
< HTTP/1.1 403 Forbidden
< Connection: close
< Date: Sun, 25 Jul 2021 12:53:51 GMT
< X-Challenger: x-challenger-guid
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
```



## Challenge POST Amend Invalid AUTH TOKEN

> Issue a POST request on the `/secret/note` end point with a note payload {"note":"my note"} and receive 403 when X-AUTH-TOKEN does not match a valid token

- same basic message as previous challenge but the `X-AUTH-TOKEN` header is included, but the value does not match the value returned from challenge `/secret/token` request.

## Example Request

```
> POST /secret/note HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> X-AUTH-TOKEN: bob
> Content-Type: application/json
> Accept: */*
> Content-Length: 23

| {
|   "note": "my note"
| }
```

## Example Response

```
< Connection: close
< Date: Sun, 25 Jul 2021 12:57:42 GMT
< X-Challenger: x-challenger-guid
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
```


## Overview Video

{{<youtube-embed key="A9T9yjzEOEE" title="Solution to POST unauthorized and forbidden challenges using header">}}

[Patreon ad free version](https://www.patreon.com/posts/54091910)




