# POST /challenger (201)

> Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion.

- This challenge is essential if you want to persist your sessions in multi-user mode
- This challenge is optional if you want to work in single-user mode

## CURL

~~~~~~~~
curl --request POST \
  --url http://localhost:4567/challenger \
  -v
~~~~~~~~

~~~~~~~~
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 4567 (#0)
> POST /challenger HTTP/1.1
> Host: localhost:4567
> User-Agent: curl/7.64.1
> Accept: */*
> 
< HTTP/1.1 201 Created
< Date: Tue, 28 Jul 2020 15:11:56 GMT
< X-CHALLENGER: rest-api-challenges-single-player
< Location: /gui/challenges
< Content-Type: text/html;charset=utf-8
< Transfer-Encoding: chunked
< Server: Jetty(9.4.z-SNAPSHOT)
< 
* Connection #0 to host localhost left intact
* Closing connection 0
~~~~~~~~

## Basic Instructions

- Issue a POST request to end point "/challenger"
    - if running locally that would be
        - 'http://localhost:4567/challenger' 
    - if running in the cloud that would be
        - 'https://apichallenges.herokuapp.com/challenger' 
- The response will have an `X-CHALLENGER` header
- Use this in any future requests to track your progress
- The `LOCATION` header has a url to access your challenge status through the GUI

## Insomnia

~~~~~~~~
POST /challenger HTTP/1.1
Host: localhost:4567
User-Agent: insomnia/6.5.4
Accept: */*
Content-Length: 0
~~~~~~~~


~~~~~~~~
HTTP/1.1 201 Created
Date: Tue, 28 Jul 2020 14:26:48 GMT
X-CHALLENGER: rest-api-challenges-single-player
Location: /gui/challenges
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Server: Jetty(9.4.z-SNAPSHOT)
~~~~~~~~


_NOTE: the first version of apichallenger.jar had a bug where the `X-CHALLENGER` header would be repeated. In theory this wouldn't make any real difference, but these are the types of bugs which can cause applications issues, particularly if the values were different. It is always worth reading the headers carefully to make sure they are as we expect. This type of issue is more severe on the server side, so is worth testing for. It can cause the server side to only validate one of the headers, but potentially allow unvalidated data to be processed. This can also be quite hard to generate because often our tooling prevents us from generating invalid requests._


---

## Video

> API Challenges - How to Solve POST Challenger 201 in Insomnia

[youtu.be/tNGuZMQgHxw](https://youtu.be/tNGuZMQgHxw)

Learn how to solve the first API Challenge, which requires:

- POST an HTTP Request
- identify a Custom HTTP Header
- Use a custom HTTP Header in a request

This video shows how to complete the challenge using Insomnia.

Find the application links and more information at:

- https://eviltester.com/apichallenges

---

Patreon ad free (Insomnia version): https://www.patreon.com/posts/39882254