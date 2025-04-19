---
title: Bruno HTTP Rest Client - Overview and Review
description: An overview of the Bruno HTTP REST API Client.
showads: true
---

# Bruno

Bruno is an Open Source API client designed for local use, with Git as the main project sharing mechanism.

## About Bruno

Bruno is an Open Source API client with paid features. The free features have been good enough for everything that I have had to do with Bruno.

Bruno can be found at:

- [usebruno.com](https://usebruno.com)

Bruno is a fast developing project so to get a full understanding of the tool I read the documentation, the blog and the Github feature request forums.

- [Bruno Documentation](https://docs.usebruno.com/)
- [Bruno Blog](https://blog.usebruno.com/)
- [Bruno Github Repo](https://github.com/usebruno/bruno)
- [Support and Feature Request forums](https://github.com/usebruno/bruno/issues)
- [Discussions](https://github.com/usebruno/bruno/discussions)

## Demo Video of Bruno

{{<youtube-embed key="3TlwUKyfOMw" title="Bruno API Client Overview">}}


[Patreon ad free video](https://www.patreon.com/posts/127001249)

## Benefits of Bruno

I find Bruno to be easy to use. Bruno focuses on making it easy to interact with an API either interactively using the UI or automating using the `Test` tabs.

I have not used the `Test` capabilities because I automate using HTTP libraries. But I have used the Scripting capabilities to generate random data and log the output of my testing.

## Quick Review Criteria

| Feature                                             |Y/N| Comment                                                           |
|-----------------------------------------------------|-----|-------------------------------------------------------------------|
| **Optional**                                        ||                                                                   |
| Read Open API files                                 |Y| Also populates Environment host variables                         |
| Supports global environment variables e.g. for host |Y| With syntax highlighting and linting                              |
| Different payload body types                        |Y|                                                                   |
| Easy to add headers                                 |Y|                                                                   |
| - custom headers                                    |Y|                                                                   |
| - auth header support                               |Y|                                                                   |
| - override body type headers                        |Y| It is possible to edit as JSON and set content-type header to XML |
| Variables in headers                                |Y|                                                                   |
| Custom Verb support                                 |N| GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD                      |
| Repeating or cloning earlier requests               |Y| But not from the History, only the Request UI                     |
| View history of requests/responses                  |Y| But no export capability                                          |
| Exporting requests to cURL format                   |Y|                                                                   |
| **Essential**                                       ||                                                                   |
| Send HTTP or HTTPS requests                         |Y|                                                                   |
| Proxy support                                       |Y|                                                                   |
| Can create invalid requests                         |Y|                                                                   |
| **Bonus**                                           ||                                                                   |
| Data driven requests                                |Y| Paid plan only                                                    |
| Output log of test sessions                         |N|                                                                   |
| Scriptable for customisation                        |Y| At Collection and Request level                                   |
| Importing cURL requests                             |Y| When creating a request can import cURL                           |

Bruno met all my basic needs. The Data Driven feature is only available on a paid plan.

For people who like to automate in the REST Client, the `test` suite capability can be used on the free plan.

## Notes on Usage

Bruno saves all the collection files in a folder. This makes it easy to review and manage in version control.

I haven't used the direct Git integration features in Bruno, preferring to manage the Collection versioning using Git directly.

Bruno makes the version control easy by keeping all data in simple files, and multiple files are used rather than a single database file or large JSON or XML file.

When I imported an Open API file with environment data the environment variables in Bruno were set correctly and I was able to use the remote server or local server without configuring the collection in the UI.

The Payload body editor uses the syntax of the payload type selected e.g. JSON payloads are colour coded and syntax checked.

The editor did not prevent me from sending through invalid payloads. i.e. the payload was shown as invalid in the UI but still sent through to the server which is exactly what I want.

Headers are easy to add. Code completion on Headers is available for the value, rather than the key.

Multiple authentication types are supported by the UI making it easy to add Bearer tokens or API keys. These can also be added directly through the Header amendment and can use values in variables.

Variables can be created at multiple levels:

- Collection
- Request
- Environment
- Local OS Environment

I tend to stick to Environment variables. But have experimented with Collection variables for random data.

Bruno has recently added Dynamic Data so Faker can be used to add random data into a payload.

e.g. `{{$randomCurrencyCode}}`

This is documented in the [Dynamic Variables](https://docs.usebruno.com/testing/script/dynamic-variables) section.

Bruno changes frequently.

## Conclusion

Bruno is my current default REST API tool for Exploratory and Interactive Test Sessions.

I find it:

- easy to use.
- plays well with other tools e.g. Proxies
- easy to script and generate random data
- imports Open API files well
- has a small set of features to the UI is uncluttered

The paid plan as the Data Driven capabilities. But if I want Data Driven I either use a Proxy or code the automated execution. 

## Appendix - Experiments in Scripting Bruno

I use the Scripting capabilities of tools on an ad-hoc basis to improve my workflow, rather than automating the coverage.

I found the Scripting in Bruno to be very suitable for my purposes.

Bruno comes with useful libraries built in, and it is possible to treat your [Bruno collection as a Node project](https://docs.usebruno.com/testing/script/external-libraries) by adding a `package.json` and adding whatever library you want.

### Random Data

I first experimented using Random Data in Bruno. The built in Faker `{{$randomCurrencyCode}}` capability is useful but I wanted more custom random data.

e.g. the [Simple API](https://apichallenges.eviltester.com/practice-modes/simpleapi) requires a unique ISBN number for each item

I used Bruno to make a random ISBN available to each request, if I wanted to use it.

In the Script section of my collection I added:

```
bru.setVar("isbn13",'xxx-x-xx-xxxxxx-x'.
    replaceAll('x', (match, p1, offset) => {return Math.floor(Math.random() * 10)}));
```

This means on every Request, there is a variable called `isbn13` which has been set to a random valid ISBN 13 format, which I could use in the request by adding `{{isbn13}}` anywhere in the request. This gave me the ability to test quickly when I didn't care what ISBN to use, and I could easily use a String when I wanted more control.

I noticed that one of the [supported libraries](https://docs.usebruno.com/testing/script/inbuilt-libraries) when the collection is opened in Developer mode is lodash. so I used lodash to randomly select a 'type' from an Array. I could have used normal JavaScript for this but wanted to experiment with the Scripting capabilities.

I found I had to `require` lodash as follows:

```
var _ = require('lodash');
bru.setVar("type",_.sample(['cd', 'dvd', 'blu-ray','book']));
```

The code above created a `{{type}}` variable with a random type if I wanted to use it in a payload.


e.g. in a POST payload

```
{
  "type": "{{type}}",
  "isbn13": "{{isbn13}}",
  "price": "13",
  "numberinstock": "12"
}
```

The Scripting approach was well documented and easy to use.

### Logging Test Sessions

I started writing a script to run at a Collection level at a post response event level (after every response).

I wanted to write the request and response object to a file to keep track of my testing.

I found that I had to use a feature that is documented but not obviously documented.

Amending the `bruno.json` file in the collection to allow scripts to access the file system.

```
 "scripts": {
   "filesystemAccess": {
     "allow": true
   }
 }
```

This is shown in the documentation in some code sections but I found it by searching Github for other questions related to file system access through Scripting.

- https://github.com/usebruno/bruno/issues/306

This is also when I found that there is a section in the Discussions where people have uploaded their own scripts.

- [Bruno - Scriptmania](https://github.com/usebruno/bruno/discussions/385)

This is where I found a script that closely matched what I wanted:

- https://github.com/usebruno/bruno/discussions/385#discussioncomment-7246390

The above script:

- requires creating a `logs` folder in the collection
- writes a JSON representation of the request and response to a time stamp named file

I amended the script to:

- create the `logs` folder
- write all the responses to a markdown formatted file
- with one file for each hour
- if a `TestNote` variable is in the Request section then it will be written as a paragraph in the log output

This more closely matches my Test Approach, where I Test for small chunks of time, and create notes in a text editor about the work I'm doing.

The script I used is below, and it was pasted into the Collection `post response` Script section:

```
// based on https://github.com/usebruno/bruno/discussions/385#discussioncomment-7246390

const fs = require('fs');
const path = require('path');
const moment = require('moment');

const safeStringify = function (obj) {
  try {
    return JSON.stringify(obj, null, 2);
  } catch (err) {
    return obj
  }
};

const request = {
  url: req.getUrl(),
  headers: req.getHeaders(),
  method: req.getMethod(),
  body: req.getBody()
};

const response = {
  status: res.getStatus(),
  headers: res.getHeaders(),
  body: res.getBody()
};

const now = moment();
const fileNameFormattedDate = now.format('YYYYMMDD-HH');

const dir = path.join(bru.cwd(), 'logs')
if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir);
}

const filename = path.join(dir, `${fileNameFormattedDate}.md`);
const content = safeStringify({request, response}, null, 2);

let title = "\n\n## " + new Date().toLocaleString() +
  " - " + res.getStatus() + " " + 
  req.getMethod() + " " + req.getUrl() +
  "\n\n";

fs.appendFileSync(filename, title);

let comment = bru.getRequestVar("TestNote")

if(comment!=null){
  fs.appendFileSync(filename, "\n\n" + comment + "\n\n");
}

fs.appendFileSync(filename, content);
```
