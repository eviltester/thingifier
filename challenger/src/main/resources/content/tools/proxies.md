---
title: HTTP Proxies - Introduction
description: Basic HTTP Proxies and introductory information.
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
- [Owasp ZAP](https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project)
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