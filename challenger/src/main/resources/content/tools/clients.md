---
title: HTTP Rest Clients - Introduction
description: An overview of the most popular, and mostly free, HTTP and REST Clients.
showads: true
---

# REST HTTP Client Tools Are Essential

We Need Tools.

Web Services are designed for software and other applications to call, we humans need tools to access them.

We might be tempted to immediately think of tools for automating, but the tools listed here are being included because they support interactive exploration and testing of REST APIs.

For automating I would use a library like Rest Assured, or an HTTP library.

For exploratory testing, I would use one of the listed tools.

---

## Example REST API and HTTP Client Tools

I have used all of the following tools.

At the moment I use [Bruno](https://www.usebruno.com/) for my exploratory testing. I configure it to feed all traffic through the [BurpSuite](https://portswigger.net/burp/communitydownload) proxy to capture all my traffic and allow me to save and review my testing.

### Command Line Clients

- [cURL](https://curl.se)
    - command line based
    - API examples often shown in cURL
    - recommended that you learn this eventually
    - [download](https://curl.se/download.html)
  - [Read our More Detailed Review of cURL](/tools/clients/curl)


### GUI Clients

I recommend Bruno. It is a very capable free client, with paid plans if you want to use the tool for automated execution.

- [Bruno](https://www.usebruno.com/)
  - An [open source](https://github.com/usebruno/bruno) REST Client which can import Swagger files and is easy to configure.
  - [Read our More Detailed Review of Bruno](/tools/clients/bruno)

A list of Open Source API Clients is available at the [Awesome API Clients Github list](https://github.com/stepci/awesome-api-clients). This is a well maintained and up to date list containing more clients than are listed here.

- [Paw (Mac only)](https://paw.cloud/)
  - Free Mac only REST Client. A previously commercial REST Client which was good enough for me to buy a license.

### Other Popular clients

The tools listed here are often used for testing.

I have used Postman and Insomnia, but both have now expanded their feature set such that they are suites of API tooling and are not as simple to use for exploratory testing as they once were.

- [Postman](https://www.getpostman.com/)
  - A commercial REST Client with Free version for personal use, requires a free account and login to use effectively
- [Insomnia](https://insomnia.rest/)
  - Another commercial REST Client with Free version for personal use, which also requires a free account and login to use effectively

GUI Clients which I have not yet used, but are often mentioned by Software Testers. They may be more feature packed than required for exploratory testing.

- [Katalon Studio](https://katalon.com/katalon-studio)
- [SoapUI Open Source](https://www.soapui.org)
  - I have previously used this for testing SOAP APIs, I haven't tried it on REST APIs yet.


### Online Clients

Online clients are useful to get started quickly. One issue with Online clients is that they may experience [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) issues with some requests (e.g. `OPTIONS` is particularly problematic)

But they are very useful to experiment with an API quickly.

- [Hoppscotch](https://hoppscotch.io)
  - A well implemented browser based client. Import Postman and Swagger (Open API) collections.
- [RestTestTest](https://resttesttest.com/)
  - This is a very basic online client. Recommend using the Developer Tool Network tab to view the requests and responses.


## Features Useful For Testing

I look for the following features in an API Client that I want to use for Exploratory Testing.

I do not really use API Clients for Automating, I use them to support my testing. For Automating I tend to use code and HTTP libraries so the features below may not be the same review criteria that you apply.

- Optional
  - Read Open API files
  - Supports global environment variables e.g. for host
  - Different payload body types
  - Easy to add headers
    - custom headers
    - auth header support
    - override body type headers
  - Variables in headers e.g. for session headers
  - Custom Verb support
  - Repeating or cloning earlier requests
  - View history of requests/responses
- Essential
  - Send HTTP or HTTPS requests
  - Proxy support
  - Can create invalid requests
- Bonus
  - Data driven requests
  - Output log of test sessions
  - Scriptable for customisation - not automating

### Optional Features

The Optional features are there to make my Testing a little easier. I can live without them. I have used Proxy tools for HTTP API testing, and when testing in this way there is very little support for easy HTTP Request construction.

Most of the features are usability features. Does the tool make it easy to edit the requests? Can I create variables that I share across requests? e.g. to add custom authentication headers, or change the Host URL in a single place rather than amending on every request.

I like to be able to create a set of requests by opening an Open API file.

Any feature that makes things easier is useful but not essential.

A history of requests and responses is useful to see what I just did, and to let me see what the tool thinks it has sent and received. But if I have proxy capability then I can use the Proxy to do this.

### Essential Features

It sounds obvious, but I need to be able to make any HTTP or HTTPS request. Very often online tools are limited by CORS headers, I need tools that let me explore a wide range of requests. This includes invalid requests. I cannot allow my testing to be constrained by validations imposed upon me by the tooling. e.g. I need to be able to send invalid JSON in payloads to see how the API responds.

I also need Proxy tool support to be able to send every request through a Proxy. This lets me see exactly what is sent and received. Allows me to do basic data driven testing using fuzzing, and allows me to keep a log of my testing.

### Bonus Features

The bonus items are there because many tools don't have them, but if I'm testing for long periods of time then it is useful to:

- feed a set of data into a request and see what happens
- export what I did while testing to a log file as evidence. It is sometimes possible to get this from a Proxy.
- I like tools which are scriptable then I can use them in adhoc ways.

Scriptable needs to be easy to use and I mainly use it to support me, rather than automate. e.g. generating random data, outputting information to logs.


## Why test interactively and not just automate?

- observe traffic
- create varied requests
- experiment fast
- setup data
- send 'invalid' requests
- exploratory testing of API
- test while API still 'flexible'
- Interactive CRUD testing - CREATE, READ, UPDATE, DELETE

'Testing' is a different activity from 'Automating'.

Testing requires variation, observation and exploration, so we need tools that can help us send requests, easily vary requests and inspect the responses to find problems.

Automating helps us identify unanticipated changes in behaviour that we previously asserted as acceptable.

