---
title: HttpBin - An Attp Request and Response Service - Practice HTTP
description: An overview of the HttpBin site, explaining how to use it to learn about HTTP Requests and Responses.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [HttpBin.org](https://httpbin.org/).

# HttpBin - An Http Request and Response Server

HttpBin is not really an API, but it is an excellent test bed for exploring HTTP requests, responses and message formats.

## About HttpBin

HttpBin should not be viewed as an API. It does not conform to any API standards, nor does it implement any state.

But it is a very good place to expand your knowledge of HTTP requests.

[HttpBin.org](https://httpbin.org/) presents a Swagger style API to make HTTP requests to the server.

The site can also be run easily from docker:

```bash
docker run -p 80:80 kennethreitz/httpbin
```

Above command will run the server via docker locally on port 80 `localhost:80`

It is possible to explore the server endpoints using the swagger interface but I think you'll gain more value from making requests from an HTTP client as you can explore more combinations.

There are actually endpoints that you can explore as a tester since the Dynamic Data endpoints respond based on the inputs provided.

I think the site is useful for working through each of the endpoints, thinking through what you expect the endpoint to respond with, then making the request from an HTTP client and checking the result against your expectations.

The [mdn web docs for HTTP](https://developer.mozilla.org/en-US/docs/Web/HTTP) are a great reference to have open as you explore the endpoints.

## Links

- The main site [HttpBin.org](https://httpbin.org/)
- The source code https://github.com/postmanlabs/httpbin

## Summary

- A server for HTTP requests and responses.
- A lot of endpoints for different types of verbs and request contents.
- Good for working through the endpoints and checking your expectations


## Exercises

I've created a set of suggested exercises in case you need some prompting for practice.

### Exercise - Explore the HTTP Methods in an HTTP/REST Client

HttpBin offers endpoints for some HTTP verbs: `/delete`, `/get`, `/patch`, `/post`, `/put`

For each endpoint, think through what you expect before:

- issue a request with the verb associated with the endpoint e.g. `DELETE /delete`
- issue a request with a verb not associated with the endpoint e.g. `GET /delete`
- issue a request on the endpoint with trailing path params e.g. `/delete/`, `/delete/1`

### Exercise - Explore the Auth Methods in the Swagger Endpoint

The Auth endpoints allow you to explore valid and invalid authentication. Each of the authentication URLs define the parameters for the authentication scheme and if you issue a GET on that endpoint with the valid authentication setup then you will see an authenticated request.

- explore each authentication method (note: some you may not be able to authenticate via swagger)
- try and configure the username and passwords to accept empty values

### Exercise - Explore the Auth Methods in an HTTP/REST Client

The Auth endpoints allow you to explore valid and invalid authentication. Each of the authentication URLs define the parameters for the authentication scheme and if you issue a GET on that endpoint with the valid authentication setup then you will see an authenticated request.

- explore each of the endpoints using the HTTP Client and you should be able to authenticate with all methods

### Exercise - Explore the Status Codes

- Use the list of status codes from [mdn web docs](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) and issue them for the various verbs
- Use status codes which are outside the range of valid responses

### Exercise - Explore the Request Inspection Endpoints

The Request Inspection Endpoints are very similar to the [Api Challenges Mirror Mode](https://apichallenges.eviltester.com/practice-modes/mirror). When you make a request, the response lists your headers, or ip address, or whatever is being mirrored.

- Try the endpoints with a variety of HTTP Clients and see if the requests are what you expect
- Are there any additional headers that you didn't expect? Can you override or prevent these headers being sent?
- Are any of the headers that you see injected by the HTTP server? How would you know if the HTTP Server or the HTTP Client is adding them?
- Add invalid headers, and header formats, and multiple headers to see if they are sent to the server

These are good endpoints for making sure that your HTTP client is doing what you ask of it.

### Exercise - Explore the Dynamic Data Endpoints

The Dynamic Data Endpoints respond based on the input so it is possible to explore their handling of:

- valid and invalid data (e.g. data that is not Base64 encoded)
- missing parameters
- data that is too large

## Some Observations

### Exploring HTTP Methods

- when exploring the HTTP Method endpoints I found that it conformed more to my expectations than some of the simulation APIs
   - I received `404` when expected
   - I received `415` when expected
   - `HEAD` and `OPTIONS` also worked as I expected


### Exploring Request Inspection Endpoints

- when exploring the headers endpoint, the only way I can determine if the client is adding the header, or if it is the HTTP server, is by configuring my REST Client to use an HTTP Proxy like BurpSuite.
- My Http Proxy was also the only way I found of issuing a clean request to the server.
- I also found it interesting that when I amended the "Host" header in my REST Client (Bruno) to be an invalid host like "bob", my Proxy refused to send the message to the server. So I discovered a guard in my Proxy that I didn't know about.