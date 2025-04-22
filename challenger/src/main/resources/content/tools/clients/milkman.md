---
title: MilkMan HTTP API Rest Client - Overview and Review
description: An overview of the MilkMan HTTP REST API Client.
showads: true
---

# Milkman

Milkman is a Desktop Open Source API client. Completely open source and community driven.

## About Milkman

Milkman is one of the few Open Source projects with no commercialisation attached. So it is free to use in a commercial environment, no features are locked away and no licensing issues.

Because it is a community project it is a little rough around the edges but it is well featured and performed well in my evaluation.

I installed Milkman by downloading the nightly release zip files.

The basic concept of Milkman is a Workbench that is highly extensible through community contributed plugins. Milkman is a JavaFX application and should run on windows, mac and linux.

[Milkman](https://github.com/warmuuh/milkman)

Since this works, and is completely open source, it is a viable tool for Exploratory Testing. The main concern is the storage of collections in a database in the app folder, so make sure you export the collections regularly to Postman format to version control them.

- Can import Open API files from disk
- Uses the environment variables to configure environments listed in Open API file
- Json responses can be queried using [JMESPath](https://jmespath.org/)
- Possible to have simple scripting using [plugins](https://github.com/warmuuh/milkman/blob/master/milkman-scripting/readme.md)
- Can configure a proxy
- Can add custom headers. Header names do have auto completion. Header values do not.
- it is very easy to send through invalid requests
- Auth looks like it needs to be handled manually by crafting headers
- It is possible to export a collection as a postman collection
- Possible to create environment variables
- Content type header is not automatically added after choosing a content-type in the editor, so configure header manually
- Can not see the actual request sent
- Custom methods are not supported
- Collections are saved to the database.db in the application folder when the application exits, rather than plain text files

Shame this doesn't save to local files that are easy to version control like Bruno. But since this can work with a proxy I could consider using this as an emergency API client for exploratory testing. Or a fall-back tool, just in case all the other easy to use tools lock basic features behind a paywall or registration wall.

## Quick Review Criteria

| Feature                                             | Y/N | Comment                                            |
|-----------------------------------------------------|-----|----------------------------------------------------|
| **Essential**                                       |     |                                                    |
| Send HTTP or HTTPS requests                         | Y   |                                                    |
| View the actual requests and responses | N   | Request can not be viewed.                         |
| Proxy support                                       | Y   | Easy to configure e.g. `http://127.0.0.1:8080`     |
| Can create invalid requests                         | Y   |                                                    |
| **Optional**                                        |     |                                                    |
| Read Open API files                                 | Y   | from disk                                          |
| Supports global environment variables e.g. for host | Y   |                                                    |
| Different payload body types                        | Y   | All headers are manually configured                |
| Easy to add headers                                 | Y   |                                                    |
| - custom headers                                    | Y   |                                                    |
| - auth header support                               | N   | Can send Auth headers. Manually configure only     |
| - override body type headers                        | Y   | No header type set by default. Manually configure. |
| Variables in headers                                | Y   |                                                    |
| Custom Verb support                                 | N   |                                                    |
| Repeating or cloning earlier requests               | Y   | Duplicate option available on tab right click      |
| View history of requests/responses                  | N   | Only last response can be viewed                   |
| Exporting requests to cURL format                   | Y   | Save > export...                                   |
| **Bonus**                                           |     |                                                    |
| Data driven requests                                | N   |                                                    |
| Output log of test sessions                         | N   |                                                    |
| Scriptable for customisation                        | Y   | Not evaluated, but capability is there             |
| Importing cURL requests                             | N   |                                                    |
| Free for commercial use | Y   |                                                    |

## Summary - Recommended as Completely Open Source and Free

Given that it is completely free and open source, has proxy support and can handle Open API files. Milkman has to be an option if looking for completely free tooling.

