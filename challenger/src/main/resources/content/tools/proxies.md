---
title: HTTP Proxies - Introduction
description: A list of essential HTTP Proxies tools and how you can use HTTP Proxies to improve your REST API Testing.
---

# HTTP Proxies

- What is an HTTP Proxy?
- Example HTTP Proxies?
- Why use an HTTP Proxy?
- How to direct REST Client through Proxy?
    - Inspect Traffic
    - Filter Traffic (System Proxies)
    - Port Config
    - Replay Request
- Fuzzing

Exercises: in browser - GET, viewing traffic, Ajax requests

---

## What is an HTTP Proxy?

- HTTP Proxy captures HTTP Traffic
- Allows replay of requests
- Allows manipulation of responses

---

## Which proxies?


- [BurpSuite](https://portswigger.net/burp)
    - Free edition more than good enough for API Testing
- [Owasp ZAP](https://www.zaproxy.org/)
    - Open Source
- [Fiddler](http://www.telerik.com/fiddler)
    - Windows (Beta: Linux, Mac)
- [Charles](https://www.charlesproxy.com/)
    - Commercial but allows 30 mins in 'shareware' mode

Fiddler & Charles act as System Proxies making them easy to use with Postman.

I tend to rely on BurpSuite.

---

## Why use when Testing API?

- Record requests
- Create evidence of your testing
- Replay requests outside of client tool
- Fuzzing

For every HTTP REST Client I try, I make sure that I know how to send all HTTP requests through an HTTP Proxy.

This allows me to view every Request I send to the server and response I receive.

Why? Would you want to do this when most REST Clients have a debug view or history view to see the requests?

Well, these views often don't show you the exact request sent.

For example, many REST API tools add additional headers into the request, which they filter out from the debug or history view.

It is very important to know exactly what you have sent to the API Server, and exactly what has been received.

The HTTP Proxies will capture the requests and you can see exactly what the HTTP message is, including all the additional headers the REST Client has added.

Additionally, you can see the full and uninterpreted request from the API.

## Review all the HTTP Traffic

HTTP Proxies are designed to make it easy to see all the requests and responses sent in a session.

Most Proxies will allow you to:

- search the requests and responses
- replay the requests with minor edits
- review historic requests easily to check if you have covered a condition you want to test for
- show a hierarchy of urls to check which end points you have tested

## Save the Evidence

You can also use them as an accurate historical record of the testing you conduct by saving the session as a HAR file.

A HAR file is an HTTP Archive File.

- [wikipedia description of HAR file format](https://en.wikipedia.org/wiki/HAR_(file_format))

This can be easily stored because it is a text file, and later read back into a proxy tool or HAR Viewer to check your testing coverage or review evidence when investigating defects. 



---

## Recommended Trust Exercise

Don't trust your REST Tool History, review what the REST Tool sends and receives against the Proxy

Try this with 3 or 4 different REST API Tools through a Proxy

- GET a request on an endpoint
- where the request has multiple accept headers
- e.g.

```
accept: application/xml
accept: text/html
accept: application/json
```

Why?

HTTP standard says you can't send multiple accept headers and they should be combined like:

```
accept: application/xml, text/html, application/json
```

Check:

- What request does the REST Client send?
   - Did it combine headers? Did it send one? Did it choose the one you expect? Does it complain?
- What response did you receive?
- What did the REST Client say it sent and received?
  - Are the proxy and client in agreement?

Some tools don't let you create extreme circumstances and send invalid requests (which you might want to do when testing).

Some tools choose on your behalf without telling you.