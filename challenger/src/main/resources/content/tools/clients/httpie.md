---
title: Httpie HTTP API Rest Client - Overview and Review
description: An overview of the Httpie HTTP REST API Client.
showads: true
---

# Httpie

Httpie is a Desktop Open Source API client. An expansion of the Httpie CLI client.

Free to use for commercial environments but requires an account for some advanced features. Missing features required for Exploratory Testing.

## About Httpie

I've seen HTTPIE listed as a CLI tool. But when I visited the site it is now a Desktop and CLI tool.

Httpie is an Open Source API client with a login for some advanced features.

Httpie can be found at:

- [Httpie](https://httpie.io/)

I should start with the limitations. These were enough that they prevent me recommending the tool as an Exploratory Testing tool, given the competition in this space.

- does not support loading Open API files - only Postman or Insomnia, so in theory could convert using those tools but this seems like a limitation
- proxy does not seem to be supported from Desktop App
- account required to choose where to save files locally

For me, the proxy support is essential. I wouldn't recommend Exploratory Testing without the ability to hook a client up to a Proxy.

In all other respects it seems very competent with a functional UI.

- can create environment variables these are shared across all collections in a 'space'
- can create custom methods
- good cURL support, can generate cURL and create requests from cURL
- it is possible to override content type
- body is syntax checked and it is possible to send through invalid payloads
- authentication is easy to add
- code completion on headers
- no scripting capabilities
- can only see the 'last' response, not a history of responses
- can see the request sent.

I found that the tool is prioritising AI features rather than the competitive basics. So you'll b enable to generate requests from a prompt, but you won't be able to view the history of requests through a proxy or extend the tool with scripting. But if you want AI support then this is the first of the simple clients I've seen support it.

Creating an account and logging in is optional, doing so allows you to create a space on your disk in a location of your choosing, and access the AI.

## Quick Review Criteria

| Feature                                             | Y/N | Comment                                                           |
|-----------------------------------------------------|-----|-------------------------------------------------------------------|
| **Essential**                                       |     |                                                                   |
| Send HTTP or HTTPS requests                         | Y   |                                                                   |
| View the actual requests and responses | Y   | Last request and response can be viewed                           |
| Proxy support                                       | N   |                                                                   |
| Can create invalid requests                         | Y   |                                                                   |
| **Optional**                                        |     |                                                                   |
| Read Open API files                                 | N   | Can read Postman and Insomnia collections                         |
| Supports global environment variables e.g. for host | Y   |                                                                   |
| Different payload body types                        | Y   |                                                                   |
| Easy to add headers                                 | Y   |                                                                   |
| - custom headers                                    | Y   |                                                                   |
| - auth header support                               | Y   |                                                                   |
| - override body type headers                        | Y   | It is possible to edit as JSON and set content-type header to XML |
| Variables in headers                                | Y   |                                                                   |
| Custom Verb support                                 | Y   |                                                                   |
| Repeating or cloning earlier requests               | Y   | But not from the History, only the Request UI                     |
| View history of requests/responses                  | N   | Only last request and response can be viewed                      |
| Exporting requests to cURL format                   | Y   |                                                                   |
| Sends Diagnostic information to server              | ?   | Not possible to configure through proxy                           |
| **Bonus**                                           |     |                                                                   |
| Data driven requests                                | N   |                                                                   |
| Output log of test sessions                         | N   |                                                                   |
| Scriptable for customisation                        | N   | 'Chaining' can be used to 'pull' values from earlier responses.   |
| Importing cURL requests                             | Y   |                                                                   |
| Free for commercial use | Y   |                                                                   |


## Summary - Pretty but lacks basic features

Httpie was pretty but did not meet my basic needs.

The lack of Open API import would hinder most people using it in production environments.

The lack of Proxy support means that I wouldn't consider it for exploratory testing.