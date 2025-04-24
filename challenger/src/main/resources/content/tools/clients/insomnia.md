---
title: Insomnia HTTP Rest Client - Overview and Review
description: An overview of the Insomnia HTTP REST API Client.
showads: true
---

# Insomnia

Insomnia is a free to use REST API Client that requires a login to use properly. Paid plans are required to have control of where data is stored.

## About Insomnia

[Insomnia](https://insomnia.rest/) used to be my first choice of REST Client for API Testing. Then the company required a login and I switched to Bruno.

- [Insomnia.rest](https://insomnia.rest/)
- [Insomnia on Github](https://github.com/Kong/insomnia)
- [Insomnia Plugins](https://insomnia.rest/plugins)

It is possible to use without signing in if you click the `use local scratchpad` option on the login screen. This allows you to work with a single 'scratchpad' collection. I'm not sure where it is stored, but it is possible to export to Insomnia or HAR format.

- Can Import many formats of API definition, including Open API
- A History of requests can be seen, but cannot be exported.
- cURL is well support for import and generation.
- Environment variables are supported in requests and headers.
- Proxy is easy easily configured.
- Requests can be organised into folders.
- Scripting is available in the free version.
- Custom Methods can be used.
- Plugins can be added for additional features

After logging in you have the ability to create more collections, which are saved in the cloud and 'locally' somewhere. Git Sync feature where you control the location of collections is only available on a paid plan.

I was surprised to see that Insomnia, although requiring a login for the most part, was still a capable exploratory testing client.

## Quick Review Criteria

| Feature                                             | Y/N | Comment                                                         |
|-----------------------------------------------------|-----|-----------------------------------------------------------------|
| **Essential**                                       |     |                                                                 |
| Send HTTP or HTTPS requests                         | Y   |                                                                 |
| View the actual requests and responses | Y   | In the console view                                             |
| Proxy support                                       | Y   |                                                                 |
| Can create invalid requests                         | Y   |                                                                 |
| **Optional**                                        |     |                                                                 |
| Read Open API files                                 | Y   |                                                                 |
| Supports global environment variables e.g. for host | Y   | Editable from a JSON object view                                |
| Different payload body types                        | Y   |                                                                 |
| Easy to add headers                                 | Y   |                                                                 |
| - custom headers                                    | Y   |                                                                 |
| - auth header support                               | Y   |                                                                 |
| - override body type headers                        | Y   | It is possible to edit as JSON and set content-type header to XML |
| Variables in headers                                | Y   |                                                                 |
| Custom Verb support                                 | Y   |                                                                 |
| Repeating or cloning earlier requests               | Y   | From the hierarchical view                                      |
| View history of requests/responses                  | Y   | A History is stored but not exportable                          |
| Exporting requests to cURL format                   | Y   |                                                                 |
| **Bonus**                                           |     |                                                                 |
| Data driven requests                                | Y   | From the 'run' dialog where a full set of requests is executed  |
| Output log of test sessions                         | N   |                                                                 |
| Scriptable for customisation                        | Y   |                                 |
| Importing cURL requests                             | Y   |                           |
| Free for commercial use | Y   | Additional features are available on paid plan                  |

Insomnia has a very clean and uncluttered UI and is still a useful API client.

## Summary - Easy to use. Scratchpad mode for non-logged in use.

Requires login to get multiple collections. And a Paid plan to control where collections are saved.

Most features seem to be available on the free plan.

I prefer the convenience of saving locally with Bruno, but Insomnia remains a very capable REST API Client and supports Exploratory Testing well.

## Review Notes

Importing an Open API file automatically filled in the environment variables.

History of requests sent is maintained, but cannot be exported.

It is possible to convert requests to cURL.

It is also possible to import from curl, you have to use the `(+)` symbol next to the filter.

It is possible to create variables in the environment editing. This is done by editing a raw json object rather than a table like dialog view.

Easy to configure a proxy via the preferences dialog.

It is possible to organise requests into folders.

Scripting is available in the free version.

It is possible to create Custom Methods.

In scratchpad mode, to save your work you would have to export the collection.

You can export in Insomnia 5 format or HAR format.

It is possible to run all the requests in a collection and order them prior to sending.

Body payloads have syntax highlighting and it is possible to send invalid data. It is also possible to override the content type header

Header names have text completion, the values do not.

It is possible to use Insomnia in scratchpad mode as a capable free exploratory testing client.

When logged in it is possible to work on multiple collections. And these can be locally stored. I am not sure where they are locally stored so they are not suitable for sharing. The Git Sync feature where you can choose the folder is part of the Team Plan. It is possible to store your projects in cloud and collaborate, but I think only for 1 project.

Insomnia is still a capable tool for local exploratory testing and free to use. Can be used without login in scratchpad mode, you just don't get shared collections online.