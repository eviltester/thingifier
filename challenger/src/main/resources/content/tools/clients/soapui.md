---
title: Soap UI HTTP Rest Client - Overview and Review
description: An overview of the Soap UI HTTP REST API Client.
showads: true
---

# SoapUI

SoapUI is an Open Source API client designed for local use. One of the earliest HTTP clients, and is now showing its age.

## About SoapUI

[SoapUI](https://www.soapui.org/) is an Open Source API client. The paid version with additional features is now called RapiAPI.

SoapUI was one of the early HTTP clients and I remember using it to test Soap APIs. SoapUI is written in Java and has a slightly confusing UI with multiple floating windows within the main Desktop UI.

- [SoapUI official site](https://www.soapui.org/)
- [SoapUI on github](https://github.com/SmartBear/soapui)

- Can not import Open API files v3 without editing the file to change `"openapi":"3.0.1"` to `"swagger":"2.0"`
- Text feels small on a large monitor with no way to configure.
- A little clumsy to get started with.
- Can view raw requests
- Can view and save a log of historic requests and responses.
- Support for Auth headers
- Other headers add manually to request
- Can not issue custom methods
- Can be configured through a proxy.
- Can create adhoc load tests from requests.
- Sends diagnostic usage data to `mixpanel.com` with no way to switch this off e.g. what verbs are sent, other UI usage.

## Quick Review Criteria

| Feature                                             | Y/N | Comment                                                           |
|-----------------------------------------------------|-----|-------------------------------------------------------------------|
| **Essential**                                       |     |                                                                   |
| Send HTTP or HTTPS requests                         | Y   |                                                                   |
| View the actual requests and responses | Y   | Raw requests in UI                                                |
| Proxy support                                       | Y   |                                                                   |
| Can create invalid requests                         | Y   |                                                                   |
| **Optional**                                        |     |                                                                   |
| Read Open API files                                 | Y   | Does not support Open API 3                                       |
| Supports global environment variables e.g. for host | N   |                                                                   |
| Different payload body types                        | Y   |                                                                   |
| Easy to add headers                                 | Y   |                                                                   |
| - custom headers                                    | Y   |                                                                   |
| - auth header support                               | Y   |                                                                   |
| - override body type headers                        | Y   | It is possible to edit as JSON and set content-type header to XML |
| Variables in headers                                | N   |                                                                   |
| Custom Verb support                                 | N   | But has more than most cleints                                    |
| Repeating or cloning earlier requests               | Y   | From the Request UI                                               |
| View history of requests/responses                  | Y   | Shown in HTTP Log, which can be exported to file                  |
| Exporting requests to cURL format                   | N   |                                                                   |
| **Bonus**                                           |     |                                                                   |
| Data driven requests                                | N   |                                                                   |
| Output log of test sessions                         | N   |                                                                   |
| Scriptable for customisation                        | N   | 'tests' can be scripted                                           |
| Importing cURL requests                             | N   |                                                                   |
| Free for commercial use | Y   | The paid version is a different product                           |


## Summary - Works, but sends diagnostic usage information

Once you get used to the UI it does work, but there are now easier tools to use. This would not be my default tool.

I don't like the diagnostic information being sent to mixpanel by default, there should be an option to switch this off.

I can see the adhoc load tests being useful

## Review Notes

[SoapUI](https://www.soapui.org/) is now two products. SoapUI is free and open source. ReadyAPI is a paid product with more features: data-driven testing, coverage testing, scripting, etc.

I'll be reviewing SoapUI, the free open source version.

[SoapUI on github](https://github.com/SmartBear/soapui)

Written in Java, can be run from source if you want. Has an easy to use installer.

When SoapUI starts it jumps straight into an Endpoint Explorer allowing you to make basic API Requests without creating a collection. Unfortunately it is a modal dialog and SoapUI takes over the main screen so I chose not to show this window on launch.

To import an Open API file I created a new REST Project, then it is possible to import Open API files from the project menu or context menu.

You can only import from URL if the API is on swagger Hub, but you can import from file, so I downloaded the Open API File and tried to import it.

But received an error:

```
java.lang.NullPointerException: Cannot invoke "com.smartbear.swagger.SwaggerImporter.importSwagger(String)" because "this.val$importer" is null
```

This was the same file that I've imported into multiple clients during the review process.

I found a bug report on the community for 2 years ago:

https://community.smartbear.com/discussions/soapui_os/problem-with-importing-openapi/242050

Suggesting that json files need to be amended to change `"openapi":"3.0.1"` to `"swagger":"2.0"`

That did work, but this is clearly a long standing bug in the product. And not a good first sign.

The environments were not picked up from the file so I amended the project in the UI to have a base Path.

At this point I notice how clumsy the UI feels. The text is small and there are too many floating windows inside the internal desktop window.

I then realised that I had made a mistake, I should not have added `https://apichallenges.eviltester.com` to the base path, I need to add that as the `Endpoint` in the request view.

Having done that I was finally able to make a GET request on `/simpleapi/items`

The raw request is visible in the dialog in the Raw tab.

And can be viewed in the http log tab. This log can be saved to a file, which is good. The first tool I've encountered in this review process which can do this out of the box.

Headers are not added by default so I added an `accept` header. There is no code completion on header names or values.

Adding an auth header is done via a dialog so is easy to configure.

The payload editing part of the dialog is not available with GET requests so I can't create an 'invalid' GET request with a payload.

There is no syntax checking in the editor . It is possible to override the Media type in the editor with a header, and the media type can be customised in the editor itself.

I could not find a way to create project level variables that I could share across requests.

Can not issue custom methods

I was able to configure a proxy fairly easily. I had to scroll up in the dialog to see the manual button. But when I did, I also saw that the application was sending usage information to mixpanel.com. The information is base64 encoded and it sends information about the Method used, but not the content of the request. I could see no way of switching this off. It makes me uncomfortable to see this and I don't imagine that it is acceptable in many commercial environments.

I can see that it is possible to add requests into a TestSuite and create assertions for them.

For exploratory testing the more useful option is the creation of adhoc load tests.

The EndPoint explorer is a fairly fast way to issue random requests, feels a lot like Cartero but SoapUI has no cURL support.

Cons:

- does not import Open API v3 files in JSON format without amending file
- UI is a bit clunky,
- takes time to get used to
- no shared environment variables
- cannot switch off product usage data sent to mixpanel.com
- free and open source

Pros:
- can make basic requests
- can work through a proxy
- can create collections
- can create adhoc load tests
- can output the http log for a test session
- no cURL support
