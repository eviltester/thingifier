---
title: Testing APIs - Tutorial
description: Basic Testing REST API tutorial and how to think when thinking about testing an API.
---

# Testing a REST API

- how to model an API
- testing ideas

---

## Testing different from Technology and Tooling

- at this point we have discussed technology and tooling
- time to discuss testing

---

## What would we test?

- Risk
- Coverage
- Functionality and Outcomes

---

## Coverage Driven Testing

REST APIs are a very 'pure' system.

- Input -> Process -> Output
- Request -> Process -> Response

Most of the variation comes from:

- Input
- Current System State

With a UI we have to worry about variation like:

- which browser?
- exactly how I interact e.g. time between click and release of mouse, did I hold a key at the same time? etc.

---

## Coverage of What?

- Verbs - have you used every verb with every end point?
- Endpoints - have you tried them all?
- Swagger - have you used the Swagger API document?
- Documentation - have you read the docs?
- Query Params - have you tried combinations?
- formats (content and accept) - have you varied XML, JSON, Text and others?
- State - Get when missing, Create when exists? etc.

---

# What are the architecture risks?

- Client -> Web Server -> App Server -> App
- Do we understand the architecture?

---

# What are the capacity risks?

- Performance?
- Load Testing?

---

# What are the security risks?

- Authentication
- Authorisation
- Injection
- What headers are accepted? X-HTTP-Method-Override?

---

# Data Risks

- minimum data in requests - missing fields, headers
- not enough data in requests
- wrong format data: json, xml, length, null, empty
- malformed data
- consistency? query params across requests?
- are defaults correct?
- duplicate data in payloads?
- headers: missing, malformed, too many, duplicate

---

# Document your testing

- How can you document your testing?
- Mindmaps?
- Text files
- Record all requests through an HTTP Proxy and store as a HAR file

---

# Other Risks or Common Issues?

---

# Exercise: Think through testing

- Read the requirements
- Create some test ideas
- Look at the existing testing conducted
- Any ideas from that?
- Test
- Document and Track your Testing in a lightweight fashion
- Try different tools
- Run all your requests and responses through a Proxy and review the results - you might be surprised to see differences that your REST Client Tool did not reveal.

