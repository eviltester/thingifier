# POST /challenger (201)

> Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion.

- This challenge is essential if you want to persist your sessions in multi-user mode
- This challenge is optional if you want to work in single-user mode

## Debrief

### APIs are not always intuitive

This is the first challenge that many people will do, because they want to save their challenges. Unfortunately it isn't the most natural place to start when learning about APIs.

But that is a useful point to debrief on, because APIs are not always the most accessible way to learn and interact with an application.

Because they are designed for 'programs', 'systems', or 'applications' to interact with, not necessarily people.

And sometimes we have to use APIs in certain ways, because they tell us to. APIs don't usually offer as much flexibility in how we interact with them. Unlike GUIs which often have more than one way to achieve a result. With an API there is one way to do things.

And so with the challenges app, if we want to persist our sessions then we have to comply with the process:

- `POST /challenger`
- use the `X-CHALLENGER` header in all future requests.

### What does this do? Why is this a POST?

If I was describing this to a person, I might say:
 
- "I need the system to give me an X-CHALLENGER code, so that I can track my challenge session"
- "I need to get an X-CHALLENGER code to track my challenges"

What we are actually saying to the system is:

- "I need you to create a challenger session, and tell me what it is"

We are asking the system to make a change on the server side, in response to this request.

We use a `POST` request to do that.

If I used a `GET` request, and the system created a `challenger` session in response then that is not in the spirit of the HTTP specification.

`GET` should be cacheable, and return information, not create information as a side-effect.
 
`POST` is used to create information on the server.

## Status Code

- `201` Created
- What other status codes could be provided?
   - API documentation doesn't say
   - Assume: 405
   - https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/405
   - https://httpstatuses.com/405

## What are the headers in the response?

The response has two important headers for us.

- `X-CHALLENGER`
- `LOCATION`

Notes:

- `X-CHALLENGER` is a custom header.
- applications are free to create custom headers
- `X-...` used to be the standard recommended approach
- Based on this 'standard' it is now a deprecated recommendation, but still used
    - https://tools.ietf.org/html/rfc6648
    - "this is a matter for the designers of those protocols"
    
Notes: `Location`

- https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Location
- used with 303 and 201
- if a browser then it will follow Location header
- semantics are 'looser' for APIs

## Cookies?

- Cookies are not used in APIs as frequently as GUI
- Cookies can often be scraped from GUI and passed to API
     - not always the best approach
     - "The Pulper" practice app can use cookies to access the data, if you want to practice with cookies then try that.
     - https://www.eviltester.com/page/tools/thepulper/
     
     
     
## Video

> API Challenges - POST Challenger 201 Debrief

[youtu.be/YJDyCow1QTE](https://youtu.be/YJDyCow1QTE)

A debrief and lessons learned on the "POST Challenger 201"

- What is a POST Request?
- How does POST Request differe from a GET request?
- How to identify a Custom HTTP Header?
- What is a Custom HTTP Header?
- Standards for Custom HTTP Headers?
- What is a 201 status code?
- Dangers of duplicate headers.
- How to learn about status codes?
- What is a Location header?

Useful links:

- https://tools.ietf.org/html/rfc6648
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/201
- https://httpstatuses.com/201
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/405
- https://httpstatuses.com/405
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Location

Find the application links and more information at:

- https://eviltester.com/apichallenges
     
---

Patreon ad free video version: https://www.patreon.com/posts/39882304     