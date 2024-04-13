---
title: HTTP Rest Clients - Introduction
description: An overview of the most popular, and mostly free, HTTP and REST Clients.
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

At the moment I primarily use [Bruno](https://www.usebruno.com/) or [Thunderclient](https://www.thunderclient.com/) for my exploratory testing. I configure it to feed all traffic through the [BurpSuite](https://portswigger.net/burp/communitydownload) proxy to capture all my traffic and allow me to save and review my testing.

### Command Line Clients

- [cURL](https://curl.se)
    - command line based
    - API examples often shown in cURL
    - recommended that you learn this eventually
    - [download](https://curl.se/download.html)


### GUI Clients

- [Bruno](https://www.usebruno.com/)
  - An [open source](https://github.com/usebruno/bruno) REST Client which can import Swagger files and is easy to configure. 
- [Advanced REST Client](https://www.advancedrestclient.com/home)
  - An electron based [open source client](https://github.com/advanced-rest-client/arc-electron), not updated often
- [Paw (Mac only)](https://paw.cloud/)
  - Free Mac only REST Client. A previously commercial REST Client which was good enough for me to buy a license.
- [Postman](https://www.getpostman.com/)
  - A commercial REST Client with Free version for personal use, requires a free account and login to use effectively
- [Insomnia](https://insomnia.rest/)
  - Another commercial REST Client with Free version for personal use, which also requires a free account and login to use effectively


GUI Clients which I have not yet used:

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

### IDE Extensions

VS Code:

- [Thunderclient](https://www.thunderclient.com/)


---

## cURL Overview

~~~~~~~~
curl  {{<ORIGIN_URL>}}/heartbeat -i
curl -X GET {{<ORIGIN_URL>}}/todos
curl {{<ORIGIN_URL>}}/todos -H "accept: application/xml"
~~~~~~~~

[cURL](https://curl.se) Can be complicated but useful for emergencies, scripting, bug reporting.

> **HINT**: you can use Postman or Insomnia to generate cURL code but different continuation characters on different operating systems: `^` Windows and `\` on Mac/Linux also `"` and `'` differences.

> **NOTE**: By default Windows Powershell has a `curl` command but it is not really curl. It is an alias around `Invoke-WebRequest` and does not have all cURL features or use all the command line parameters. e.g. all the cURL examples listed above would fail. But if you change `curl` to `curl.exe` you can start using the non-alias Windows preinstalled `cURL` tool.

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