---
title: API Challenges Open API Swagger File
description: Download the Open API files for the API Challenges.
showads: true
---

# API Challenge Open API Files

Download an OpenAPI/Swagger JSON file to use in your REST Client.

## File Download Links

Two OpenAPI/Swagger formatted files are available.

- [Download Normal OpenAPI File](/docs/swagger) - to use API like a User
- [Download Permissive OpenAPI File](/docs/swagger?permissive) - less validation and more suitable for supporting testing

## About API Challenge's Normal OpenAPI File

The Normal OpenAPI File is intended for use as though you were a user.

It only lists endpoints that are valid to use, and has additional validation on the URL Parameters that you can enter.

When this type of file is loaded into a Swagger UI Generation application it makes it easy to USE the API but makes it harder to TEST the API.

- [Download Normal OpenAPI File](/docs/swagger)

## About API Challenge's Permissive OpenAPI File

The Permissive OpenAPI File is intended for testing.

It lists all the end points with more Verbs i.e. even verbs that the API defines as not available.

The parameters are also possible to send as empty and type validation is not performed on the parameter values. This makes it possible to use Swagger UI applications to test more extreme situations.

- [Download Permissive OpenAPI File](/docs/swagger?permissive)