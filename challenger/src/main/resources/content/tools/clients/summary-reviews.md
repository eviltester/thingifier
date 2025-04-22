---
title: REST API Client Suitability for Exploratory Testing Summary Reviews
description: A list REST Clients with a summary review of each concerning their support for Exploratory Testing.
showads: true
---

# REST API Client Support for Exploratory Testing Summary Reviews

## Introduction

There are so many REST Clients available I have tried to list all those that I have evaluated here, with a summary of the evaluation results. The evaluation criteria was 'how well does the tool support exploratory testing'. If you also use the REST API Client for 'Test Automation' then you will evaluate and rank these tools differently.

The main headings are listed in alphabetic order. To see a recommended grouping check the section below.

For Exploratory Testing I use Bruno. I evaluated the tools below to make sure I have alternative tools and know the limits of each tool.

*Tools last evaluated in April 2025.*

A list of Open Source API Clients is available at the [Awesome API Clients Github list](https://github.com/stepci/awesome-api-clients). Many of desktop tools listed are reviewed below.

## Grouped Recommendations for Exploratory Testing

All of the tools listed in this section are recommended. If you are evaluating a tool for Exploratory Testing then start with these.

### Recommended Lightweight Free Clients

- Bruno [review](/tools/clients/bruno) - good all-rounder, free for commercial use. **Recommended** [usebruno.com](https://usebruno.com)
- Milkman [review](/tools/clients/milkman) - a little rough around the edges, but open source and free for commercial use [Milkman on github](https://github.com/warmuuh/milkman)
- Kreya [review](/tools/clients/kreya) - proxy can only be configured via environment variables but capable when configured [Kreya](https://kreya.app)
- Yaak [review](/tools/clients/yaak) - lacks scripting, free for commercial use if run from source. [yaak.app](https://yaak.app/)



## All Evaluated Client Summaries

All recommended clients have longer reviews.

I added tools in here that I do not recommend, simply because they are available, if you search the web you will find them. If you do have good experiences with tools that I have not recommend then feel free to [let us know](https://www.linkedin.com/in/eviltester).

### Advanced Rest Client

- [Advanced Rest Client on Github](https://github.com/advanced-rest-client)
- Hasn't been updated in a few year
- I tried to open an Open API file and it failed. 

Since there are so many other Open Source and free API clients, I do not recommend.

### API Dash

[API Dash on github](https://github.com/foss42/apidash/releases)

- Free and open source desktop client - last release Nov 2023
- Super Simple. Create collections, but I have no idea where they are saved.
- Can generate cURL commands
- No proxy support.
- No Open API import support.
- No environment variable support
- Cannot see history of requests and responses
- Cannot see the request sent
- no scripting capabilities
- no custom methods

Would not recommend given the availability of other tools. Does not seem to be under active development.

### Bruno

- Bruno is Open Source.
- Free for commercial use.
- Aids Exploratory Testing well with good Proxy support.
- Free plan also allows scripting.
- Paid plans for CI usage, data driven and automated assertions
- Good cURL integration with both export and import capabilities.
- Provides Open API import, and other import formats supported.
- [usebruno.com](https://usebruno.com)

Read our [full review of Bruno](/tools/clients/bruno)

### Cartero

I'm tempted to view Cartero more like a front end for creating cURL requests rather than a REST API Client. It seems to build cURL requests quite well.

- Allows you to create single requests, which are stored in files.
- Cannot see a way to set a proxy.
- No concept of collections.
- You can't copy requests, but you could 'save as' new file and then load it back.
- No concept of environment variables.
- It does allow adding headers and parameters but there is no auto complete.
- It does allow exporting to cURL
- [Cartero](https://cartero.danirod.es/)

### Kreya

Kreya would be much easy to recommend with simpler Proxy configuration. But it is easier to configure the proxy for Kreya than build Yaak from source.

- A free forever plan and I think can be used in a commercial environment
- Can import Open API files
- Can use a proxy but only if set environment variables.
- No script capabilities in the free version.
- A templating feature is avaiable with some ability to script.
- Faker is built in to generate random data
- Can use custom methods.
- [Kreya](https://kreya.app)

Read our [full review of Kreya](/tools/clients/kreya)


### HTTPIE

- Free for commercial use
- Requires a login for basic functionality like 'choosing where to save files'
- Does not support HTTP Proxy
- Does not support Open API import - but can import from Postman and Insomnia
- Pretty UI
- Login for experimental Generative AI features
- cURL Support
- Can use Custom Methods
- No Scripting
- No request and response history
- [Httpie](https://httpie.io)

Requires Open API import and Proxy Support to be viable for supporting Exploratory Testing.

Read our [full review of Httpie](/tools/clients/httpie)

### Httpyac

- VS Code plugin tool
- Write and Issue API requests from text file
- Allows the creation of a literate HTTP testing spec
- Scripting and Request Chaining capabilities
- To get the proxy working I had to configure vs code `preferences > settings` to 'on' and not 'override'.
- I have an example `.http` file in the thingifier repo: [httpyac example](https://github.com/eviltester/thingifier/tree/master/docs/httpyac)
- [Httpyac](https://httpyac.github.io/)

### MilkMan

I installed milkman by downloading the nightly release zip files.

[Milkman](https://github.com/warmuuh/milkman)

- MilkMan is a JavaFX application and should run on windows, mac and linux.
- Import Open API files from disk
- Can not see the actual request sent
- Json responses can be queried using JMESPath
- Supports simple scripting using [plugin](https://github.com/warmuuh/milkman/blob/master/milkman-scripting/readme.md)
- Can configure a proxy
- All Headers are manually configured. Can add custom headers. Header names do have auto completion, header values do not
- Not support custom methods
- Collections are saved to the database.db in the application folder when the application exits. Exportable to Postman.
- Environment variable support.

A completely free and Open Source REST API Client with collection and Proxy support.

Read our [full review of Milkman](/tools/clients/milkman)

### Pororoca

- I had huge problems with this application.
- I could import an Open API collection, but then I could not make any requests.
- Most of the time when I clicked on an imported request the UI would go blank.
- When I finally did manage to click Send for a valid URL, the application shut down.
- From what I saw, there was no way to set a proxy.
- And it didn't look like a history of requests was maintained.
- [Pororoca](https://pororoca.io/intro/download)

Perhaps I downloaded an unluckily buggy version but I can't recommend.

### REST Client

Listed here but requires future evaluation. These are initial notes.

- Rest Client seems to be very similar to Httpyac
- Rest Client seems to support a subset of curl so it might be possible to write cURL directly into the editor.
- Does not get updated very often, last commit (October 2024)
- Seems like a very interesting tool. Evaluate at the same time as HttpYac
- [Rest Client](https://github.com/Huachao/vscode-restclient)

### Thunder Client VS Code extension

- The licensing model makes it hard to evaluate.
- A Paid license is required for basic functionality.
- Hard to recommend given the free plan available in Bruno or Yaak
- Evaluate if you want a VS Code based IDE with cheap commercial license
- [thunderclient.com](https://www.thunderclient.com/)

### Yaak

- This is a very capable basic client which can be used for exploratory testing quite easily.
- Open Source API client which requires a paid license if pre-built binaries are used in a commercial organisation with more than two people.
- $50 per year per user to be compliant with license. All features available for evaluation period.
- Yaak is completely free for commercial use if run from source.
- Does not support scripting, but does support Request Chaining to populate new requests with data from earlier responses.
- Good cURL integration with both export and import capabilities.
- Provided excellent Open API import, with other import formats supported.
- To get the most out of Yaak for exploratory testing I found that I needed to configure it to use a Proxy because the Requests are not shown in the Client, only the responses.
- Supports custom methods.
- [Yaak](https://yaak.app/)

Read our [full review of Yaak](/tools/clients/yaak)



















