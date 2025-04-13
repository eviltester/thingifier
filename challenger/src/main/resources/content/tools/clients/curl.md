---
title: cURL HTTP Client - Overview and Review
description: An overview of the cURL HTTP Client.
showads: true
---

# cURL

cURL is an Open Source API CLI based client for making HTTP requests.

## cURL Overview

cURL is a command line tool. Available by default in most Linux and Mac systems. Windows ships with an alias for `curl` and cURL needs to be installed on Windows to use properly.

- Official Site - [cURL](https://curl.se)

Because it is a command line tool it can be more difficult to use. But it is an essential tool to have at your disposal. Most other REST Clients will be able to generate a cURL command.

cURL is useful because when you raise a defect, you don't want to try and attach a Postman of Bruno Collection, but you can easily paste in a `cUrl` command that can trigger the issue, and can reasonably expect that the programmers on the team will have cURL installed.

~~~~~~~~
curl  {{<ORIGIN_URL>}}/heartbeat -i
curl -X GET {{<ORIGIN_URL>}}/todos
curl {{<ORIGIN_URL>}}/todos -H "accept: application/xml"
~~~~~~~~

You might also find cURL useful for data driven testing, if your REST Client does not support feeding in a file of data to a request then you can probably find a tutorial online for the operating system you are using, generate the request from the REST API Client, and feed a file of data into the request.

[cURL](https://curl.se) Can be complicated but useful for emergencies, scripting, bug reporting.

> **NOTE**: By default Windows Powershell has a `curl` command but it is not really curl. It is an alias around `Invoke-WebRequest` and does not have all cURL features or use all the command line parameters. e.g. all the cURL examples listed above would fail. But if you change `curl` to `curl.exe` you can start using the non-alias Windows preinstalled `cURL` tool. 
 

## cURL Support From Other REST Clients

cURL came first. Before Postman, Insomnia, Bruno or any other REST Client. If you wanted a tool to make HTTP requests you used cURL.

This is why, when you use the Swagger UI. For each of the requests you will see the cURL command that can replicate that request.

Other Rest Clients will also generate cURL requests.

- In Bruno, if you click on 'Generate Code' then you can choose "Shell-Curl" and it will generate a cURL command matching the request you are currently editing.

> **HINT**: you can use Postman or Insomnia to generate cURL code but different continuation characters on different operating systems: `^` Windows and `\` on Mac/Linux also `"` and `'` differences.

Postman and Insomnia can generate a request in the UI by pasting a cURL request into the request URL in the application.