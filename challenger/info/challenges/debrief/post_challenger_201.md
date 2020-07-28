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

`GET` should be cacheable, and return information, not create information.

`POST` is used to create information on the server.

## What are the headers in the response?

The response has two important headers for us.

- `X-CHALLENGER`
- `LOCATION`