---
title: Postman HTTP Rest Client - Overview and Review
description: An overview of the Postman HTTP REST API Client.
showads: true
---

# Postman

Postman is a free to use REST API Client that requires a login to use properly. It has many features and this can make the UI hard to get used to.

## About Postman

When [Postman](https://www.postman.com/) was first released, it was a very simple Electron application which could create a collection of requests and make them easy to send. It was perfect for Exploratory Testing. There were few competitors and I remember paying around $10 to get an early version of cloud storage to try and support the project.

Now it is one of the largest API Suites. This has the side effect that it has more features than you will use, crammed into a UI, which I find makes it a little cluttered and harder to use. I tend to choose a simpler client for Exploratory Testing.

I have also had to email Postman support in the past to clarify their license agreement. Now the website is clearer and it is obvious that the API Client can be used for free in commercial environments no matter the team size.

- Import many collection formats
- Export postman formats
- Easy to configure proxy
- Simple variable support
- Most features required for exploratory testing in free plan
- Scripting available in free plan.
- Will probably be the default client for most people.
- Feature Cluttered UI that takes time to get used to.
- Collections stored in cloud, limited sharing on free plan. Be careful what you make public.


## Quick Review Criteria

| Feature                                             | Y/N | Comment                                                          |
|-----------------------------------------------------|-----|------------------------------------------------------------------|
| **Essential**                                       |     |                                                                  |
| Send HTTP or HTTPS requests                         | Y   |                                                                  |
| View the actual requests and responses | N   | Can see the last response, but not the full actual raw request.  |
| Proxy support                                       | Y   |                                                                  |
| Can create invalid requests                         | Y   |                                                                  |
| **Optional**                                        |     |                                                                  |
| Read Open API files                                 | Y   |                                                                  |
| Supports global environment variables e.g. for host | Y   | Editable from a JSON object view                                 |
| Different payload body types                        | Y   |                                                                  |
| Easy to add headers                                 | Y   |                                                                  |
| - custom headers                                    | Y   |                                                                  |
| - auth header support                               | Y   |                                                                  |
| - override body type headers                        | Y   | It is possible to edit as JSON and set content-type header to XML |
| Variables in headers                                | Y   |                                                                  |
| Custom Verb support                                 | Y   |                                                                  |
| Repeating or cloning earlier requests               | Y   | From the hierarchical view or history view.                      |
| View history of requests/responses                  | Y   | A History is stored but not exportable                           |
| Exporting requests to cURL format                   | Y   |                                                                  |
| Sends Diagnostic information to server              | N   | Does not seem to send diagnostic information                      |
| **Bonus**                                           |     |                                                                  |
| Data driven requests                                | Y   | From the 'run' dialog where a set of requests is executed    |
| Output log of test sessions                         | N   |                                                                  |
| Scriptable for customisation                        | Y   |                                                                  |
| Importing cURL requests                             | Y   |                                                                  |
| Free for commercial use | Y   | Additional features are available on paid plan                   |

Postman is probably the most fully featured of all the clients, but I find the UI cluttered and the login requirement annoying. I am always slightly paranoid that my collection is being saved in the public workspace. I prefer to work locally. But this seems to be the most popular API Client, or perhaps just has the largest marketing department, and the Postman collection format is becoming a standard export and import format.

## Summary - Free to use. Requires logging in and Practice to get the most of.

The non-login 'lightweight API Client' is like the Endpoint Explorer of [Soap UI](/tools/clients/soapyi) or [Cartero](/tools/clients/summary-reviews). This can be used for very adhoc testing.

Requires login to get the most of. It is a very capable tool and you get a lot for free, but I find the UI to be cluttered and hard to use. I'm sure you get used to it if you use it as your main API Client, but I don't use all the features so I tend to use a simpler tool.

## Review Notes

[getpostman.com](https://getpostman.com)

Without logging in you get access to the 'lightweight API Client' which allows you to paste in cURL Requests, or create basic requests.

It is like the Endpoint Explorer from SoapUI but much more usable.

- custom methods
- import/export cURL
- history of requests and responses sent (but not exportable)
- configure proxy in settings

This mode is suitable for very adhoc exploratory testing.

You cannot import or export the session as a collection so it is very adhoc and lightweight.

Logging in gives access to more features.

I do find the Postman UI confusing because it tries to cram so much functionality into a tiny space.

Sometimes the send button says 'try' and then sometimes it is 'send'.

It is not possible to see the actual request sent.

The curl snippet does not match the exact request sent from Postman because postman adds additional headers.

The history view by default does not save the response by default. So you have to configure the save response setting. Also the 'actual' request with the custom postman headers it not saved e.g the postman token. It isn't a raw request history.

It is possible to re-issue requests from the history view, where it acts like the lightweight API Client but by amending prior to resending you amend the previously sent item and then send it as new one. I found this all a little confusing.

One thing you have to be careful about is to avoid accidentally adding items to your public workspace.

It is possible to export collections to disk.

I'm sure, with time, I could get used to the UI, but I don't need the complexity on offer here.

Looking through the pricing and licensing page https://www.postman.com/pricing/ it does seem as though there has been an effort to clean this up and make it less ambiguous. The free plan does seem to be completely free, for any sized companies. The free plan limits the number of people you can collaborate with via cloud projects, but since you can export collections and save them locally you can share the collection with as many people as you want. The 'teams of 3' text is just an example, not a license condition.