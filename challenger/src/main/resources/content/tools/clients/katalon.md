---
title: Katalon Studio HTTP Rest Client - Overview and Review
description: An overview of the Katalon Studio HTTP REST API Client.
showads: true
---

# Katalon Studio

Katalon Studio has a free plan. Free, user-friendly test automation tools to simplify testing for individuals and small teams. It does not support Exploratory Testing well.

## About Katalon Studio

[katalon Studio Free](https://katalon.com/download-studio-free) is advertised as a tool for test automation.

So to be fair to Katalon, I'm probably evaluating it for the wrong use case as it does not show up in searches for "REST API Testing Client".

However, part of the feature set is working with Open API files and manually issuing REST API calls prior to automating them.

I chose to evaluate it because it does appear in other recommended lists for API Testing Tools.

I found it to be too slow at sending requests manually to be usable as an Exploratory Testing tool.

- Can not import Open API files v3 on free plan without editing the file.
- Slow to send requests.
- Clumsy login and account creation process which is required to use the tool.
- No obvious proxy support.
- Not designed to support Exploratory Testing.


## Quick Review Criteria

I didn't fully review the tool hence the incompleteness of the table. I found it was too slow to send requests and so abandoned the review.


| Feature                                             | Y/N | Comment                                                           |
|-----------------------------------------------------|-----|-------------------------------------------------------------------|
| **Essential**                                       |     |                                                                   |
| Send HTTP or HTTPS requests                         | Y   |                                                                   |
| View the actual requests and responses | Y   | In the HAR view                                                   |
| Proxy support                                       | N   |                                                                   |
| Can create invalid requests                         | Y   |                                                                   |
| **Optional**                                        |     |                                                                   |
| Read Open API files                                 | Y   | Not Open API v3                                                   |
| Supports global environment variables e.g. for host | ?   |                                                                   |
| Different payload body types                        | Y   |                                                                   |
| Easy to add headers                                 | Y   |                                                                   |
| - custom headers                                    | Y   |                                                                   |
| - auth header support                               | Y   |                                                                   |
| - override body type headers                        | Y   | It is possible to edit as JSON and set content-type header to XML |
| Variables in headers                                | ?   |                                                                   |
| Custom Verb support                                 | N   |                                                                   |
| Repeating or cloning earlier requests               | Y   | But not from the History, only the Request UI                     |
| View history of requests/responses                  | N   | Only last request and responses can be viewed.                    |
| Exporting requests to cURL format                   | N   |                                                                   |
| Sends Diagnostic information to server              | ?   | Not configurable through proxy to check                           |
| **Bonus**                                           |     |                                                                   |
| Data driven requests                                | ?   |                                                                   |
| Output log of test sessions                         | N   |                                                                   |
| Scriptable for customisation                        | ?   |
| Importing cURL requests                             | N   |                                                                   |
| Free for commercial use | Y   |                                                                   |


## Summary - Not designed for Exploratory Testing, designed for Automated Execution of Requests

I don't think the tool was designed to support Exploratory Testing. It was frustratingly slow to send requests and I abandoned the review.

It has so many features related to automating that I should just not consider it an option for supporting exploratory testing.

## Review Notes

[katalon.com](https://katalon.com)

I downloaded Katalon Studio Free

https://katalon.com/download-studio-free

And the first thing I have to do is login. Which is a big barrier to entry.

I created an account because I didn't trust the login with Google and Github dialogs. I would have preferred pop up, normal Oauth login.

This also meant going through an annoying "I am not a robot" process, clicking on images.

First thing I tried to do was import an OpenAPI File.

"You are importing OpenAPI 3, which requires a Studio Enterprise License"

I managed to import a file using the same approach required for SoapUI Open API json importing. Requests are then found in the Object Repository.

Requests were very slow to send. I had to open Bruno and issue a request to check that it was the Katalon tool and not the server. It was the Katalon tool.

It is not possible to view the actual request sent, only one response is shown, there sees to be no history log. It is possible to export the request and response from a HAR view, so this is where you can see the request.

No headers are added to the request by default according to the HAR output.

Code completion is available on header names and values via drop down UI elements.

I tried to create an adhoc Rest API Request, but could not send it for some reason. No errors were reported on screen. I could only use the requests imported from the amended Open API file.

I could not find a way to change Proxy.

Since the requests were frustratingly slow to send I gave up.

This is clearly not a tool designed to support interactive exploratory testing. It has so many features related to automating that I should just not consider it an option for supporting exploratory testing.
