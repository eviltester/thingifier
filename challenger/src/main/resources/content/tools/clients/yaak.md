---
title: Yaak HTTP Rest Client - Overview and Review
description: An overview of the Yaak HTTP REST API Client.
showads: true
---

# Yaak

Yaak is an Open Source API client designed for local use. Projects are stored as files so can be shared via Git.

Free to use for commercial environments if the application is built and run from source. Using the pre-built binaries requires a paid licence.

## About Yaak

Yaak is an Open Source API client which requires a paid license if used in a commercial organisation with more than two people, and you want to use the pre-built binaries. $50 per year per user to be compliant with license.

Yaak is completely free if run from source.

Yaak can be found at:

- [Yaak](https://yaak.app/)

To get the most out of Yaak for exploratory testing I found that I needed to configure it to use a Proxy. This was because the Requests are not shown in the Client, only the responses.

This is a very capable basic client which can be used for exploratory testing quite easily.

Does not support scripting, but does support Request Chaining to populate new requests with data from earlier responses.

Good cURL integration with both export and import capabilities.

Provided excellent Open API import, with other import formats supported.

## Quick Review Criteria

| Feature                                             | Y/N | Comment                                                                                                                  |
|-----------------------------------------------------|-----|--------------------------------------------------------------------------------------------------------------------------|
| **Essential**                                       |     |                                                                                                                          |
| Send HTTP or HTTPS requests                         | Y   |                                                                                                                          |
| View the actual requests and responses | Y   | Only the Response can be viewed                                                                                          |
| Proxy support                                       | Y   |                                                                                                                          |
| Can create invalid requests                         | Y   |                                                                                                                          |
| **Optional**                                        |     |                                                                                                                          |
| Read Open API files                                 | Y   | Also populates Environment host variables and param variables                                                            |
| Supports global environment variables e.g. for host | Y   |                                                                                                                          |
| Different payload body types                        | Y   |                                                                                                                          |
| Easy to add headers                                 | Y   |                                                                                                                          |
| - custom headers                                    | Y   |                                                                                                                          |
| - auth header support                               | Y   |                                                                                                                          |
| - override body type headers                        | Y   | It is possible to edit as JSON and set content-type header to XML                                                        |
| Variables in headers                                | Y   |                                                                                                                          |
| Custom Verb support                                 | Y   |                                                                                                                          |
| Repeating or cloning earlier requests               | Y   | But not from the History, only the Request UI                                                                            |
| View history of requests/responses                  | Y   | Only responses can be viewed.                                                                                            |
| Exporting requests to cURL format                   | Y   |                                                                                                                          |
| **Bonus**                                           |     |                                                                                                                          |
| Data driven requests                                | N   |                                                                                                                          |
| Output log of test sessions                         | N   |                                                                                                                          |
| Scriptable for customisation                        | N   | 'Chaining' can be used to 'pull' values from earlier responses.                                                          |
| Importing cURL requests                             | Y   | Auto detects when a cUrl command is in the clipboard.                                                                    |
| Free for commercial use | Y   | When run from source MIT License applies. Prebuilt binary requires commercial licence for companies bigger than 2 people |

## Summary - Recommended but be careful of the licensing

Yaak met all my basic needs when fed through a Proxy. I tried both the pre-built binary and running from source.

Be careful to remain in compliance with the licensing because it uses an Honour System. All features are available, but if you use the pre-built download in a commercial environment then you should buy a licence.

If you are running from source then you are using the MIT License and can use it without payment.

## Review Notes

It will import Open API files and during import, setup Global variables for the cloud server. URL params identified in the Open API file were identified and added to the request automatically e.g. `:id`

Easy to configure routing through a proxy. I configured both the HTTP and HTTPS settings to `127.0.0.1:8080` to fee traffic through BurpSuite.

There is a history of each response which is viewable, responses can be saved out individually. It is not possible to view the actual request, but hooking it up to a proxy solves that.

It is possible to send custom methods.

XML and JSON are pretty printed in the response. It is possible to filter and query XML responses using XPATH. JSONPath can be used to filter JSON responses.

Code completion is available for header names and values

Auth can be setup easily. You have to remember to unset it if you use a header because the Auth tab value takes precedence.

The body header can be overridden. There is no syntax highlighting for the body request.

It is possible to send invalid requests.

There are no automated test capabilities.

The requests and config are stored as yaml files, and the directory to store them can be configured. It is not obvious from the files what the request is e.g. `yaak.rq_cPn7eng93Q.yaml` but at least they can be saved in github easily and yaml makes them easy to review.

It is unfortunate that a folder can not be configured for the history, and then the history saved or exported, possibly as a HAR file.

Yaak offers cURL support. Requests can be copied to cURL by right-clicking on the request in the collection.  The app detects when a curl command is on the clipboard and automatically prompts you to 'import from cURL'.

Support for plugins seems to be on the way.

There is no scripting. But it is possible to chain requests. Chain request does not mean, issue Request 1 then automatically issue Request 2. Chaining Requests means that if Request 1 has been issued then it is possible to use information from the response of Request 1 in a new request. By referencing some path extract function from a response received by an earlier request. It is also possible to use the same mechanism to create global variables which are updated from request responses automatically. e.g. pull out a header value and use in future requests. This is very similar to the approaches used the IDE Rest Clients RestClient and Httpyac).

This is a very capable basic client which can be used for exploratory testing quite easily.

If you use the pre-build application in a commercial environment then you are in breach of the license. Be aware of this it is only 'completely free' if you run from source.

### Running Yaak from Source

Running from source is a little painful only because of the development dependencies:

- install node - https://nodejs.org/en
    - on windows this will also install the Visual Studio C++ development tools required by Rust
- install rust - https://www.rust-lang.org/tools/install

Then to be able to run Yaak locally:

- download the repo zip from https://github.com/mountain-loop/yaak
- unzip into a folder
- in the folder run `npm install`
- `npm run bootstrap`

The above two processes will take about 15 minutes, but you'll have to check the development requirements when you upgrade to new versions.

From then on, running `npm start` in the directory will run `Yaak`

**Note: closing the application the VITE server was still running on port 1420, to allow me to re-run Yaak and stop the VITE server I issued the command `npx kill-port 1420`**

So it isn't quite as convenient as running the pre-built binary, but if you are using this in a commercial environment with more than two people and want to use it as a free tool. This is what you do.