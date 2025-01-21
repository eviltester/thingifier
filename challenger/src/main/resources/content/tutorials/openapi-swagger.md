---
title: OpenAPI / Swagger- Tutorial
description: An introduction to the OpenAPI (Swagger) API Specification Format.
showads: true
---

# Introduction to OpenAPI

## What is the OpenAPI Specification Format?

[OpenAPI](https://www.openapis.org/) is a specification format for documenting APIs. It primarily concentrates on REST and HTTP APIs. Because it was created as part of the Swagger project it is sometimes referred to as the "Swagger API Format".

- [openapis.org](https://www.openapis.org/)
- [swagger.io/specification](https://swagger.io/specification/)

The specification can be written using YAML or JSON, and can be read by tools to:

- automatically create API call templates in REST API Clients
- automatically create HTTP Requests which compare the API response with the specification
- automatically create an interface to allow humans to interact with the API
- automatically generate code to create an SDK to access the API

## How to View an OpenAPI Specification

It is possible to view an OpenAPI specification in any text editor.

But if you are not familiar with the OpenAPI specification then it can be hard to read.

The easiest way to view an OpenAPI specification is to load it into the Swagger Open API Editor. 

- [swagger.io/tools/swagger-editor](https://swagger.io/tools/swagger-editor/)

The editor can be used online at [editor.swagger.io](https://editor.swagger.io/).

You can import the OpenAPI file either from a URL or from a file that you have downloaded.

> Exercise: try the following URLs in the swagger editor
> 
> - https://apichallenges.eviltester.com/simpleapi/docs/swagger
> - https://apichallenges.eviltester.com/docs/swagger
> - https://api.practicesoftwaretesting.com/docs/api-docs.json

The Editor has the benefit that you can see the YAML version of the specification on the left of the screen, and a rendered UI on the right of the screen that allows you to issue requests to the API.

The more detailed the OpenAPI specification file, the more features the UI will have and the more benefit you will gain from the tools that consume the API.

## Running the Swagger Editor locally

I prefer to run the Swagger Editor locally and do so using Docker.

Initially:

```
docker run -p 1235:8080 --rm swaggerapi/swagger-editor
```

The above command will run the Swagger Editor UI, via Docker, on port 1235, and delete the container when I exit the command line.

I can then access the editor by visiting `http://localhost:1235`.

> Exercise: Run the editor locally using Docker
> 
> - Load the URL into the editor https://apichallenges.eviltester.com/simpleapi/docs/swagger
> - Try some requests
> - Edit some of the YAML and see what difference it makes to the Swagger UI

## Disadvantages of Using the Swagger UI and Editor

The Swagger UI and Editor are very convenient tools, but they are designed for using an API and have disadvantages if you want to rely on them for Testing.

### CORS Security

The Swagger UI and Editor make requests to the API using JavaScript calls.

Browsers have security measures to prevent JavaScript making `POST`, `PATCH`, `PUT`, `DELETE` and other requests to a server.

This security is known as [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) - Cross-Origin Resource Sharing

- [mdn CORS documentation](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)

Most Web Applications are strict about CORS and do not send the Response headers that ease up on the Security, so you may find that you cannot issue all the requests you want, to any API, using the Swagger Tools.

The API Challenges APIs have relaxed CORS headers to allow most tools to run against them.

> Exercise: Load the SimpleAPI OpenAPI Spec into Swagger and view CORS Headers
> 
> The SimpleAPI has very permissive CORS headers to allow most API tooling to access it.
> 
> - Load the OpenAPI Spec into a Swagger UI
>    - Access UI [petstore.swagger.io](https://petstore.swagger.io/)
>    - Amend the URL input at the top to be "https://apichallenges.eviltester.com/simpleapi/docs/swagger"
>    - Click [Explore]
>    - Use the API requests
>    - View the Response Headers to see all the CORS headers

APIs that use Swagger to allow people to use the API tend to have the Swagger UI served from the same domain as the API. These allow most requests without having to relax security and set all the CORS response headers.

> Exercise: Try Swagger UIs designed to be used to support all requests
> 
> Try and use the ToolShop Swagger API and Petstore API
> 
> - [api.practicesoftwaretesting.com/api/documentation](https://api.practicesoftwaretesting.com/api/documentation)
> - [petstore.swagger.io/](https://petstore.swagger.io/)

### Designed for Usage Not Testing

The Swagger tools are useful for testing out an API. They tend to have validation on the requests so that fields are enforced as required, or the types are enforced like Integer or Enum etc.

Additionally only valid HTTP verbs will be shown, if a URL does not support a `DELETE` verb then there won't be a `DELETE` option for that URL in the Swagger UI.

When we are testing an API we want to go beyond the normal usage scenarios and want to test all the extreme scenarios, like `DELETE /todos` to delete all the todos, and make sure that the API does not perform the action.

In order to use Swagger UI to do this we would have to amend the OpenAPI specification file. This is why the Swagger Editor can be useful, but only if the API supports CORS requests to allow these actions to occur.

> Exercise: SimpleAPI has a usage OpenAPI spec and a Testing API Spec
> 
> Compare the two OpenAPI specifications provided by the SimpleAPI and see how the restrictions are enforced in the normal OpenAPI spec and how they are relaxed in the permissive OpenAPI spec.
> 
> - [normal SimpleAPI spec for usage](http://localhost:4567/simpleapi/docs/swagger)
> - [Permissive SimpleAPI Spec for Testing](http://localhost:4567/simpleapi/docs/swagger?permissive)


## Using OpenAPI in a REST Client

Most Rest Clients support loading in an OpenAPI file and generating a skeleton collection of requests to allow you to then test the API by editing and issuing the skeleton requests.

- Using Bruno - Import a Collection, from a downloaded OpenAPI file

This is typically the approach that most people testing APIs will use because the REST API client will not be subject to the same CORS restrictions that the Swagger browser based tools encounter.


## Example Open Source Tools for Automated Execution of API from OpenAPI

- [Dredd — HTTP API Testing Framework](https://dredd.org/en/latest/)
- [Postman Contract Test Generator](https://github.com/allenheltondev/postman-contract-test-generator)
- [K6 load generation from OpenAPI](https://k6.io/blog/load-testing-your-api-with-swagger-openapi-and-k6/)
- [Tcases](https://github.com/Cornutum/tcases/blob/master/tcases-openapi/README.md#tcases-for-openapi-from-rest-ful-to-test-ful)
- [Humlix](https://www.humlix.com/)
- [OpenAPI Testing Tools List](https://openapi.tools/#testing)


