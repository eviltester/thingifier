---
title: Kreya HTTP API Rest Client - Overview and Review
description: An overview of the Kreya HTTP REST API Client.
showads: true
---

# Kreya

Kreya is a Desktop Open Source API client.  Free to use for commercial environments with extra features available on a paid plan. Harder to configure proxy for exploratory testing.

## About Kreya

Kreya has a free forever plan and I believe can be used in a commercial environment

[kreya.app](https://kreya.app)

A very capable API client but the proxy configuration is a little hard to setup as it can only be configured using environment variables, and then all application traffic is visible.

It can import Open API files, but I had to amend the collection afterwards because the default base url had been configured incorrectly.

I don't like that I cannot 'see' the actual request that is sent. I was able to view it in proxy but not in the UI.

There are no script capabilities in the free version. But the templating feature is powerful, and it is possible to use it for basic scripting.

Environment variables were a little hard to amend, because a JSON view is shown, but it is possible and the variables can be used in request and headers.

This is only the second tool I've seen with Test Data generation. Faker and Bogus are built in. I have no idea how to use Bogus in this tool as the documentation does not have examples and only `faker.` works in the code completion of the template payloads. [more info](https://kreya.app/docs/templating/#generating-fake-data)

I was able to use the faker capability to generate random data to feed into the simpleapi

```
{
  "type": "cd",
  "isbn13": "{{ faker.random.replace_numbers "#############" #}}",
  "price": 34.2
}
```

It is possible to send custom methods.

Two things make this hard to use:

- inability to see the requests sent
- hard to configure the Proxy

If it was easy to configure the proxy then it wouldn't really matter if I couldn't see the request sent. But templates are hard to debug if you can't see the data generated in the request.

But it is easier to configure the proxy for Kreya than build Yaak from source, so Kreya is part of my recommended list (provided you do configure it to use a proxy).

## Quick Review Criteria

| Feature                                             | Y/N | Comment                                                                  |
|-----------------------------------------------------|-----|--------------------------------------------------------------------------|
| **Essential**                                       |     |                                                                          |
| Send HTTP or HTTPS requests                         | Y   |                                                                          |
| View the actual requests and responses | N   | Last Response only. Request can not be viewed.                           |
| Proxy support                                       | Y   | Hard to configure the proxy as it requires setting environment variables |
| Can create invalid requests                         | Y   |                                                                          |
| **Optional**                                        |     |                                                                          |
| Read Open API files                                 | Y   | Minor edits required after loading from a URL                            |
| Supports global environment variables e.g. for host | Y   |                                                                          |
| Different payload body types                        | Y   |                                                                          |
| Easy to add headers                                 | Y   |                                                                          |
| - custom headers                                    | Y   |                                                                          |
| - auth header support                               | Y   |                                                                          |
| - override body type headers                        | Y   | It is possible to edit as JSON and set content-type header to XML        |
| Variables in headers                                | Y   |                                                                          |
| Custom Verb support                                 | Y   |                                                                          |
| Repeating or cloning earlier requests               | Y   | from the collection list                                                 |
| View history of requests/responses                  | N   | Only last response can be viewed                                         |
| Exporting requests to cURL format                   | N   |                                                                          |
| **Bonus**                                           |     |                                                                          |
| Data driven requests                                | N   |                                                                          |
| Output log of test sessions                         | N   |                                                                          |
| Scriptable for customisation                        | N   | Scripting and Tests available in Pro plan                                |
| Importing cURL requests                             | N   |                                                                          |
| Free for commercial use | Y   |                                                                          |


## Summary - Recommended if you use it through a proxy

If the proxy was easier to use, and the request was visible in the UI then Kreya would be easier to recommend.

I think it is a perfectly capable in its free configuration if you use it through a proxy. Its just a shame the Proxy isn't configurable from a dialog in the UI.

## Kreya Proxy Support

I did manage to get kreya working with Proxy, but unfortunately all requests are sent through the proxy e.g. the messages the app sends to its server as well as the HTTP requests.

I had to start a command line, create the environment variables and then run the tool from the command line `"C:\Program Files\Kreya\Kreya.exe"`

```
set HTTP_PROXY=http://127.0.0.1:8080
set HTTPS_PROXY=http://127.0.0.1:8080
```

To clear env variables on Windows:

```
set HTTP_PROXY=
set HTTPS_PROXY=
```

## Open API Support

I imported an Open API file from a URL, but then each request used the imported Open API file url host was set to the import file i.e. `https://apichallenges.eviltester.com/simpleapi/docs/swagger?permissive`

So it was making requests to https://apichallenges.eviltester.com/simpleapi/docs/simpleapi/items

I had to amend the REST settings at the collection level.

Without proxy support I might not have found this quickly because there is no history of requests made (in the free version).

The Open API import did not pick up the environments in the file so the base url needs to be set at the collection level.

But it does support Open API, which is good.